package com.a.univ_edt_ade.EdTFile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by 7 on 16/09/2017.
 */

public class Event {

    private String sum;

    private String day;

    private int[] hours;

    private boolean siImportant;

    private String salle;

    private String titre;

    private String desc;

    public Event(String event) {

        //Log.d("DebugEvent", "Creating Event with values : ");

        try {
            JSONObject Jevent = new JSONObject(event);

            this.sum = Jevent.optString(JsonKeys.SUM);
            this.day = Jevent.optString(JsonKeys.JOUR);
            this.siImportant = Jevent.optBoolean(JsonKeys.isIMPORTANT);
            this.salle = Jevent.optString(JsonKeys.SALLE);
            this.titre = Jevent.optString(JsonKeys.NAME);
            this.desc = Jevent.optString(JsonKeys.DESC);

            //Log.d("DebugEvent", "sum : " + this.sum + "\nday : " + this.day + "\nsiImportant : " + this.siImportant + "\nsalle : " + this.salle + "\ntitre : " + this.titre + "\ndesc : " + this.desc);

            JSONArray Jhours = Jevent.getJSONArray(JsonKeys.HOUR);
            this.hours = new int[Jhours.length()];

            for (int i=0; i<this.hours.length;i++) {
                this.hours[i] = Jhours.getInt(i);
            }

            //Log.d("DebugEvent", "hours : " + hoursToString());
        }
        catch (JSONException je) {
            Log.e("DebugEvent", "Unable to retrieve event : " + event, je);
        }
    }

    public String getDay() {
        //Log.d("DebugEvent", "Getting day of " + this.day + " at " + hoursToString());
        return day;
    }

    public String getTitre() {
        //Log.d("DebugEvent", "Getting titre of " + this.day + " at " + hoursToString());
        return this.titre;
    }

    public String getSalle() {
        return salle;
    }

    public String getDesc() {
        return desc;
    }

    public int[] getHours() {
        return hours;
    }

    public String hoursToString() {
        return String.format(Locale.FRANCE, "%02d:%02d - %02d:%02d", hours[0], hours[1], hours[2], hours[3]);
    }
}
