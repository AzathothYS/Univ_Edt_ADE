package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.icu.text.LocaleDisplayNames;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Voir VScrollView
 */

public class HScrollView extends HorizontalScrollView {

    public HScrollView(Context context, AttributeSet attrs, int defStyle) {super(context, attrs, defStyle);}

    public HScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HScrollView(Context context) {
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
