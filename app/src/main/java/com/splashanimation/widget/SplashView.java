package com.splashanimation.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.splashanimation.R;

/**
 * Created by wangzhiguo on 15/9/7.
 */
public class SplashView extends View {

    //大圆(里面包含很多小圆)的半径
    private float mRotationRadius = 90;
    //每一个小圆的半径
    private float mCircleRadius = 18;
    //小圆圈的颜色值表，在initialize方法里面初始化
    private int[] mCircleColors;
    //大圆和小圆旋转的时间
    private long mRotationDuration = 1200;//ms
    //第二部分动画的执行总时间（包括两个动画时间，各占1/2）
    private long mSplashDuration = 1200;//ms
    //整体北京颜色
    private int mSplashBgColor = Color.WHITE;

    /**
     *
     * 参数，保存了一些绘制状态，会被动态的改变
     */
    //空心圆的初始半径
    private float mHoleRadius = 0F;
    //当前大圆旋转角度（弧度）
    private float mCurrentRotationAngle = 0F;
    //当前大圆半径
    private float mCurrentRotationRadius = mRotationRadius;

    //绘制圆的画笔
    private Paint mPaint = new Paint();
    //绘制背景的画笔
    private Paint mPaintBackground = new Paint();

    //屏幕正中心点坐标
    private float mCenterX;
    private float mCenterY;

    //屏幕对角线的一半
    private float mDiagonalDist;

    private SplashState mState = null;
    private abstract class SplashState {
        public abstract void drawState(Canvas canvas);
    }

    public SplashView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mCircleColors = context.getResources().getIntArray(R.array.splash_circle_colors);
        //画笔初始化
        //消除锯齿
        mPaint.setAntiAlias(true);
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setColor(mSplashBgColor);
    }

    /**
     * 当View呈现出来的时候会调用onSizeChanged方法
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //初始化中心点坐标
        mCenterX = w / 2f;
        mCenterY = h / 2f;
        //对角线的一半
        mDiagonalDist = (float)(Math.sqrt(w * w + h * h)/2);

    }

    //进入主界面－－－开启后面的两个动画
    public void splashAndDisappear() {
        //先要让第一个旋转动画停下来
        ((RotationState)mState).cancel();
        mState = new MergingState();
        //提醒view重新绘制－－－onDraw方法
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设计模式－－模版模式
        //这里面就做一个简单的绘画事件分发
        if(mState == null) {
            //第一次执行动画
            mState = new RotationState();
        }
        mState.drawState(canvas);
    }

    /**
     * 旋转动画
     */
    private class RotationState extends SplashState {
        private ValueAnimator mAnimator;
        public RotationState() {
            //小圆的坐标－－－－>大圆的半径，大圆旋转了多少角度
            //估值器，1200ms时间内计算某个时刻当前角度是：0～2PI
            mAnimator = ValueAnimator.ofFloat(0,(float)Math.PI * 2);
            //线性差值器
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration(mRotationDuration);
            //设置监听
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //得到某个时间点的结果－－当前大圆所转的角度
                    mCurrentRotationAngle = (float) animation.getAnimatedValue();
                    //提醒View重新绘制－－onDraw
                    invalidate();
                }
            });
            //设置旋转次数无穷
            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAnimator.start();
        }
        @Override
        public void drawState(Canvas canvas) {
            //绘制旋转动画
            //绘制小圆的旋转动画
            //1.清空画板
            drawBackground(canvas);
            //2.绘制小圆
            drawCircle(canvas);
        }
        public void cancel() {
            mAnimator.cancel();
        }
    }

    /**
     * 聚合动画
     */
    private class MergingState extends SplashState {
        private ValueAnimator mAnimator;
        public MergingState() {
            //估值器，1200ms时间内计算某个时刻当前大圆的半径：r～0
            mAnimator = ValueAnimator.ofFloat(0,mRotationRadius);
            //加速器效果
            mAnimator.setInterpolator(new OvershootInterpolator(10));
            mAnimator.setDuration(mRotationDuration / 2);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //监听当前大圆的半径
                    mCurrentRotationRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mState = new ExpandingState();
                }
            });
            //反过来计算
            mAnimator.reverse();
        }
        @Override
        public void drawState(Canvas canvas) {
            //绘制聚合动画
            //绘制小圆
            //1.清空画板
            drawBackground(canvas);
            //2.绘制小圆
            drawCircle(canvas);
        }
    }

    /**
     * 扩散动画
     */
    private class ExpandingState extends SplashState {
        private ValueAnimator mAnimator;
        public ExpandingState() {
            //估值器，1200ms时间内计算某个时刻当前空心圆的半径：0～对角线的一半
            mAnimator = ValueAnimator.ofFloat(0,mDiagonalDist);
            mAnimator.setDuration(mRotationDuration / 2);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //监听当前空心圆的半径
                    mHoleRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.start();
        }
        @Override
        public void drawState(Canvas canvas) {
            //绘制扩散动画
            //绘制小圆
            //1.清空画板
            drawBackground(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        if(mHoleRadius > 0) {
            //绘制空心圆
            //技巧使用一个非常宽的画笔，不断减小画笔的宽度
            //设置画笔的宽度＝对角线/2 － 空心圆的半径
            float strokeWidth = mDiagonalDist - mHoleRadius;
            mPaintBackground.setStrokeWidth(strokeWidth);
            float radius = mHoleRadius + strokeWidth / 2;
            canvas.drawCircle(mCenterX,mCenterY,radius,mPaintBackground);
        } else {
            //清空画板
            canvas.drawColor(mSplashBgColor);
        }

    }

    private void drawCircle(Canvas canvas) {
        //绘制小圆

        //间隔角度
        float rotationAngle = (float)(2 * Math.PI/mCircleColors.length);
        for(int i = 0;i < mCircleColors.length;i++) {
            double a = mCurrentRotationAngle + rotationAngle * i;
            float cx = (float)(mCurrentRotationRadius * Math.cos(a) + mCenterX);
            float cy = (float)(mCurrentRotationRadius * Math.sin(a) + mCenterY);

            mPaint.setColor(mCircleColors[i]);
            canvas.drawCircle(cx,cy,mCircleRadius,mPaint);
        }
    }
}
