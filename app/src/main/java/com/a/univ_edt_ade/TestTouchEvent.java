package com.a.univ_edt_ade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;

import com.a.univ_edt_ade.CustomsAssets.HScrollView;
import com.a.univ_edt_ade.CustomsAssets.VScrollView;

public class TestTouchEvent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_touch_event);

        HScrollView Hview = (HScrollView) findViewById(R.id.HviewT);
        VScrollView Vview = (VScrollView) findViewById(R.id.VviewT);
        GridView Gview = (GridView) findViewById(R.id.Gview);

        for (int i=0;i<5;i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.imagetestedt);
        }
    }
}
