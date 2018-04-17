package com.a.univ_edt_ade.PathFile;

import android.content.Context;
import android.icu.text.LocaleDisplayNames;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.a.univ_edt_ade.ArboFile.ArboExplorer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import static com.a.univ_edt_ade.PathFile.PathFileKeys.EdT_NAME;
import static com.a.univ_edt_ade.PathFile.PathFileKeys.HASH;
import static com.a.univ_edt_ade.PathFile.PathFileKeys.PATH_ARRAY;
import static com.a.univ_edt_ade.PathFile.PathFileKeys.PATH_FILES;

/**
 * TODO
 *
 *
 *
 * Structure du Json :
 *
 * {
 *      "edt_name":  nom de l'emploi du temps,
 *      "hash": hash de l'emploi du temps,
 *      "path_files": array contenant le nom de chaque fichier utilisés dans l'emploi du temps
 *      [
 *          NOM FICHIER 1
 *          NOM FICHIER 2
 *          etc...
 *      ]
 *      "path": array contenant le 'path' vers chacun des fichiers utilisés dans l'emploi du temps
 *      [
 *          [
 *              NOM fichier/dossier,
 *              INDEX du fichier/dossier dans le dossier parent,
 *              LIGNE du fichier/dossier dans le fichier,
 *              NOM fichier/dossier,
 *              INDEX du fichier/dossier dans le dossier parent,
 *              LIGNE du fichier/dossier dans le fichier,
 *              etc...
 *          ],
 *          [
 *              NOM fichier/dossier,
 *              INDEX du fichier/dossier dans le dossier parent,
 *              LIGNE du fichier/dossier dans le fichier,
 *              etc...
 *          ]
 *          etc...
 *      ]
 * }
 * etc...
 */

// TODO : essayer de casser le Json en mettant des ", \, [, (, ,, } partout, et même des caractères unicodes


public class PathFileExplorer {

    public static final String PATH_FILE_NAME = "EdT_Paths.txt";

    private final File pathFile;
    private JSONArray pathArrayJson;


    public PathFileExplorer(Context context) {

        pathFile = new File(context.getExternalFilesDir(null), PATH_FILE_NAME);

        if (!pathFile.exists()) {
            try {
                pathFile.createNewFile();
            } catch (IOException e) {
                Log.e("EdTList", "Couldn't create file '" + PATH_FILE_NAME + "' in external dir", e);
            }

            Log.d("EdTList", "File created : '" + PATH_FILE_NAME + "' in external dir");

            pathArrayJson = new JSONArray();

        } else {
            // on charge tout le fichier dans le JSON
            try {
                FileInputStream pathStream = new FileInputStream(pathFile);

                if (pathStream.available() == 0) {
                    Log.d("PathFileExplorer", "Path file was empty");
                    pathArrayJson = new JSONArray();
                } else {
                    byte[] buffer = new byte[pathStream.available()];
                    pathStream.read(buffer);

                    pathArrayJson = new JSONArray(new String(buffer));
                }

            } catch (IOException e) {
                Log.e("PathFileExplorer", "An error happened while parsing " + PATH_FILE_NAME, e);
            } catch (JSONException e) {
                Log.e("PathFileExplorer", "Incorrect Json in " + PATH_FILE_NAME, e);
            }
        }
    }


