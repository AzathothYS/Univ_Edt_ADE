package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.a.univ_edt_ade.EdTFile.Event;
import com.a.univ_edt_ade.R;

import java.util.Locale;

/**
 * Custom view qui affiche les events, et gère les interactions avec eux
 */

public class ViewEvent extends View {

    // dimensions de l'EdTLayout
    public static int parentWidth, parentCornerHeight, parentHourSpacing;

    public int[] hours = {0, 0, 0, 0};
    public float length = 0;
    public float start = 0;
    public String day = "19700101"; // YYYY MM DD
    public String title = "none";
    public String salle = "none";
    public String[] desc;

    public String[] undisplayed;

    public boolean debug = false;

    public ViewEvent(Context context) {
        super(context);
    }

    public ViewEvent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewEvent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void fromEvent(Event event) {

        if (debug)
            Log.d("DebugViewEvent", "Setting event params of event '" + event.getTitre() + "' ...");

        this.day = event.getDay();
        this.title = event.getTitre();
        this.desc = event.getDesc().replace("\nPhysique", "").split("\n");
        this.undisplayed = new String[this.desc.length];
        this.salle = event.getSalle().replace(",", ", "); // on ajoute un espace après chaque virgule pour faciliter l'affichage
        this.setHours(event.getHours());

        if (debug)
            Log.d("DebugViewEvent", "start: " + start + " length:" + length);
    }

    /**
     * Définit les heures de l'event, vérifie si elles sont correctes et peuvent-être affichées
     * Définit les dimensions de l'event en fonction de l'heure de début et de fin de l'event
     * Ces dimentions ne sont pas en fonction des dimensions du parent
     */
    public void setHours(int[] hours) {

        if (debug)
            Log.d("DebugLength", "heures : " + String.format(Locale.FRANCE, "%02d:%02d - %02d:%02d", hours[0], hours[1], hours[2], hours[3]));

        if (hours[0] > hours[2] || (hours[0] == hours[2]) && (hours[1] > hours[3])) {
            if (debug)
                Log.d("DebugLength", "HEURES DE DEBUT ET DE FIN INVALIDES : " + hours[0] + ":" + hours[1] + " et " + hours[2] + ":" + hours[3]);
            return;
        }

        this.hours = hours;

        length = hours[2] - hours[0];
        // heure de début moins le min (8h)
        start = hours[0] - 8;

        if (start < 0) {
            if (debug)
                Log.e("ViewEvent", "ERROR : 'start' is under min display", new IllegalArgumentException("Starting hour of the event is under 8am"));
        }

        if (hours[1] % 5 != 0) {
            if (debug)
                Log.d("DebugLength", "invalid minStart: " + hours[1]);
            hours[1] -= hours[1] % 5;
            if (debug)
                Log.d("DebugLength", "transformed into: " + hours[1]);
        }

        if (hours[3] % 5 != 0) {
            if (debug)
                Log.d("DebugLength", "invalid minStop: " + hours[3]);
            hours[3] -= hours[3] % 5;
            if (debug)
                Log.d("DebugLength", "transformed into: " + hours[3]);
        }

        if (debug)
            Log.d("DebugLength", "Length : adding " + (hours[3] - hours[1]) / 5.f / 12.f + " to " + length);
        length += (hours[3] - hours[1]) / 5.f / 12.f;
        //length *= getResources().getDimension(R.dimen.hourSpacing);

        if (debug)
            Log.d("DebugLength", "Start : adding " + hours[1] / 5.f / 12.f + " to " + start);
        start += hours[1] / 5.f / 12.f;
        //start *= getResources().getDimension(R.dimen.hourSpacing);
        //start -= 3;

        if (debug)
            Log.d("DebugLength", "length : " + length + " - start : " + start);
    }

    public void setBackground() {
        setBackground(new EventBackground(this));
    }

    public String hoursToString() {
        return String.format(Locale.FRANCE, "%02d:%02d - %02d:%02d", hours[0], hours[1], hours[2], hours[3]);
    }

    /**
     * Fixe les dimensions en fonction des dimensions absolues données par 'setHours' et celles du parent
     */
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredDimension(this.getMinimumWidth(), this.getMinimumHeight());
    }

    /**
     * Retourne la position de l'event dans la layout
     */
    public int getStart() {
        return (int) (this.start * parentHourSpacing) + 3 + parentCornerHeight;
    }

    /**
     * Retourne la longueur de l'event dans la layout
     */
    public int getLength() {
        return (int) (this.length * parentHourSpacing);
    }

    @Override
    public int getMinimumHeight() {
        return getLength();
    }

    @Override
    public int getMinimumWidth() {
        return parentWidth;
    }
}
