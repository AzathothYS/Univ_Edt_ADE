package com.a.univ_edt_ade.EdTFile;


import android.content.Context;
import android.util.Log;

/**
 * Singleton stockant l'EdT
 */

public class EdTStorage {
    private static final EdTStorage EdTInstance = new EdTStorage();

    public static EdTStorage getInstance() {
        return EdTInstance;
    }

    private EdTStorage() {}


    private JsonEdt EdT;
    public static boolean isEdTInitialised = false;

    public void setEdT(Context context) {
        EdT = new JsonEdt(context);
        EdT.getJSONedt();

        isEdTInitialised = true;

        Log.d("EdTStorage", "Initialised EdTStorage.");
    }

    public Week getWeek(int weeknb, int year, boolean idontcare) {
        return EdT.getWeek(weeknb, year, idontcare);
    }
}
