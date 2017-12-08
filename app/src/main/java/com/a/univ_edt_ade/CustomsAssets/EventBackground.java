package com.a.univ_edt_ade.CustomsAssets;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.a.univ_edt_ade.R;

/**
 * Drawable derrière chaque Event
 * Affiche le texte et définit la couleur de fond
 */

public class EventBackground extends Drawable {

    private final ViewEvent event;

    private static final Paint normal = new Paint(), bold = new Paint(), small = new Paint(), rectBack = new Paint();

    private static final int canvasTextOutline = 8; // un offset qui permet au texte de ne pas se coller aux parois de l'event

    private static final int boldHeight, normalHeight, smallHeight;

    private static android.support.v4.util.ArrayMap<String, Integer> colorMap = new android.support.v4.util.ArrayMap<>();

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

        // on définit la taille des polices, le '7' est utilisé car c'est un grand caractère, qui
        // est au moins aussi grand que tout les caractères que l'on va afficher
        Rect fontMesurer = new Rect();

        bold.getTextBounds("7", 0, 1, fontMesurer);
        boldHeight = fontMesurer.height();

        normal.getTextBounds("7", 0, 1, fontMesurer);
        normalHeight = fontMesurer.height();

        small.getTextBounds("7", 0, 1, fontMesurer);
        smallHeight = fontMesurer.height();
    }

    public EventBackground(ViewEvent Event) {
        event = Event;

        // si la couleur n'a pas été initialisée
        if ((int) normal.getColor() == -16777216) {
            normal.setColor(event.getResources().getColor(R.color.textEvent));
            bold.setColor(normal.getColor());
            small.setColor(normal.getColor());
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        // TODO: refaire tout ça mais de manière plus optimisée et stable... (déterminer le nombre de lignes dont on dispose dans le canvas pour simplifier sliceTextForDisplay)

        final int height = canvas.getHeight();
        final int width = canvas.getWidth();

        // bordures de l'event + couleur de fond
        RectF e = new RectF(0.f, 0.f, width, height);
        rectBack.setColor(getBackColor(event, event.title));
        canvas.drawRoundRect(e, 25.f, 25.f, rectBack);

        // texte dans l'event (titre en gras, salle et horaires en police normale, description en petit, si il y a de la place

        if (height < boldHeight) {
            // on ne peut pas afficher le titre

            // TODO, genre afficher '...' seulement, si il a de la place

            Rect textMesurer = new Rect();
            small.getTextBounds("...", 0, 3, textMesurer);

            if (textMesurer.height() * 2 < height) {
                canvas.drawText("...", height - canvasTextOutline / 2, width / 2, small);
            } else {
                Log.d("EventBackground", "Event '" + event.title + "' is too small! ");
            }
        }

        // on se positionne en haut au milieu du canvas, avec l'offset en Y
        float brushY = canvasTextOutline / 2;
        final float brushX = width / 2;

        String[] toPrint = {event.title, event.salle, event.hoursToString()};

        boolean isTitle = true;
        for (String text : toPrint) {

            // tant que l'on n'a pas mis tout texte en lignes, on continue.
            String[] temp;
            do {
                if (isTitle) {
                    temp = sliceWord(text, bold, width - canvasTextOutline);
                    canvas.drawText(temp[0], brushX, brushY, bold);
                    brushY += boldHeight;
                } else {
                    temp = sliceWord(text, normal, width - canvasTextOutline);
                    canvas.drawText(temp[0], brushX, brushY, normal);
                    brushY += normalHeight;
                }

                text = temp[1];
            } while (text != "");    // tant qu'il y a de l'excès

            if (brushY > height)
                break;  // rien ne sert de continuer, car il n'y a déjà plus de place dans l'event

            if (isTitle)
                isTitle = false; // seule la 1ère itération contient le titre
        }

        // on ajoute la description si il reste de la place
        if (brushY < height - smallHeight * event.desc.length && event.desc.length > 0) {
            for (String text : event.desc) {

                String[] temp;
                do {
                    temp = sliceWord(text, small, width - canvasTextOutline);
                    canvas.drawText(temp[0], brushX, brushY, small);
                    brushY += smallHeight;

                    text = temp[1];
                } while (text != "");
            }
        }
    }


    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int i) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }


    /**
     * retourne 'word' coupé pour que sa longueur lorque affiché par 'paint" soit plus petite que
     * 'maxwidth' - 'canvasTextOutline'
     */
    private String[] sliceWord(String word, Paint paint, int maxWidth) {

        String excess = "";
        int wordLength = word.length();

        Rect textMesurer = new Rect();
        paint.getTextBounds(word, 0, wordLength, textMesurer);

        while (textMesurer.width() > maxWidth - canvasTextOutline) {
            // tant que le mot est trop long, on lui enlève un caractère, que l'on rajpoute à l'excès
            excess += word.charAt(wordLength - 1);
            word = word.substring(0, --wordLength);

            paint.getTextBounds(word, 0, wordLength, textMesurer);
        }

        return new String[]{word, excess};
    }


    /**
     * Retourne la couleur de l'event en fonction de son titre, via 'colorMap'
     * Retourne la même couleur pour 2 events différents si leur titres sont suffisament proches
     * ex: 'CM Maths' et 'TD Maths' auront la même couleur
     */
    private static int getBackColor(ViewEvent event, String title) {

        String key = findSimilarKey(title);

        if (!colorMap.containsKey(key)) {
            if (colorMap.size() < 8) {
                colorMap.put(title, Color.parseColor(event.getResources().getStringArray(R.array.eventColorSet1)[colorMap.size()]));
            } else {
                colorMap.put(title, Color.parseColor(event.getResources().getStringArray(R.array.eventColorSet1)[(int) (Math.random() * 7)])
                                    + (int) (Math.random() * 256));
            }
        }
        return colorMap.get(key); // 'title' si aucune key similaire, sinon la key similaire
    }

    /**
     * Retourne la couleur stockée dans 'colorMap' correspondant à la clé 'str' fournie, sinon
     * retourne 'str'.
     * Retourne la même couleur si 2 clés ont moins de 5 caractères différents
     */
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
