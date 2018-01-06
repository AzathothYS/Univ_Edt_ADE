package com.a.univ_edt_ade.ArboFile;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Singleton stockant l'arborescence
 */

public class ArboStorage {
    private static final ArboStorage ArboInstance = new ArboStorage();

    public static ArboStorage getInstance() {
        return ArboInstance;
    }

    private ArboStorage() {}


    public static ArboExplorer arbo;

    private static File arboFile;
    private static final String FILE_NAME = "arbo_ADE.txt";


    public static void setFile(Context context) {
        if (arboFile != null)
            return; // le fichier à déjà été initialisé

        arboFile = new File(context.getExternalFilesDir(null), FILE_NAME);

        if (!arboFile.exists())
            Log.e("ArboStorage", "Unable to initialise Arborescence! " + FILE_NAME + " not found!",
                    new FileNotFoundException("arbo_ADE.txt not found!"));
    }


    public static void setArborescence() {
        if (arboFile == null) {
            Log.d("ArboStorage", "Could not initialise Arborescence! File object if null!");
            return;
        }

        if (arbo != null) {
            Log.d("ArboStorage", "Arborescence has already been initialised.");
            return;
        }

        arbo = new ArboExplorer(arboFile);

        Log.d("ArboStorage", "Initialised ArboStorage.");
    }
}
