package com.a.univ_edt_ade.ArboFile;

import android.content.Context;
import android.util.Log;

/**
 * Singleton stockant l'arborescence
 */

public class ArboStorage {
    private static final ArboStorage ArboInstance = new ArboStorage();

    public static ArboStorage getInstance() {
        return ArboInstance;
    }

    private ArboStorage() {}


    public ArboExplorer arbo;
    public static boolean isArboInitialised = false;

    public void setArborescence(Context context) {
        arbo = new ArboExplorer(context);

        isArboInitialised = true;

        Log.d("ArboStorage", "Initialised ArboStorage.");
    }
}
