package com.a.univ_edt_ade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class TestGridView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_grid_view);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.RLayout);
        RelativeLayout relativeLayout2 = (RelativeLayout) findViewById(R.id.RLayout2);

        Button testButton = new Button(this);
        testButton.setText("test");

        ViewGroup.MarginLayoutParams MLP = new ViewGroup.MarginLayoutParams((int) getResources().getDimension(R.dimen.daySpacing), testButton.getMinHeight());
        MLP.setMargins(0, 50, 0, 0);

        RelativeLayout.LayoutParams RLP = new RelativeLayout.LayoutParams(MLP);

        relativeLayout.addView(testButton, RLP);

        MLP.setMargins(0, 200, 0, 0);
        RLP = new RelativeLayout.LayoutParams(MLP);
        testButton = new Button(this);
        testButton.setText("test2");
        relativeLayout2.addView(testButton, RLP);
    }
}
