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

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Drawable derrière chaque Event
 * Affiche le texte et définit la couleur de fond
 */

public class EventBackground extends Drawable {

    public static boolean modeLandscape = false;    // change l'affichage du texte si on est en mode landscape

    private final ViewEvent event;

    private static final Paint normal = new Paint(), bold = new Paint(), small = new Paint(), smallTitle = new Paint(), rectBack = new Paint();

    private static final int canvasTextOutline = 8; // un offset qui permet au texte de ne pas se coller aux parois de l'event

    private static final int boldHeight, normalHeight, smallHeight;

    private static android.support.v4.util.ArrayMap<String, Integer> colorMap = new android.support.v4.util.ArrayMap<>();

    static {
        normal.setTextSize(25.f);
        normal.setTypeface(Typeface.DEFAULT);
        normal.setTextAlign(Paint.Align.CENTER);
        normal.setAntiAlias(true);

        bold.setTextSize(30.f);
        bold.setTypeface(Typeface.DEFAULT_BOLD);
        bold.setTextAlign(Paint.Align.CENTER);
        bold.setAntiAlias(true);

        small.setTextSize(21.f);
        small.setTypeface(Typeface.DEFAULT);
        small.setTextAlign(Paint.Align.CENTER);
        small.setAntiAlias(true);

        smallTitle.setTextSize(22.f);
        smallTitle.setTypeface(Typeface.DEFAULT_BOLD);
        smallTitle.setTextAlign(Paint.Align.CENTER);
        smallTitle.setAntiAlias(true);

        rectBack.setStrokeWidth(5.f);
        rectBack.setAntiAlias(true);

        // on définit la taille des polices, le '7' est utilisé car c'est un grand caractère, qui
        // est au moins aussi grand que tout les caractères que l'on va afficher, et le 'q' car
        // c'est le plpus bas
        Rect fontMesurer = new Rect();

        bold.getTextBounds("q7", 0, 2, fontMesurer); // TODO : end = 1 -> 2, peut-être que j'ai tout fait foiré dans les offsets de merde
        boldHeight = fontMesurer.height();

        normal.getTextBounds("q7", 0, 2, fontMesurer);
        normalHeight = fontMesurer.height();

        small.getTextBounds("q7", 0, 2, fontMesurer);
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

    /**
     * crée le background de l'event :
     *  - définit la forme et couleur du fond
     *  - affiche en fonction de la place disponible dans le canvas toutes les infos de l'event
     *  - n'affiche que le titre en petit si on est en mode landscape
     */
    @Override
    public void draw(@NonNull Canvas canvas) {

        final int height = canvas.getHeight();
        final int width = canvas.getWidth();

        // bordures de l'event + couleur de fond
        RectF e = new RectF(0.f, 0.f, width, height);
        rectBack.setColor(getBackColor(event, event.title));
        canvas.drawRoundRect(e, 25.f, 25.f, rectBack);

        // si on est en mode landscape, on affiche tout ici et on ignore le reste
        if (modeLandscape) {
            // on affiche seulement le titre, dans une police de taille adaptée, le tout centré dans le canvas
            // d'abord on détermine le nombre de lignes que l'on a avec la police 'smallTitle'
            // ensuite si on a déjà suffisament de place on affiche le titre, sinon on le coupe pour le
            // répartir sur plusieurs lignes, et si ce n'est toujours pas assez, on réduit encore la taille de 'smallTitle'

            Rect fontMesurer = new Rect();
            smallTitle.getTextBounds("q7", 0, 2, fontMesurer);
            int smallTitleHeight = fontMesurer.height();

            int nbOfPossibleLines = (int) ((height - 2 * canvasTextOutline) / (smallTitleHeight * 1.1f));

            String[] slicedText;

            if (nbOfPossibleLines >= 1) {

                float currentFontSize = smallTitle.getTextSize();

                while (true) {
                    slicedText = sliceText(event.title, smallTitle, width - 2 * canvasTextOutline);

                    if (slicedText.length <= nbOfPossibleLines)   // on a réussi à faire rentrer le texte dans le canvas
                        break;
                    else if (currentFontSize < 18.f)  // on s'arrête pour ne pas obtenir un texte illisible
                        break;
                    else {
                        // on réessaye avec la même police mais en plus petit
                        smallTitle.setTextSize(currentFontSize -= .5f);
                        smallTitle.getTextBounds("q7", 0, 2, fontMesurer);
                        smallTitleHeight = fontMesurer.height();

                        // on met à jour le nombre de lignes disponibles
                        nbOfPossibleLines = (int) ((height - 2 * canvasTextOutline) / (smallTitleHeight * 1.1f));
                    }
                }

                if (currentFontSize < 18.f) {
                    // on n'a pas réussi à avoir tout le texte dans l'event, on doit donc le couper

                    // on "coupe" 'linesToPrint' pour avoir autant d'éléments que de lignes possibles
                    for (int i=slicedText.length - 1;i>nbOfPossibleLines;i--)
                        slicedText[i] = null;

                    // on rajoute '...' soit sur la dernière ligne, soit à la fin de la seule ligne
                    if (nbOfPossibleLines > 1 && slicedText.length > 1) {
                        slicedText[slicedText.length -1] = "...";

                    } else {
                        // on remplace les 3 derniers caractères par '...'
                        // on suppose que '...' prend moins de place que les 3 caractères remplacés
                        slicedText[0] = slicedText[0].substring(0, slicedText[0].length() - 3) + "...";
                    }
                }

            } else {
                // on prend que ce qui peut tenir sur la seule ligne et on remplace les 3 derniers caractères par '...'
                slicedText = sliceText(event.title, smallTitle, width - 2 * canvasTextOutline);
                slicedText[0] = slicedText[0].substring(0, slicedText[0].length() - 3) + "...";

                for (int i=1;i>slicedText.length;i--)
                    slicedText[i] = null;
            }

            float brushY = (height - smallTitleHeight * 1.1f * slicedText.length - canvasTextOutline) / 2 + smallTitleHeight;
            final float brushX = width / 2;

            // on affiche les lignes en les centrant :
            for (String line : slicedText) {
                if (line == null)
                    break; // on a terminé

                canvas.drawText(line, brushX, brushY, smallTitle);
                brushY += smallTitleHeight + 1.1f;
            }

            return;
        }


        // on affiche l'event en mode normal ou dayDisp

        // texte dans l'event (titre en gras, salle et horaires en police normale, description en petit, si il y a de la place

        if (height < boldHeight) {
            // on ne peut pas même pas afficher le titre
            // TODO : non testé

            Rect textMesurer = new Rect();
            small.getTextBounds("...", 0, 3, textMesurer);

            if (textMesurer.height() * 2 < height) {
                canvas.drawText("...", height - canvasTextOutline / 2, width / 2, small);
            } else {
                Log.d("EventBackground", "Event '" + event.title + "' is too small to print anything!");
            }

            return;
        }

        LinkedHashMap<String, Integer> linesToPrint = new LinkedHashMap<>();
        // ligne , ID de la painture utilisée pour la ligne
        // 1 : bold, 2 : normal, 3 : small

        // on se positionne en haut au milieu du canvas, avec l'offset en Y
        float totalTextHeight = canvasTextOutline * 2;
        final int maxWidth = width - 4 * canvasTextOutline;

        String[] textInLines;

        // on affiche le titre
        textInLines = sliceText(event.title, bold, maxWidth);
        for (String line : textInLines) {
            linesToPrint.put(line, 1);
            totalTextHeight += boldHeight * 1.2;
        }

        if (totalTextHeight > height)
            return;  // rien ne sert de continuer, car il n'y a déjà plus de place dans l'event

        // on affiche l'heure, en supposant qu'il y a toujours suffisament de place
        linesToPrint.put(event.hoursToString(), 2);
        totalTextHeight += normalHeight * 1.2;

        if (totalTextHeight > height)
            return;

        // on affiche la salle de l'event
        textInLines = sliceText(event.salle, normal, maxWidth);
        for (String line : textInLines) {
            linesToPrint.put(line, 2);
            totalTextHeight += normalHeight * 1.2;
        }

        // on ajoute la description si il reste de la place, et si elle n'est pas vide
        if (totalTextHeight < height - smallHeight * 1.2 * event.desc.length && event.desc.length > 0) {

            for (String text : event.desc) {
                textInLines = sliceText(text, small, maxWidth);
                for (String line : textInLines) {
                    linesToPrint.put(line, 3);
                    totalTextHeight += smallHeight * 1.2;
                }
            }
        }


        // on affiche le texte en le centrant
        float brushY = (height - totalTextHeight - 2 * canvasTextOutline) / 2 + canvasTextOutline + boldHeight;  // position de la 1ere ligne lorsque l'on centre le texte dans l'event
        final float brushX = width / 2;

        for (String line : linesToPrint.keySet()) {
            switch (linesToPrint.get(line)) {
                case 1:
                    canvas.drawText(line, brushX, brushY, bold);
                    brushY += boldHeight * 1.2;
                    break;
                case 2:
                    canvas.drawText(line, brushX, brushY, normal);
                    brushY += normalHeight * 1.2;
                    break;
                case 3:
                    canvas.drawText(line, brushX, brushY, small);
                    brushY += smallHeight * 1.2;
                    break;
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
     * découpe 'text' en lignes plus petites que 'maxWidth' lorsque 'text' est affiché avec 'paint'
     */
    private String[] sliceText(String text, Paint paint, int maxWidth) {

        // le regex est équivalent à '\s+' : match tous les espaces, retour à la ligne, etc...
        // mais en plus on ignore ' - ' et on coupe après chaque virqule en incluant l'espace après si il y en a un
        // on ignore aussi les espaces placés avant des nombres (pour garder les n° et les noms des salle ensembles)
        String[] wordList = text.split("((?! [0-9])(?! - ) (?<! - )|[\n\r\t\f]|(?<=,) *)+");
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
                    processingWord.setLength(0);

                    do {
                        word = temp[1];

                        // on coupe le mot, fait une ligne
                        temp = sliceWord(word, paint, maxWidth);
                        lineList.add(temp[0]);

                        if (temp[1].isEmpty())
                            break;  // il n'y a plus d'excès, on a terminé

                        paint.getTextBounds(temp[1], 0, temp[1].length(), textMesurer);
                    } while (textMesurer.width() > maxWidth);

                    processingWord.append(temp[1]);

                } else {
                    // 'word' va avoir sa propre ligne

                    lineList.add(processingWord.toString());
                    processingWord.setLength(0); // on reset 'processingWord'
                    processingWord.append(word);
                }

            } else {
                // on rajoute 'word' à la ligne, sauf si le début d'une ligne
                if (processingWord.length() == 0)
                    processingWord.append(word);

                else
                    processingWord.append(" ").append(word);
            }

            if (++wordListIndex >= wordList.length) {
                // on est à la fin de la liste, on check si la dernière ligne à bien été mise dans la liste
                // LinkedList.getLast() crash si la liste est vide, d'où la 1ere condition
                if (lineList.size() == 0)
                    lineList.add(processingWord.toString());

                else if (!lineList.getLast().equals(processingWord.toString()))
                    lineList.add(processingWord.toString());
            }
        }

        return lineList.toArray(new String[lineList.size()]);   // on transforme 'lineList' en une array de String
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
                return key;
            }
        }
        return str;
    }
}
