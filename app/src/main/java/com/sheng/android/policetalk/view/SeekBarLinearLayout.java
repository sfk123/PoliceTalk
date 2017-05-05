package com.sheng.android.policetalk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2017/4/9.
 */

public class SeekBarLinearLayout extends LinearLayout {
    public SeekBarLinearLayout(Context context){
        super(context);
    }
    public SeekBarLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev){
        super.onTouchEvent(ev);
        return true;
    }
}
