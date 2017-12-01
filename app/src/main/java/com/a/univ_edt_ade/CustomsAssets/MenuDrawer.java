package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.a.univ_edt_ade.R;

/**
 * Custom DrawerLayout pour permettre à Edt_display de recevoir les touchEvents
 */

public class MenuDrawer extends DrawerLayout {

    public MenuDrawer(Context context) {
        super(context);
    }

    public MenuDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if(event.getX() > 30 && event.getAction() == MotionEvent.ACTION_DOWN){
            return isDrawerVisible(Gravity.START);
        }

        return true;
    }
}
