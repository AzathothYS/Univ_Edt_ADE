package com.a.univ_edt_ade;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Adapter pour la RecyclerList affichant l'arborescence d'ADE
 */

public class arboRListAdapter extends RecyclerView.Adapter<arboRListAdapter.ViewHolder> {

    private String[] noms;
    private int TextColor;

    public arboRListAdapter(String[] str, int textColor) {
        noms = str;
        TextColor = textColor;
    }

    @Override
    public arboRListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view_arbo_r_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        holder.textView.setBackground(new TextRListBackground());
        holder.textView.setText(noms[pos]);
    }

    @Override
    public int getItemCount() {
        return noms.length;
    }


    public class TextRListBackground extends Drawable {
        private Paint linePaint;

        public TextRListBackground() {
            linePaint = new Paint();
            linePaint.setColor(TextColor);
            linePaint.setStrokeCap(Paint.Cap.ROUND);
            linePaint.setAntiAlias(false);
            linePaint.setStrokeWidth(5.f);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawLine(1, 8, 1, canvas.getHeight() - 8, linePaint);
        }

        @Override
        public void setAlpha(int i) {}

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            linePaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;

        public ViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }
}
