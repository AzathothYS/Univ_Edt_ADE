package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.a.univ_edt_ade.R;

/**
 * Adapter pour la RecyclerList affichant l'arborescence d'ADE
 */

public class arboRListAdapter extends RecyclerView.Adapter<arboRListAdapter.ViewHolder> {

    private String[] noms;
    private int TextColor;

    public static Bitmap closedFolder = null;
    public static Bitmap openedFolder = null;

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

            canvas.drawBitmap(closedFolder, 2.f, 2.f, linePaint);
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

    public static void setDrawables(Context context, @DrawableRes int openedFolderID, @DrawableRes int closedFolderID) {
        openedFolder = getBitmapFromDrawable(context, openedFolderID);
        closedFolder = getBitmapFromDrawable(context, closedFolderID);
    }

    /**
     * From : https://stackoverflow.com/a/38635587/8662187
     */
    private static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
