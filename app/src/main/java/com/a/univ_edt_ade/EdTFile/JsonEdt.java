package com.a.univ_edt_ade.EdTFile;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;


/**
 * Stockage de l'Edt en Json envoié par le serveur
 * Donne les checksums nécessaires pour déterminer si il faut mettre à jour le fichier
 *
 * Lecture du Json :
 *  en fonction du fichier demandé
 *  en fonction de la semaine demandée
 * Sort une liste avec les semaines, qui stokent les jours, qui stockent les events
 */

public class JsonEdt {

    // TODO: à transformer en singleton, ou l'encapsuler dans un singleton, pour qu'il soit accessible par toutes les activités sans nouvelle instanciation

    public final String FILE_NAME = "emploi_du_temps.json";
    public File file;

    public String edtInString = null;
    public JSONArray edtJson = null;

    public String[] file_info = new String[2];
    private JSONObject[] Weeks;
    private int[] weeks_nb;

    public JsonEdt(Context context) {
        Log.d("Univ_Edt_ADE_TAG", "Managing Json stored file...");
        try {
            file = new File(context.getExternalFilesDir(null), FILE_NAME);

            if (!file.exists()) {
                file.createNewFile();
                Log.println(Log.DEBUG, "Univ_Edt_ADE_TAG", "File " + FILE_NAME + " created in " +
                        file.getAbsolutePath() + " as " + file.getName());

            } else {
                Log.println(Log.DEBUG, "Univ_Edt_ADE_TAG", "File " + FILE_NAME + " already existed at " +
                        file.getAbsolutePath() + " as " + file.getName());
            }
        }
        catch (Exception e){
            Log.e("Exception", "File creation failed: " + e.toString());
        }
    }

    public void getJSONedt() {

        edtInString = null;

        try{
            FileInputStream edtFile = new FileInputStream(file);

            byte[] buffer = new byte[edtFile.available()];

            while (edtFile.read(buffer) != -1);

            edtInString = new String(buffer);

            if (!edtInString.equals("")) {
                Log.d("Univ_Edt_ADE_TAG", "edtFile length : " + edtInString.length());
            } else {
                Log.w("Univ_Edt_ADE_TAG", "edtInString is empty!", new IOException("FILE EMPTY"));
            }
        }
        catch (Exception e){
            Log.e("Exception", "Something bad happened! (getJSONedt) : " + e.toString(), e);
        }

        edtJson = null;

        try {
            edtJson = new JSONArray(edtInString);

            Log.d("Univ_Edt_ADE_TAG", "edt in json : " + edtJson.length() + ", and : \n" + edtJson.toString());
        }
        catch (JSONException e){
            Log.e("Exception", "Invalid Json in " + FILE_NAME + " : " + e.toString(), e);
        }

        try {
            file_info[0] = (String) edtJson.getJSONObject(1).get(JsonKeys.SOURCE);
            file_info[1] = (String) edtJson.getJSONObject(1).get(JsonKeys.FILE_SUM);

            Log.d("JsonEdt", "file_info : \n" + file_info[0] + "\n" + file_info[1]);

            Weeks = new JSONObject[edtJson.length() -1];
            weeks_nb = new int[Weeks.length];

            for (int i=0;i<edtJson.length() -1;i++) {
                Weeks[i] = edtJson.getJSONObject(i);
                weeks_nb[i] = Weeks[i].getInt(JsonKeys.WEEK);
                Log.d("Json", "Retrieved data of week n°" + weeks_nb[i]);
            }


        }
        catch (JSONException je) {
            Log.e("JsonException", "ERROR", je);
        }

        if (Weeks.length + 1 == edtJson.length()) {
            Log.d("Json", "Deleted edtJson because all of its values have been retrieved.");
            edtJson = null;
        }
        else {
            Log.d("JsonERROR", "ERROR : not all values of edtJson have been retireved!");
        }
    }

    public Week getWeek(int week_nb, int year, boolean IdontCare) {

        if (IdontCare) {
            Log.d("DebugGetWeek", "Week length :" + Weeks.length);
            return toWeek(Weeks[0]);
        }

        int week_index = -1;
        boolean serveralWeeks = true;
        for (int i : weeks_nb) {
            week_index++;
            if (i == week_nb) {
                serveralWeeks = !serveralWeeks;
                if (serveralWeeks) {
                    break;
                }
            }
        }

        Log.d("DebugGetWeek", "serveral weeks: " + serveralWeeks + " - week_index:" + week_index + " - for week " + week_nb + " in " + year);

        if (serveralWeeks) {
            try {

                for (week_index = 0; week_index < weeks_nb.length; week_index++) {
                    if (weeks_nb[week_index] == week_nb) {
                        if (Integer.parseInt(Weeks[week_index].getString(JsonKeys.START).substring(0, 4)) == year) {
                            break;
                        }
                    }
                }
            }
            catch (Exception e) {
                Log.e("JsonERROR", "getWeek failed for " + week_nb + " in " + year, e);
            }
        }

        return toWeek(Weeks[week_index]);
    }

    private Week toWeek(JSONObject Jweek) {

        JSONArray Jempty_days = null;
        JSONArray Jdays = null;

        try {
            Jempty_days = Jweek.getJSONArray(JsonKeys.EMPTYDAYS);
            Jdays = Jweek.getJSONArray(JsonKeys.DAYS);
        }
        catch (JSONException je) {
            Log.e("JsonERROR" , "Something bad in toWeek for " + Jweek.optInt(JsonKeys.WEEK, -1), je);
        }

        int[] empty_days = new int[Jempty_days.length()];
        String[] days = new String[7];

        try {
            for (int i = 0; i < empty_days.length; i++) {
                empty_days[i] = Jempty_days.getInt(i);
            }
            boolean isEmpty;
            int j = 0;
            for (int i = 0; i < days.length; i++) {

                isEmpty = false;
                for (int d : empty_days)
                    if (d == i)
                        isEmpty = true;

                days[i] = isEmpty ? null : Jdays.getString(j++);
                Log.d("DaysOUT", "day n°" + i + " = " + days[i]);
            }
        }
        catch (JSONException je) {
            Log.e("JsonERROR" , "Something bad happened in 'toWeek' for week n°" + Jweek.optInt(JsonKeys.WEEK, -1), je);
        }

        //Log.d("Debug", "toWeek : Successfully converted week n°" + Jweek.optInt(JsonKeys.WEEK, -1));

        return new Week(Jweek.optString(JsonKeys.SUM),
                             Jweek.optString(JsonKeys.START),
                             Jweek.optString(JsonKeys.END),
                             Jweek.optInt(JsonKeys.IMPORTANT),
                             empty_days,
                             Jweek.optInt(JsonKeys.HOURS),
                             Jweek.optInt(JsonKeys.WEEK),
                             days);
    }
}
