package com.a.univ_edt_ade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.a.univ_edt_ade.CustomsAssets.HScrollView;
import com.a.univ_edt_ade.CustomsAssets.VScrollView;

public class TestNestedScrollView extends AppCompatActivity {

    private HorizontalScrollView Hview;
    private ScrollView Vview;

    private float currentX, currentY, mx, my;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_nested_scroll_view);

        Hview = (HScrollView) findViewById(R.id.Hview);
        Vview = (VScrollView) findViewById(R.id.Vview);

        //Hview.requestDisallowInterceptTouchEvent(true);
        //Vview.requestDisallowInterceptTouchEvent(true);

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("Activity", "Touched");

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                mx = event.getX();
                my = event.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                currentX = event.getX();
                currentY = event.getY();

                Hview.scrollBy((int) (mx - currentX), (int) (my - currentY));
                Vview.scrollBy((int) (mx - currentX), (int) (my - currentY));

                mx = currentX;
                my = currentY;
                break;
            }
            case MotionEvent.ACTION_UP: {
                currentX = event.getX();
                currentY = event.getY();
                Vview.scrollBy((int) (mx - currentX), (int) (my - currentY));
                Hview.scrollBy((int) (mx - currentX), (int) (my - currentY));
                break;
            }
        }

        return true;
    }
}
