package com.a.univ_edt_ade.CustomsAssets;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;

import com.a.univ_edt_ade.EdTFile.Event;
import com.a.univ_edt_ade.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by 7 on 16/09/2017.
 */

public class EventBackground extends Drawable {

    private final ViewEvent event;

    private static final Paint normal = new Paint(), bold = new Paint(), small = new Paint(), rectBack = new Paint();
    private static int textLineSpacing = 0;

    private static final int canvasTextOutline = 8; // un offset qui permet au texte de ne pas se coller aux parois de l'event

    private static android.support.v4.util.ArrayMap<String, Integer> colorMap = new android.support.v4.util.ArrayMap<>();
    private LinkedHashMap<String, Paint> printables = null;

    public static boolean debug = false;

    static {
        normal.setTextSize(25.f);
        normal.setTypeface(Typeface.DEFAULT);
        normal.setTextAlign(Paint.Align.CENTER);

        bold.setTextSize(30.f);
        bold.setTypeface(Typeface.DEFAULT_BOLD);
        bold.setTextAlign(Paint.Align.CENTER);

        small.setTextSize(23.f);
        small.setTypeface(Typeface.DEFAULT);
        small.setTextAlign(Paint.Align.CENTER);

        rectBack.setStrokeWidth(5.f);

        textLineSpacing = -normal.getFontMetricsInt().top + normal.getFontMetricsInt().bottom;
    }

