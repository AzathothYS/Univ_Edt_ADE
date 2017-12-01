package com.a.univ_edt_ade;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.a.univ_edt_ade.CustomsAssets.DebugCustomLayout;

public class TestCustomLayout extends AppCompatActivity {

    private DebugCustomLayout debugLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_custom_layout);

        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.alalalala);

        debugLayout = new DebugCustomLayout(this);

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.imagetestedt);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(imageView.getWidth(), imageView.getHeight());

        debugLayout.addView(imageView);

        constraintLayout.addView(debugLayout);
    }
}
