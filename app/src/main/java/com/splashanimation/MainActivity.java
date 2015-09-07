package com.splashanimation;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.splashanimation.widget.SplashView;

public class MainActivity extends AppCompatActivity {
    private FrameLayout mFrameLayout;
    private SplashView mSplashView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        //帧布局
        mFrameLayout = new FrameLayout(this);
        //先添加ContentView
        ContentView contentView = new ContentView(this);
        mFrameLayout.addView(contentView);
        //添加splashView
        mSplashView = new SplashView(this);
        mFrameLayout.addView(mSplashView);
        setContentView(mFrameLayout);
        startLoad();
    }

    Handler mHandler = new Handler();

    private void startLoad() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //数据加载完毕,执行后面的动画－－让ContentView显示出来
                mSplashView.splashAndDisappear();
            }
        },3000);
    }

}
