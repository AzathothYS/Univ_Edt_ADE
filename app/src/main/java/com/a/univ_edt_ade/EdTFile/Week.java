package com.a.univ_edt_ade.EdTFile;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 7 on 16/09/2017.
 */

public class Week {

    private String _sum;

    // format : YYYY-MM-DD
    private String start;

    private String end;

    private int important;

    private int[] empty_days;

    private int hours;

    private int week_nb;

    private String[] days;

    public Week(String sum, String start, String end, int important, int[] empty_days, int hours, int week_nb, String[] days) {
        this._sum = sum;
        this.start = start;
        this.end = end;
        this.important = important;
        this.empty_days = empty_days;
        this.hours = hours;
        this.week_nb = week_nb;
        this.days = days;
    }

    public Day[] getDays() {
        Day[] Days = new Day[7];

        int i = 0;
        for (String day : this.days) {
            Days[i++] = day != null ? new Day(day) : null;
        }

        return Days;
    }

    /**
     * retourne le jour voulu dans cette semaine, ou "" si il n'y a rien ce jour-ci
     * utiliser 'dayInWeek' est plus rapide et bug proof
     * @param dayInWeek 0 à 6 -> le numéro du jour dans la semaine (0 pour lundi, etc...)
     * @param date la date (format YYYYMMDD) du jour voulu. Si non nul, utilisé à la place de dayInWeek
     */
    public String getDay(int dayInWeek, @Nullable String date) {

        if (date != null) {
            Log.d("DebugWeek", "Result of str.replace(\"-\", \"\") : " + start.replace("-", ""));
            dayInWeek = Integer.parseInt(date) - Integer.parseInt(start.replace("-", ""));
            Log.d("DebugWeek", "Result of date processing for " + date + " in week " + start + " : " + dayInWeek);
        }

        for (int day : empty_days) {
            if (dayInWeek == day) {
                Log.d("DebugWeek", "The day " + dayInWeek + " is empty in week " + start);
                return "";
            }
        }

        if (!(dayInWeek < days.length && dayInWeek >= 0)) {
            // date compliquée : changement de mois ou d'année dans cette semaine, on regarde les dates une à une
            try {
                int i = 0;
                for (String day : days) {
                    JSONObject Jday = new JSONObject(day);
                    if (date == Jday.getString(JsonKeys.DAY)) {
                        dayInWeek = i;
                        break;
                    }
                    i++;
                }
                if (i == days.length) {
                    // aucun jour dans la semaine ne correspond à 'date'
                    Log.d("DebugWeek", "No Match for " + date + " in " + start);
                    return "";
                }
            }
            catch (JSONException je) {
                Log.e("ERRORWeek", "Week n°" + week_nb + " n'a pas un days[] correct.", je);
            }
        }

        return days[dayInWeek];
    }

    public int getWeek_nb() {
        return this.week_nb;
    }
}