    public void save() {
        FileWriter writer = null;

        try {
            writer = new FileWriter(pathFile);

            writer.write(pathArrayJson.toString(4));

        } catch (IOException e) {
            Log.e("PathFileExplorer", "An error occured while trying to write to file " + PATH_FILE_NAME, e);
        } catch (JSONException e) {
            Log.e("PathFileExplorer", "An error occured while trying to convert Json Array to string", e);

        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public String[] getPathFilesNames(int index) {

        JSONObject edtJson = pathArrayJson.optJSONObject(index);
        if (edtJson == null) {
            Log.d("PathFileExplorer", "ERROR : edt at " + index + " isn't valid or doesn't exist!");
            return null;
        }

        JSONArray namesJsonArray = edtJson.optJSONArray(PATH_FILES);
        if (namesJsonArray == null) {
            Log.d("PathFileExplorer", "ERROR : edt at " + index + " doesn't have a correct names array!");
            return null;
        }

        String[] output = new String[namesJsonArray.length()];

        try {
            for (int i = 0;i < namesJsonArray.length();i++) {
                output[i] = namesJsonArray.getString(i);
            }
        } catch (JSONException e) {
            Log.e("PathFileExplorer", "Json in " + index + " dosen't have a valid names array! Parsing error.", e);
        }

        return output;
    }


    public String getPathInEdTAt(int EdTIndex, int pathIndex, LinkedList<ArboExplorer.PathEntry> output) {

        JSONObject targetEdT = pathArrayJson.optJSONObject(EdTIndex);
        if (targetEdT == null) {
            Log.d("PathFileExplorer", "ERROR : path at " + EdTIndex + " isn't valid or doesn't exist!");
            return null;
        }

        String pathName = "";
        try {
            pathName = (String) targetEdT.getJSONArray(PATH_FILES).get(pathIndex);
        } catch (JSONException e) {
            Log.e("PathFileExplorer", "ERROR : no path present in path object at index=" + pathIndex,
                    new JSONException("Missing Array : '" + PATH_FILES + "' in EdT at index " + EdTIndex));
        }

        try {
            JSONArray pathJson = targetEdT.getJSONArray(PATH_ARRAY).getJSONArray(pathIndex);

            ArboExplorer.PathEntry pathEntry;
            for (int i = 0; i < pathJson.length();i += 3) {
                pathEntry = new ArboExplorer.PathEntry(
                        pathJson.getInt(i + 1),
                        pathJson.getInt(i + 2),
                        pathJson.getString(i));

                output.add(pathEntry);
            }

        } catch (JSONException e) {
            Log.e("PathFileExplorer", "ERROR : no path present in path object at index=" + pathIndex,
                    new JSONException("Missing Array : '" + PATH_ARRAY + "' in EdT at index " + EdTIndex));
        }

        return pathName;
    }


    public String[] getEdTList() {
        String[] EdTNames = new String[pathArrayJson.length()];

        for (int i=0;i<EdTNames.length;i++) {
            try {
                EdTNames[i] = pathArrayJson.getJSONObject(i).getString(EdT_NAME);
            } catch (JSONException e) {
                Log.e("PathFileExplorer", "An error occured while getting the name of the EdT at " + i, e);
            }
        }

        return EdTNames;
    }


    public String getEdTAt(int index, LinkedList<LinkedList<ArboExplorer.PathEntry>> output) {

        JSONObject outputPath = pathArrayJson.optJSONObject(index);
        if (outputPath == null) {
            Log.d("PathFileExplorer", "ERROR : path at " + index + " isn't valid or doesn't exist!");
            return null;
        }

        String name = outputPath.optString(EdT_NAME, "");

        JSONArray pathArray = outputPath.optJSONArray(PATH_ARRAY);
        if (pathArray == null) {
            Log.e("PathFileExplorer", "ERROR : no path present in path object at index=" + index,
                    new JSONException("Missing Array : '" + PATH_ARRAY + "' in EdT at index " + index));
            return name;
        }

        output = new LinkedList<>();

        // on remplit 'output' avec le path présent dans 'outputPath'
        ArboExplorer.PathEntry pathEntry;
        JSONArray pathJson;
        try {
            for (int pathIndex=0;pathIndex<pathArray.length();pathIndex++) {
                pathJson = pathArray.getJSONArray(pathIndex);

                LinkedList<ArboExplorer.PathEntry> pathOut = new LinkedList<>();

                for (int i = 0; i < pathJson.length();i += 3) {
                    pathEntry = new ArboExplorer.PathEntry(
                            pathJson.getInt(i + 1),
                            pathJson.getInt(i + 2),
                            pathJson.getString(i));

                    pathOut.add(pathEntry);
                }

                output.add(pathOut);
            }

        } catch (JSONException e) {
            Log.e("PathFileExplorer", "ERROR : invalid Json or incomplete array", e);
        }

        return name;
    }


    private int doEdTExist(String EdTName) {
        try {
            for (int i = 0; i< pathArrayJson.length(); i++) {
                JSONObject element = pathArrayJson.getJSONObject(i);

                if (element.has(EdT_NAME))
                    if (EdTName.equals(element.getString(EdT_NAME))) {
                        return i;
                    }
            }

        } catch (JSONException e) {
            Log.e("PathFileExplorer", "An error occured while iterating Json contents of " + PATH_FILE_NAME, e);
        }

        return -1;
    }


    private int isPathAlreadyPresent(int edtIndex, String pathName) {
        try {
            JSONArray pathNames = pathArrayJson.getJSONObject(edtIndex).getJSONArray(PATH_FILES);

            for (int i=0;i<pathNames.length();i++) {
                if (pathNames.getString(i).equals(pathName))
                    return i;
            }

        } catch (JSONException e) {
            Log.e("PathFileExplorer", "Invalid json EdT at " + edtIndex + " for '" + pathName + "'", e);
        }

        return -1;
    }


    public void addPath(LinkedList<ArboExplorer.PathEntry> pathToSave, int EdTIndex, String EdTName, String pathName) {
        try {
            long hash = computeHash(pathToSave);


            int edtIndex = doEdTExist(EdTName);

            Log.d("PathFileExplorer", "'AddPath' args : EdTIndex=" + EdTIndex + " - Name=" + EdTName + " - Found Index=" + edtIndex + " - pathName=" + pathName);

            if (edtIndex != EdTIndex && EdTIndex < pathArrayJson.length()) {
                Log.d("PathFileExplorer", "Wowowowow... on a un problème! '" + EdTName + "'=" + edtIndex + " or l'index donné est : " + EdTIndex);
            }

            JSONObject edt;
            JSONArray edtPathArray;
            JSONArray edtPathNames;
            int pathIndex;
            if (edtIndex != -1) {
                if (pathArrayJson.getJSONObject(edtIndex).optLong(HASH) == hash) {
                    Log.d("PathFileExplorer", "path '" + pathName+ "' is already present in the EdT '" + EdTName + "', with the same hash");
                    return;
                }

                Log.d("PathFileExplorer", "path '" + pathName + "' is already present in the EdT '" + EdTName + "', but with a different hash");

                edt = pathArrayJson.getJSONObject(edtIndex);
                edtPathArray = edt.getJSONArray(PATH_ARRAY);
                edtPathNames = edt.getJSONArray(PATH_FILES);
                // TODO : mettre à jour le hash??????

                pathIndex = isPathAlreadyPresent(edtIndex, pathName);

            } else {
                // l'emploi du temps n'existe pas, on en crée un nouveau

                Log.d("PathFileExplorer", "Created a new EdT with name : " + EdTName);

                edt = new JSONObject();

                edt.put(EdT_NAME, EdTName);
                edt.put(HASH, hash);

                edtPathArray = new JSONArray();
                edtPathNames = new JSONArray();

                pathIndex = -1;
            }

            JSONArray pathInJson = new JSONArray();

            for (ArboExplorer.PathEntry pathEntry : pathToSave) {
                pathInJson.put(pathEntry.NOM);
                pathInJson.put(pathEntry.INDEX);
                pathInJson.put(pathEntry.LIGNE); // TODO : necessaire ??
            }

            // on rajoute le path à la liste si il n'y était pas déjà, sinon on le remplace
            if (pathIndex < 0) {
                Log.d("PathFileExplorer", "Adding the new path to the EdT...");

                edtPathArray.put(pathInJson);

                edtPathNames.put(pathName);
                edt.put(PATH_FILES, edtPathNames);

            } else {
                Log.d("PathFileExplorer", "Replacing the path at " + pathIndex + " to the EdT...");

                // on supprime le path dèjà présent, avant de la rajouter
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    edtPathArray.remove(edtIndex);

                } else {
                    // alternative forcée à cause du manque de la fonction 'remove', laborieux mais fonctionnel
                    JSONArray temp = new JSONArray();
                    for (int i=0;i<edtPathArray.length();i++) {
                        if (i != edtIndex) {  // on ignore l'index que l'on veut supprimmer
                            temp.put(edtPathArray.get(i));
                        }
                    }
                    edtPathArray = temp;
                }

                edtPathArray.put(pathIndex, pathInJson);


                // on modifie la liste des path si elle a changée
                JSONArray pathNamesList = pathArrayJson.getJSONObject(edtIndex).getJSONArray(PATH_FILES);
                if (!pathName.equals(pathNamesList.get(pathIndex))) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        pathNamesList.remove(pathIndex);
                    } else {
                        JSONArray temp = new JSONArray();
                        for (int i=0;i<edtPathArray.length();i++) {
                            if (i != edtIndex) {  // on ignore l'index que l'on veut supprimmer
                                temp.put(edtPathArray.get(i));
                            } else {
                                temp.put(pathName);
                            }
                        }
                        pathNamesList = temp;
                    }

                    edt.put(PATH_FILES, pathNamesList);
                }
            }

            edt.put(PATH_ARRAY, edtPathArray); // on remplace l'array par la nouvelle


            saveModificationsOfEdTAt(edtIndex, edt);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public boolean changeEdTNameAt(int index, String newName) {
        JSONObject targetEdT = pathArrayJson.optJSONObject(index);
        if (targetEdT == null) {
            Log.d("PathFileExplorer", "ERROR : path at " + index + " isn't valid or doesn't exist!");
            return false;
        }

        String oldName = targetEdT.optString(EdT_NAME);
        if (oldName != null)
            if (oldName.equals(newName)) {
                Log.d("PathFileExplorer", "Le nouveau nom est le même que le nom actuel!");
                return false;
            }

        try {
            targetEdT.put(EdT_NAME, newName);

            saveModificationsOfEdTAt(index, targetEdT);

        } catch (JSONException e) {
            Log.e("PathFileExplorer", "An error occured while updating the name of the EdT at " + index, e);
        }

        return true;
    }


    public void editEdTAt(int EdTIndex, int pathIndex, @Nullable LinkedList<ArboExplorer.PathEntry> path) {
        try {
            JSONObject edt = pathArrayJson.getJSONObject(EdTIndex);
            int pathArrayLength = edt.getJSONArray(PATH_ARRAY).length();

            if (pathIndex >= pathArrayLength) {
                // on veut rajouter un nouveau fichier

                JSONArray newPathJson = new JSONArray();
                for (ArboExplorer.PathEntry pathEntry : path) {
                    newPathJson.put(pathEntry.NOM);
                    newPathJson.put(pathEntry.INDEX);
                    newPathJson.put(pathEntry.LIGNE);
                }

                edt.accumulate(PATH_ARRAY, newPathJson);

                Log.d("PathFileExplorer", "Added file to EdT at " + EdTIndex);

            } else if (pathIndex < pathArrayLength && path == null) {
                // on veut supprimmer un fichier

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    edt.getJSONArray(PATH_ARRAY).remove(pathIndex);

                } else {
                    JSONArray pathArray = edt.getJSONArray(PATH_ARRAY);
                    JSONArray temp = new JSONArray();
                    for (int i = 0;i < pathArrayLength; i++) {
                        if (i != pathIndex) {  // on ignore l'index que l'on veut supprimmer
                            try {
                                temp.put(pathArray.get(i));
                            } catch (JSONException e) {
                                Log.e("PathFileExplorer", "An error occured while parsing the Json file, at edt " + EdTIndex + " at path " + i, e);
                            }
                        }
                    }
                    edt.put(PATH_ARRAY, temp);
                }

                Log.d("PathFileExplorer", "Removed file " + pathIndex + " of EdT at " + EdTIndex);
            }

            saveModificationsOfEdTAt(EdTIndex, edt);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void removePathOfEdTAt(int EdTIndex, int pathIndex) {
        try {
            JSONObject edt = pathArrayJson.getJSONObject(EdTIndex);
            JSONArray pathArray = edt.getJSONArray(PATH_ARRAY);
            JSONArray pathNames = edt.getJSONArray(PATH_FILES);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                pathArray.remove(pathIndex);
                pathNames.remove(pathIndex);

            } else {
                JSONArray temp = new JSONArray();
                JSONArray temp2 = new JSONArray();
                for (int i=0;i<pathArray.length();i++)
                    if (i != pathIndex) {
                        temp.put(pathArray.get(i));
                        temp2.put(pathNames.get(i));
                    }

                pathArray = temp;
                pathNames = temp2;
            }

            edt.put(PATH_ARRAY, pathArray);
            edt.put(PATH_FILES, pathNames);

            saveModificationsOfEdTAt(EdTIndex, edt);

            // DEBUG
            Log.d("PathFileExplorer", "Result of delet operation : ");
            Log.d("PathFileExplorer", "     path array length:" + pathArrayJson.getJSONObject(EdTIndex).getJSONArray(PATH_ARRAY).length());
            Log.d("PathFileExplorer", "     path names length:" + pathArrayJson.getJSONObject(EdTIndex).getJSONArray(PATH_FILES).length());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeEdTAt(int EdTIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pathArrayJson.remove(EdTIndex);

        } else {
            JSONArray temp = new JSONArray();
            for (int i=0;i < pathArrayJson.length();i++) {
                if (i != EdTIndex) {
                    try {
                        temp.put(pathArrayJson.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.e("PathFileExplorer", "An error occured while parsing the Json file, at EdT " + i, e);
                    }
                }
            }
        }

        Log.d("PathFileExplorer", "Removed EdT at " + EdTIndex);
    }



    private void saveModificationsOfEdTAt(int EdTIndex, JSONObject modifiedEdT) {
        if (EdTIndex < 0) {
            // nouveau emploi du temps
            pathArrayJson.put(modifiedEdT);

        } else {
            // on édite un emploi du temps -> on supprime l'ancien et on le remplace par le nouveau
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                pathArrayJson.remove(EdTIndex);

            } else {
                // alternative forcée à cause du manque de la fonction 'remove', laborieux mais fonctionnel
                JSONArray temp = new JSONArray();
                for (int i = 0;i < pathArrayJson.length(); i++) {
                    if (i != EdTIndex) {  // on ignore l'index que l'on veut supprimmer
                        try {
                            temp.put(pathArrayJson.get(i)); // TODO : à tester un jour!
                        } catch (JSONException e) {
                            Log.e("PathFileExplorer", "An error occured while parsing the Json file, at edt " + i, e);
                        }
                    }
                }
                pathArrayJson = temp;
            }

            try {
                pathArrayJson.put(EdTIndex, modifiedEdT);
            } catch (JSONException e) {
                Log.e("PathFileExplorer", "An error occured while updating the Json file, at index " + EdTIndex, e);
            }
        }
    }


    // TODO : delete ??
    private long computeHash(LinkedList<ArboExplorer.PathEntry> path) {

        MessageDigest hash = null;
        try {
            hash = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e("PathFileExplorer", "No such algorithm : 'MD5'", e);
        }

        StringBuilder condensedPath = new StringBuilder();
        for (ArboExplorer.PathEntry pathEntry : path) {
            condensedPath.append(pathEntry.NOM);
            condensedPath.append(pathEntry.INDEX);
            condensedPath.append(pathEntry.LIGNE); // TODO : nécessaire ?

            hash.update(condensedPath.toString().getBytes());
            condensedPath.setLength(0);
        }

        byte[] digest = hash.digest();

        Log.d("PathFileExplorer", "DEBUG : digest length=" + digest.length); // TODO : DEBUG

        // on compacte le hash en 8 bits, pour qu'il tienne dans un long
        byte[] convertedDigest = new byte[8];
        for (int i=0;i<digest.length;i++) {
            convertedDigest[i % 8] |= digest[i];
        }

        // on crée le long à partir des bytes du digest
        long longDigest = 0L;
        for (int i=0;i<8;i++) {
            longDigest <<= 8;
            longDigest ^= convertedDigest[i] & 0xFF;
        }

        return longDigest;
    }
}
