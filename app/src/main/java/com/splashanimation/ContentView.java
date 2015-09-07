package com.splashanimation;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by wangzhiguo on 15/9/7.
 */
public class ContentView extends ImageView {
    public ContentView(Context context) {
        super(context);
        //主界面
        setScaleType(ScaleType.FIT_XY);
        setImageResource(R.drawable.girl_img);
    }
}
