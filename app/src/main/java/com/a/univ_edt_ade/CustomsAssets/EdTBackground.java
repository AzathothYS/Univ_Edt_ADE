package com.a.univ_edt_ade.CustomsAssets;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Background de l'EdT
 * Prend en paramètres les dimentions de l'EdT, plus les couleurs custom
 * Dessine manuellement les lignes séparant les heures et les jours + les heures sur les côtés
 */

public class EdTBackground extends Drawable {

    private final int daySpacing, hourSpacing, cornerWidth, cornerHeight;

    private Paint PaintDay, PaintHour = new Paint(), PaintText = new Paint();
    private int textSize;

    private final String[] days = {"lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"};
    private String[] dates = new String[7];

    public int dayToDisp = -1;
    private boolean printDates = true;

    public EdTBackground(int newDaySpacing, int newHourSpacing, int newCornerWidth, int newCornerHeight,
                         int hourLineColor, int dayLineColor, int hourFontColor, boolean printDates,
                         int dayToDisplay, @Nullable Calendar cal) {

        daySpacing = newDaySpacing;
        hourSpacing = newHourSpacing;
        cornerWidth = newCornerWidth;

        PaintHour = new Paint();
        PaintHour.setStrokeCap(Paint.Cap.ROUND);
        PaintHour.setAntiAlias(false);
        PaintHour.setStrokeWidth(5.f);

        PaintDay = new Paint(PaintHour);

        PaintHour.setColor(hourLineColor);
        PaintDay.setColor(dayLineColor);

        PaintText.setColor(hourFontColor);
        PaintText.setTypeface(Typeface.DEFAULT);
        PaintText.setTextAlign(Paint.Align.CENTER);
        PaintText.setTextSize(35.f);

        textSize = PaintText.getFontMetricsInt().bottom - PaintText.getFontMetricsInt().top;

        this.printDates = printDates;

        if (!printDates) {
            // on veut afficher l'EdT en mode landscape, donc pas de date et on divise par 2 le cornerHeight
            cornerHeight = newCornerHeight / 2;
        } else {
            cornerHeight = newCornerHeight;
            setDates(cal);
        }

        if (dayToDisplay >= 0 && dayToDisplay <= 6)
            dayToDisp = dayToDisplay;

        Log.d("EdTBackground", "init finished");
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int height = getBounds().height();
        int width = getBounds().width();

        int brush = cornerWidth;

        Log.d("EdTBackground", "starting draw : day=" + dayToDisp + " - printDates=" + printDates);

        if (dayToDisp == -1) {
            // on est en mode normal

            // 6 lignes séparant les jours + les noms des jours
            for (int i=0;i<6;i++) {
                brush += daySpacing;
                canvas.drawLine(brush, 0, brush, height, PaintDay);
                canvas.drawText(days[i], brush - daySpacing / 2 + i, textSize + 4, PaintText);
                if (printDates)
                    canvas.drawText(dates[i], brush - daySpacing / 2 + i, textSize * 2 + 4, PaintText);
            }
            canvas.drawText(days[6], brush + daySpacing / 2 + 6, textSize + 4, PaintText);
            if (printDates)
                canvas.drawText(dates[6], brush + daySpacing / 2 + 6, textSize * 2 + 4, PaintText);

            brush = cornerHeight;

        } else {
            // on ne veut afficher qu'un seul jour
            canvas.drawText(days[dayToDisp], width / 2, textSize + 4, PaintText);
            if (printDates)
                canvas.drawText(dates[dayToDisp], width / 2, textSize * 2 + 4, PaintText);
        }

        Log.d("EdTBackground", "done drawing dates");


        // 12 lignes indiquant les heures sur la longueur de l'EdT (de 8 à 19h)
        for (int i=0;i<12;i++) {
            canvas.drawLine(cornerWidth / 2 + 50, brush, width - cornerWidth / 2 - 50, brush, PaintHour);
            canvas.drawText(i + 8 + ":00", cornerWidth / 2, brush + 13, PaintText);
            canvas.drawText(i + 8 + ":00", width - cornerWidth / 2, brush + 13, PaintText);
            brush += hourSpacing;
        }

        Log.d("EdTBackground", "done drawing hours");


    }

    /**
     * Définit les dates à afficher, en fonction de la date donnée
     * Le format est en fonction de la locale
     */
    private void setDates(@Nullable Calendar cal) {

        if (cal == null) {
            cal = Calendar.getInstance();
        }

        cal.set(Calendar.DATE, cal.getFirstDayOfWeek());
        //DateFormat dateF = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat dateF = DateFormat.getDateInstance();

        for (int weekIndex = 0; weekIndex < 7; weekIndex++) {
            dates[weekIndex] = dateF.format(cal.getTime());
            cal.add(Calendar.DAY_OF_WEEK, 1);

            Log.d("setDates", "day n°" + weekIndex + " : " + dates[weekIndex]);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        PaintDay.setAlpha(alpha);
        PaintHour.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        PaintDay.setColorFilter(colorFilter);
        PaintHour.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
