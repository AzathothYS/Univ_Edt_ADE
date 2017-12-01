package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by 7 on 10/11/2017.
 */

public class CustomNavigationView extends NavigationView {

    public CustomNavigationView(Context context) {
        super(context);
    }

    public CustomNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.d("Debug", "navigation view got the event and returned false!");
        return false;
    }

    @Override
    public  boolean dispatchTouchEvent(MotionEvent event) {
        Log.d("Debug", "dispatched nav view !");
        return true;
    }*/
}
