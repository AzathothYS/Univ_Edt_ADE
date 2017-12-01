package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Custom ScrollView qui, combinée à HScrollView, permet de se déplacer en haut et en bas en même
 * temps, car c'est le child qui va gérer tous les touchEvent et donc leurs mouvements.
 */

public class VScrollView extends ScrollView {

    public VScrollView(Context context, AttributeSet attrs, int defStyle) {super(context, attrs, defStyle);}

    public VScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VScrollView(Context context) {
        super(context);
    }

    /**
     * on enlève la fonctionnalité de la HorizontalScrollView, pour traiter l'event depuis l'EdT directement
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
