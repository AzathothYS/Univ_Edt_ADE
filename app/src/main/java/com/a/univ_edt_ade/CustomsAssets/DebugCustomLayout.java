package com.a.univ_edt_ade.CustomsAssets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.a.univ_edt_ade.R;

/**
 * Created by 7 on 18/11/2017.
 */

public class DebugCustomLayout extends LinearLayout {

    private Paint paint = new Paint();

    public DebugCustomLayout(Context context) {
        super(context);
        init(context);
    }

    public DebugCustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DebugCustomLayout(Context context, AttributeSet attrs, int defTruc) {
        super(context, attrs, defTruc);
        init(context);
    }

    private void init(Context context) {

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(8, 8, 0, 0);

        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setLayoutParams(layoutParams);

        this.setBackgroundColor(0x85FF00FF);

        paint.setStrokeWidth(10.f);
        paint.setColor(Color.RED);

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.imagetestedt);

        this.addView(imageView);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        canvas.drawLine(canvas.getWidth(), 0, 0, canvas.getHeight(), paint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //setMeasuredDimension(500, 500);
        Point point = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(point);
        setMeasuredDimension(point.x, point.y);
    }
}
