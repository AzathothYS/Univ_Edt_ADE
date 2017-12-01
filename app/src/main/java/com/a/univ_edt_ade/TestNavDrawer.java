package com.a.univ_edt_ade;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.a.univ_edt_ade.CustomsAssets.EdTLayout;
import com.a.univ_edt_ade.CustomsAssets.HScrollView;
import com.a.univ_edt_ade.CustomsAssets.VScrollView;

public class TestNavDrawer extends AppCompatActivity {

    private EdTLayout EdT;
    private VScrollView Vview;
    private HScrollView Hview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_nav_drawer);

        Vview = (VScrollView) findViewById(R.id.EdTVview);
        Hview = (HScrollView) findViewById(R.id.EdTHview);

        // désactive les blocs bleus qui apparaissent lorsque l'on essaye de scroller plus loin que la limite
        Vview.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        Hview.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("EdTState")) {
                EdT = new EdTLayout(this, savedInstanceState.getBundle("EdTState"));
            }
        }

        if (EdT == null)
            EdT = new EdTLayout(this);

        EdT.setWillNotDraw(false);

        Hview.addView(EdT);

        EdT.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        EdT.Vview = Vview;
        EdT.Hview = Hview;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EdT.handleTouchEvents(event);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("EdTState", EdT.getState());
    }
}
