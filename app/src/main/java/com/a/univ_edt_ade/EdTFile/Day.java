package com.a.univ_edt_ade.EdTFile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 7 on 16/09/2017.
 */

public class Day {

    private String sum;

    private String day;

    private float hours;

    private int important;

    private String[] events;

    public Day(String day) {
        try {
            JSONObject Jday = new JSONObject(day);

            this.sum = Jday.optString(JsonKeys.SUM);
            this.day = Jday.optString(JsonKeys.DAY);
            this.hours = (float) Jday.optDouble(JsonKeys.HOURS);
            this.important = Jday.optInt(JsonKeys.IMPORTANT);

            JSONArray events = Jday.getJSONArray(JsonKeys.EVENTS);
            this.events = new String[events.length()];

            for (int i=0; i<events.length();i++) {
                this.events[i] = events.getString(i);
            }

            //Log.d("DebugDay", "Retrieved data for day " + day);
        }
        catch (JSONException je) {
            Log.e("ERRORday", "Failed to create day from str : " + day, je);
        }
    }

    public Day(String sum, String day , float hours, int important, String[] events) {
        this.sum = sum;
        this.day = day;
        this.hours = hours;
        this.important = important;
        this.events = events;
    }

    public Event[] getEvents() {
        Event[] events_lol = new Event[this.events.length];

        //Log.d("DebugGetEvents", "events size : " + events_lol.length);

        int i = 0;
        for (String event : this.events) {
            //Log.d("DebugGetEvents", "Converting this to event :\n" + event);
            events_lol[i] = new Event(event);
            //Log.d("DebugGetEvents", "Event created : " + events_lol[i].getTitre() + " at " + events_lol[i].hoursToString());
            i++;
        }

        //Log.d("DebugGetEvents", "Passing to EdtDisplay this list of events : ");
        i = 0;
        for (Event event : events_lol) {
            //Log.d("DebugGetEvents", "   event n°" + i++ + " : " + event.getTitre() + " at " + event.hoursToString());
        }
        //Log.d("DebugGetEvents", "That's it!");

        return events_lol;
    }

    public String getDay() {
        return day;
    }
}
