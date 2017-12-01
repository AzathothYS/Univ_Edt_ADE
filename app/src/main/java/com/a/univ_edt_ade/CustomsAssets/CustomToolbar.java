package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by 7 on 09/11/2017.
 */

public class CustomToolbar extends Toolbar {

    public CustomToolbar(Context context) {
        super(context);
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //Log.d("Debug", "toolbar got the event and passed it.");
        return false;
    }
}