    public EventBackground(ViewEvent Event) {

        event = Event;

        // la couleur n'a pas été initialisée
        if ((int) normal.getColor() == -16777216) {
            normal.setColor(event.getResources().getColor(R.color.textEvent));
            bold.setColor(normal.getColor());
            small.setColor(normal.getColor());
            //bold.setColor(event.getResources().getColor(R.color.textEvent));
            //small.setColor(event.getResources().getColor(R.color.textEvent));
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        // bordures de l'event + couleur de fond
        RectF e = new RectF(0.f, 0.f, canvas.getWidth(), canvas.getHeight());
        rectBack.setColor(getBackColor(event, event.title));
        canvas.drawRoundRect(e, 25.f, 25.f, rectBack);


        // affichage du texte
        float brushX = canvas.getWidth() / 2;
        float brushY = textLineSpacing;

        String[] toPrint = {event.title, event.salle, event.hoursToString()};

        printables = new LinkedHashMap<>();

        if (debug)
            Log.d("DebugDraw", "initial brushY : " + brushY);

        Paint paintForAll;
        int i = 0;
        for (String stringToPrint : toPrint) {
            if (i == 0)
                paintForAll = new Paint(bold);
            else
                paintForAll = new Paint(normal);

            paintForAll = reduceText(stringToPrint, canvas.getWidth() - canvasTextOutline, paintForAll); // on réduit le texte jusqu'à qu'il passe entièrement dans le canvas

            if (paintForAll.getTextSize() <= small.getTextSize()) {
                // le texte est trop petit
                if (debug)
                    Log.d("DebugDraw", "paint too small! Cutting string : " + stringToPrint);
                paintForAll.setTextSize(i == 0 ? normal.getTextSize() : small.getTextSize()); // bold >> normal  et  normal >> small
                if (debug)
                    Log.d("DebugDraw", "minimized text size to " + paintForAll.getTextSize());
                stringToPrint = sliceTextForDisplay(stringToPrint, paintForAll, canvas.getWidth() - canvasTextOutline); // on passe des lignes pour le faire rentrer
            }

            for (String toAdd : stringToPrint.split("\n")) {
                if (debug)
                    Log.d("DebugDraw", "Current Y : " + brushY + " for max height : " + canvas.getHeight());
                if (brushY > canvas.getHeight()) {
                    if (debug)
                        Log.d("DebugDraw", "brushY exceeded canvas height : " + brushY + " > " + canvas.getHeight() + " with textLineSpacing : " + getFontSize(paintForAll));
                    break; // il n'y a plus de place dans le canvas
                }
                printables.put(toAdd, paintForAll);             // on ajoute le str à la liste à afficher
                if (debug)
                    Log.d("DebugDraw", "Added : '" + toAdd + "' to array with paint : " + paintForAll.getTextSize());
                toPrint[i] = toPrint[i].replace(toAdd, "");     // on supprime ce que l'on affiche
                if (debug)
                    Log.d("DebugDraw", "New processed String : '" + toPrint[i] + "'");
                brushY += getFontSize(paintForAll); // on ajoute la taille maximale de la police à la coord Y
            }

            i++;
            if (debug)
                Log.d("", "\n ");
        }

        if (debug)
            Log.d("", "\n\n ");

        // si il reste de la place pour la desc affichée en petit
        if (brushY + getFontSize(small) * event.desc.length < canvas.getHeight()) {
            if (debug)
                Log.d("DebugDraw", "Enough space for desc to be printed : " + (brushY + getFontSize(small) * event.desc.length) + " < " + canvas.getHeight());
            for (String descLine : event.desc) {
                if (debug)
                    Log.d("DebugDraw", "Added : '" + descLine + "' to printables");
                printables.put(descLine, small);
            }
        }
        else {
            if (debug)
                Log.d("DebugDraw", "Not enough space for the desc : " + (brushY + getFontSize(small) * event.desc.length) + " > " +  canvas.getHeight());
        }

        /*Log.d("", "\n\n");

        Log.d("DebugDraw", "Printables Values :");
        for (String str : printables.keySet()) {
            Log.d("DebugDraw", "        '" + str + "' paint : " + printables.get(str).getTextSize());
        }*/

        if (debug)
            Log.d("", "\n\n ");

        // on détermine la hauteur finale de ce qu'il y à a afficher
        brushY = textLineSpacing;
        for (Paint paint : printables.values()) {
            brushY += getFontSize(paint);
            //Log.d("DebugDraw", "top : " + paint.getFontMetrics().top + "  -  bottom : " + paint.getFontMetrics().bottom);
        }

        if (debug)
            Log.d("DebugDraw", "Hauteur de ce qu'il y a à afficher : " + brushY);

        if (brushY > canvas.getHeight()) {
            if (debug)
                Log.d("DebugDraw", "WWWHAAAAAAAAAAT??!! brushY =" + brushY + " > " + canvas.getHeight());
        }

        // on se place à l'endroit où lequel le texte à afficher sera centré dans le canvas, plus l'offset fixe
        brushY = (canvas.getHeight() - brushY) / 2.f + textLineSpacing;

        if (debug)
            Log.d("DebugDraw", "brushY initial : " + brushY);

        if (debug)
            Log.d("", "\n\n ");

        for (String str : printables.keySet()) {
            if (debug)
                Log.d("DebugDraw", "Drawing : '" + str + "' with paint : " + printables.get(str).getTextSize());
            canvas.drawText(str, brushX, brushY, printables.get(str));
            brushY += getFontSize(printables.get(str)); // on se déplace de la hauteur de la paint utilisée
        }

        if (debug)
            Log.d("", "\n\n ");

        if (debug)
            Log.d("DebugDraw", "final brushY = " + brushY);

        printables = null;

        if (debug)
            Log.d("DebugDraw", "Leftovers of toPrint : ");
        for (String str : toPrint) {
            if (debug)
                Log.d("DebugDraw", "        '" + str + "'");
        }


        if (debug)
            Log.d("", "\n\n-------------------------------------------------------------------------------------------------------\n\n ");


        /*
        Log.d("DebugDraw", "Drawing event : " + event.title + " at " + event.hoursToString() + " in canvas : " + canvas.getWidth() + " " + canvas.getHeight());

        Log.d("DebugDraw", "EventBeckground params : " + "\ntextLineSpacing :" + textLineSpacing);

        canvas.drawText(event.title, brushX, brushY, reduceText(event.title, canvas.getWidth(), bold));
        brushY += textLineSpacing;
        canvas.drawText(event.salle, brushX, brushY, reduceText(event.salle, canvas.getWidth(), normal));
        brushY += textLineSpacing;
        canvas.drawText(event.hoursToString(), brushX, brushY, normal);
        brushY += textLineSpacing;*/

        /*
        for (String printText : sliceTextForDisplay(event.title, bold, canvas.getWidth())) {
            canvas.drawText(printText, brushX, brushY, bold);
            brushY += textLineSpacing;
        }
        for (String printText : sliceTextForDisplay(event.salle, normal, canvas.getWidth())) {
            canvas.drawText(printText, brushX, brushY, normal);
            brushY += textLineSpacing;
        }
        canvas.drawText(event.hoursToString(), brushX, brushY, normal);
        brushY += textLineSpacing;

        if (brushY + textLineSpacing < canvas.getHeight()) {
            int indexUnprinted = 0;

            for (String descLine : event.desc) {

                String[] descPrint = sliceTextForDisplay(descLine, small, canvas.getWidth());

                if (brushY + textLineSpacing * descPrint.length < canvas.getHeight()) {
                    for (String printText : descPrint) {
                        canvas.drawText(printText, brushX, brushY, small);
                        brushY += textLineSpacing;
                    }
                    Log.d("DebugDraw", "finished printing : " + descLine + " - current brushY = " + brushY);
                } else {
                    canvas.drawText("...", brushX, brushY, normal);
                    Log.d("DebugDraw", "desc has too little space to be drawn, part : " + descLine);
                    unprintedDescLines[indexUnprinted++] = descLine;
                }
            }
        }
        else {
            unprintedDescLines = event.desc;
            canvas.drawText("...", brushX, brushY, normal);
            Log.d("DebugDraw", "The entire desc didn't had enough space to be printed!");
        }

        event.undisplayed = unprintedDescLines;

        Log.d("DebugDraw", "final brush : x=" + brushX + " y=" + brushY);
        */
    }

    private float getFontSize(Paint paint) {
        return paint.getFontMetrics().bottom - paint.getFontMetrics().top; // pour une raison inconnue, bottom est positif et top est négatif
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int i) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @NonNull
    private static String sliceTextForDisplay(String text, Paint paint, int maxWidth) {
        int index = 0;
        int tempIndex;
        int lastIndex = 0;

        if (text.endsWith(" ")) // il y a un risque d'avoir un index trop grand sinon
            text.substring(0, text.length() -2);

        if (debug)
            Log.d("sliceTextDisplay", "started processing : " + text + "\n\n");

        int protec = 0;
        while (true) {

            //Log.d("sliceTextDisplay", "Processing : '" + text.substring(index) + "' with index : " + index);
            tempIndex = paint.breakText(text.substring(index), true, maxWidth, null);
            //Log.d("sliceTextDisplay", "New tempIndex : " + tempIndex + " - index = " + index +" - lastIndex = " + lastIndex);
            //Log.d("sliceTextDisplay", "Min substring : " + text.substring(tempIndex));

            if (tempIndex + index == text.length()) {
                //Log.d("sliceTextDisplay", "index = substring.length : " + tempIndex + " = " + text.substring(index).length());
                break;
            }

            index = lastIndex + text.substring(lastIndex, tempIndex + index).lastIndexOf(" ");
            //Log.d("sliceTextDisplay", "new index : " + index);

            if (index < lastIndex) { // index - lastIndex = -1, donc il n'y avait pas l'espace dans le nouveau substring
                index = tempIndex;   // on coupe d'un caractère qui n'est pas un espace
                text = text.substring(0, index) + "\n" + text.substring(index);
                //Log.d("sliceTextDisplay", "index negative, cutting at tempIndex :" + index);
            }
            else {
                //Log.d("sliceTextDisplay", "Final index = " + index + " - New working substring : '" + text.substring(index) + "'");
                text = text.substring(0, index) + "\n" + text.substring(index + 1);
            }
            //Log.d("sliceTextDisplay", "New text : \n" + text + "\n");ZZZZZZZZZZZ
            index++; // on passe le nouveau '\n' et l'espace qui est juste après
            lastIndex = index;

            if (protec++ > 20) {
                Log.e("sliceTextDisplay", "ERROR : unable to process text : " + text + " with paint " + paint.getFontMetricsInt() + " and maxWidth : " + maxWidth, new NullPointerException("Error during text display parsing"));
                break;
            }
        }

        return text;
    }

    private Paint reduceText(String toPrint, int maxWidth, Paint paint) {
        Paint smallerPaint = new Paint(paint);

        if (debug)
            Log.d("reduceText", "Asked to reduce : '" + toPrint + "' with max width : " + maxWidth + " and current paint size : " + paint.getTextSize());

        while (smallerPaint.breakText(toPrint, true, maxWidth, null) <= toPrint.length() -1) {
            smallerPaint.setTextSize(smallerPaint.getTextSize() -.25f);

            if (smallerPaint.getTextSize() <= small.getTextSize()) {
                if (debug)
                    Log.d("reduceText" , "Too small! Returning a size of " + small.getTextSize());
                break;
            }
        }
        if (debug)
            Log.d("reduceText", "Final size : " + smallerPaint.getTextSize());

        return smallerPaint;
    }

    private static int getBackColor(ViewEvent event, String title) {

        String key = findSimilarKey(title);

        //Log.d("DebugArrayColor", "getBackColor for " + title + " : do contains : " + colorMap.containsKey(title));
        if (!colorMap.containsKey(key)) {
            //Log.d("DebugArrayColor", "colorMap size : " + (colorMap.size() < 8));
            if (colorMap.size() < 8) {
                colorMap.put(title, Color.parseColor(event.getResources().getStringArray(R.array.eventColorSet1)[colorMap.size()]));
            } else {
                colorMap.put(title, Color.parseColor(event.getResources().getStringArray(R.array.eventColorSet1)[(int) (Math.random() * 7)])
                                    + (int) (Math.random() * 256));
            }
            //Log.d("DebugArrayColor", "new value :" + colorMap.valueAt(colorMap.size() -1) + " for :" + colorMap.keyAt(colorMap.size() -1));
        }
        //Log.d("DebugArrayColor", "Color for : '" + title + "' -> " + colorMap.get(title));
        return colorMap.get(key); // 'title' si aucune key similaire, sinon la key similaire
    }

    private static String findSimilarKey(String str) {
        int i;
        int unsimilarities;
        for (String key : colorMap.keySet()) {
            unsimilarities = 0;
            i = 0;
            for (char c : key.toCharArray()) {
                if (str.charAt(i) != c)
                    unsimilarities++;

                if (unsimilarities > 5)
                    break;

                i++;
            }

            if (unsimilarities < 5) {
                if (debug)
                    Log.d("DebugSimilarKey", "str : '" + str + "' matched with : '" + key + "'");
                return key;
            }
        }
        return str;
    }
}
