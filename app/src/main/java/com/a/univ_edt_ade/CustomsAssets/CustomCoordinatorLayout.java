package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by 7 on 09/11/2017.
 */

public class CustomCoordinatorLayout extends CoordinatorLayout {

    public CustomCoordinatorLayout(Context context){
        super(context);
    }

    public CustomCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //Log.d("Debug", "customCoordLayout caught the touch event and passed it.");
        return false;
    }
}
