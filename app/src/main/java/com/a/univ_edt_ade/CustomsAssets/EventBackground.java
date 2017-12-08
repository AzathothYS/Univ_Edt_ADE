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

import java.util.LinkedList;
import java.util.Objects;

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
        // est au moins aussi grand que tout les caractères que l'on va afficher, et le 'q' car
        // c'est le plpus bas
        Rect fontMesurer = new Rect();

        bold.getTextBounds("q7", 0, 1, fontMesurer);
        boldHeight = fontMesurer.height();

        normal.getTextBounds("q7", 0, 1, fontMesurer);
        normalHeight = fontMesurer.height();

        small.getTextBounds("q7", 0, 1, fontMesurer);
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
        float brushY = canvasTextOutline + boldHeight;
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
                    brushY += boldHeight * 1.5; // le *1.5 est là pour mettre de l'espace entre les lignes
                } else {
                    temp = sliceWord(text, normal, width - canvasTextOutline);
                    canvas.drawText(temp[0], brushX, brushY, normal);
                    brushY += normalHeight * 1.5;
                }

                text = temp[1];
            } while (!text.equals(""));    // tant qu'il y a de l'excès

            if (brushY > height)
                break;  // rien ne sert de continuer, car il n'y a déjà plus de place dans l'event

            if (isTitle)
                isTitle = false; // seule la 1ère itération contient le titre
        }

        // on ajoute la description si il reste de la place
        if (brushY < height - smallHeight * 1.5 * event.desc.length && event.desc.length > 0) {
            for (String text : event.desc) {

                String[] temp;
                do {
                    temp = sliceWord(text, small, width - canvasTextOutline);
                    canvas.drawText(temp[0], brushX, brushY, small);
                    brushY += smallHeight * 1.5;

                    text = temp[1];
                } while (!text.isEmpty()); // TODO: checker si tout va bien: text.equals("") --> text.isEmpty()
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

        StringBuilder excess = new StringBuilder("");
        int wordLength = word.length();

        Rect textMesurer = new Rect();
        paint.getTextBounds(word, 0, wordLength, textMesurer);

        while (textMesurer.width() > maxWidth - canvasTextOutline) {
            // tant que le mot est trop long, on lui enlève un caractère, que l'on rajpoute à l'excès
            excess.insert(0, word.charAt(wordLength - 1));
            word = word.substring(0, --wordLength);

            paint.getTextBounds(word, 0, wordLength, textMesurer);
        }

        return new String[]{word, new String(excess)};
    }

    private String[] sliceText(String text, Paint paint, int maxWidth) {

        // TODO: implémenter cette fonction + skip l'heure, inutile car toujours suffisament petite?

        // équivalent à '\s+' : match tous les espaces, retour à la ligne, etc... mais en plus on ignore ' - '
        String[] wordList = text.split("(?:(?:[\r\n\t\f])|(?! - ) (?<! - ))+");
        LinkedList<String> lineList = new LinkedList<>();

        int wordListIndex = 0;
        Rect textMesurer = new Rect();
        StringBuilder processingWord = new StringBuilder();
        for (String word : wordList) {

            paint.getTextBounds(processingWord.toString() + " " + word, 0, processingWord.length() + word.length(), textMesurer);

            if (textMesurer.width() > maxWidth) {
                // la ligne serait trop grande, on doit donc : soit mettre 'word' sur une nouvelle ligne
                // soit si word est plus grand qu'une ligne, le couper et mettre un peu sur la ligne précédante
                // et créer de nouvelles lignes où mettre 'word' jusqu'à que tout ses caractères soient sur une ligne.

                paint.getTextBounds(word, 0, word.length(), textMesurer);

                if (textMesurer.width() > maxWidth) {
                    // 'word' ne peu pas tenir sur une seule ligne
                    // on le coupe et on met se que l'on peut sur la place qu'il reste dans la ligne précédante

                    String[] temp = sliceWord(processingWord.toString() + " " + word, paint, maxWidth);
                    lineList.add(temp[0]);

                    do {
                        word = temp[1];

                        // on coupe le mot, fait une ligne
                        temp = sliceWord(word, paint, maxWidth);
                        lineList.add(temp[0]);

                        if (temp[1].isEmpty())
                            break;  // il n'y a pas d'excès, on a terminé

                        paint.getTextBounds(temp[1], 0, temp[1].length(), textMesurer);
                    } while (textMesurer.width() > maxWidth);

                    processingWord.setLength(0);
                    processingWord.append(temp[1]);

                } else {
                    // 'word' va avoir sa propre ligne

                    lineList.add(processingWord.toString());
                    processingWord.setLength(0); // on reset 'processingWord'
                    processingWord.append(word);
                }

            } else
                processingWord.append(" ").append(word); // on rajoute 'word' à la ligne

            if (wordListIndex++ >= wordList.length) {
                // on est à la fin de la liste, on check si la dernière ligne à bien été mise dans la liste
                if (!lineList.getLast().equals(processingWord.toString()))
                    lineList.add(processingWord.toString());
            }
        }

        return lineList.toArray(new String[lineList.size()]);   // on transforme 'lineList' en une array de String
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
