package com.a.univ_edt_ade.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.a.univ_edt_ade.ArboFile.ArboExplorer;
import com.a.univ_edt_ade.CustomsAssets.EdTCardAdapter;
import com.a.univ_edt_ade.CustomsAssets.EdTInfoFragment;
import com.a.univ_edt_ade.CustomsAssets.FileListAdapter;
import com.a.univ_edt_ade.PathFile.PathFileExplorer;
import com.a.univ_edt_ade.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import static com.a.univ_edt_ade.PathFile.PathFileExplorer.PATH_FILE_NAME;

public class EdTList extends BaseActivity implements EdTCardAdapter.edtAdapterCallback,
        EdTInfoFragment.EdTInfoCallback, FileListAdapter.FileListCallback {

    public static final String PREFERENCE_KEY_EdT_LIST = "EdTList";


    public static final String RESULT_PATH = "path",
                               RESULT_TYPE = "type",
                               RESULT_NAME = "name",
                               RESULT_INDEX = "index",
                               RESULT_FILE_INDEX = "pathfileindex";

    public static final int RESULT_PATH_EDITED = 1,
                            RESULT_PATH_ADDED = 2,
                            RESULT_PATH_REMOVED = 3;

    public static final String FRAG_TAG = "info_frag";

    public PathFileExplorer pathFile;
    private boolean isPathFileModified = false;


    public static LinkedList<String> EdTList;


    private EdTCardAdapter listAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayoutToActivity(R.layout.activity_edt_list);
        setMenuItemChecked(R.id.Menu_arboSelect);

        getEdTList();

        listAdapter = new EdTCardAdapter(getEdTNames());

        final RecyclerView edtRList = (RecyclerView) findViewById(R.id.EdTRList);
        edtRList.setAdapter(listAdapter);

        RecyclerView.LayoutManager edtRListManager = new LinearLayoutManager(this);
        edtRList.setLayoutManager(edtRListManager);

        pathFile = new PathFileExplorer(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    @Override
    public LinkedList<ArboExplorer.PathEntry> getPathAt(int EdTIndex, int position) {
        LinkedList<ArboExplorer.PathEntry> output = new LinkedList<>();

        pathFile.getPathInEdTAt(EdTIndex, position, output);

        return output;
    }

    @Override
    public void makeEdTInfoFrag(int pos, String name) {
        Log.d("EdTList", "Creating Fragment for EdT '" + name + "' at pos=" + pos);

        EdTInfoFragment.newInstance(pos, name, pathFile.getPathFilesNames(pos))
                .show(getSupportFragmentManager(), FRAG_TAG);
    }

    @Override
    public void deleteEdTAt(final int pos) {
        Log.d("EdTList", "Deleting EdT at pos " + pos);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(EdTList.get(pos));
        builder.setMessage("Voulez-vous supprimmer cet emploi du temps ?");

        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EdTList.remove(pos);
                pathFile.removeEdTAt(pos);

                isPathFileModified = true;
                listAdapter.updateData();
            }
        });

        builder.create().show();
    }


    @Override
    public void onFinishEditDialog(int pos, String inputText) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show();

        if (pathFile.changeEdTNameAt(pos, inputText)) {
            // le changement de nom est réussi
            EdTList.set(pos, inputText);

            listAdapter.updateData();

            isPathFileModified = true;

            Log.d("EdTList", "Changement de nom réussi de " + pos + " pour : " + inputText);

        } else { // TODO : debug
            Log.d("EdTList", "Échec du changement de nom de " + pos + " pour : " + inputText);
        }

        if (isPathFileModified) {
            listAdapter.updateData();
        }
    }

    @Override
    public void launchNewActivity(int position, String name, @Nullable LinkedList<ArboExplorer.PathEntry> initialPath) {
        Toast.makeText(this, "yolo : " + position + " = " + name, Toast.LENGTH_SHORT).show();

        int pos = ((EdTInfoFragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG)).pos;

        Intent toArboSelect = new Intent(this, ArboSelect.class);

        toArboSelect.putExtra("EdTName", name);
        toArboSelect.putExtra("EdTPos", pos);

        if (initialPath != null) {

            JSONArray storage = new JSONArray();
            for (ArboExplorer.PathEntry pathEntry : initialPath) {
                storage.put(pathEntry.INDEX);
                storage.put(pathEntry.LIGNE);
                storage.put(pathEntry.NOM);
            }

            toArboSelect.putExtra("initialPath" , storage.toString());
        }

        startActivityForResult(toArboSelect, 42);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 42 || resultCode != Activity.RESULT_OK || data == null)
            return;

        Log.d("EdTlist", "Recieved a valid result!");

        int index;
        if (data.hasExtra(RESULT_INDEX)) {
            index = data.getIntExtra(RESULT_INDEX, -1);
        } else {
            index = EdTList.size(); // on a créé un nouveau EdT, on prend le dernier index
        }

        String name;
        if (data.hasExtra(RESULT_NAME)) {
            name = data.getStringExtra(RESULT_NAME);

            if (index < EdTList.size()) {
                EdTList.set(index, name);
            } else {
                EdTList.add(name);
            }

        } else {
            name = EdTList.get(index);
        }


        if (data.hasExtra(RESULT_TYPE)) {
            switch (data.getIntExtra(RESULT_TYPE, 0)) {
                case RESULT_PATH_EDITED:
                    LinkedList<ArboExplorer.PathEntry> path = null;
                    if (data.hasExtra(RESULT_PATH))
                        path = new LinkedList<>((ArrayList<ArboExplorer.PathEntry>) data.getSerializableExtra(RESULT_PATH));

                    pathFile.editEdTAt(index, data.getIntExtra(RESULT_FILE_INDEX, -1), path);

                    Log.d("EdTList", "Edited path at edt " + index + " for path " + data.getIntExtra(RESULT_FILE_INDEX, -1));
                    break;

                case RESULT_PATH_ADDED:
                    if (data.hasExtra(RESULT_PATH)) {
                        LinkedList<ArboExplorer.PathEntry> newPath = new LinkedList<>
                                ((ArrayList<ArboExplorer.PathEntry>) data.getSerializableExtra(RESULT_PATH));

                        pathFile.addPath(newPath, index, name, newPath.getFirst().NOM);

                        Log.d("EdTList", "Added path at edt " + index);
                    }
                    break;

                case RESULT_PATH_REMOVED:
                    pathFile.removePathOfEdTAt(index, data.getIntExtra(RESULT_FILE_INDEX, -1));
                    Log.d("EdTList", "Removed path " + data.getIntExtra(RESULT_FILE_INDEX, -1) + " of edt at " + index);
                    break;
            }

            isPathFileModified = true;
            ((EdTInfoFragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG))
                    .updateData(pathFile.getPathFilesNames(index));
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        Log.d("EdTList", "Pausing activity...");

        if (isPathFileModified) {
            Log.d("EdTList", "Saving paths...");

            pathFile.save();
            saveEdTList();
        }
    }


    private void saveEdTList() {

        Log.d("EdTList", "Saving EdT List...");

        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_KEY_EdT_LIST, MODE_PRIVATE).edit();

        JSONArray storage = new JSONArray();
        for (String str : EdTList) {
            storage.put(str);
        }

        editor.putString(PREFERENCE_KEY_EdT_LIST, storage.toString());

        editor.apply();
    }

    private void getEdTList() {

        Log.d("EdTList", "Loading saved EdT List...");

        String edtListJson = getSharedPreferences(PREFERENCE_KEY_EdT_LIST, MODE_PRIVATE)
                .getString(PREFERENCE_KEY_EdT_LIST, null);

        if (edtListJson == null) {
            if (EdTList != null)
                EdTList.clear();
            else
                EdTList = new LinkedList<>();

        } else {
            EdTList = new LinkedList<>();

            try {
                JSONArray edtJsonArray = new JSONArray(edtListJson);

                for (int i=0;i<edtJsonArray.length();i++) {
                    EdTList.add(edtJsonArray.getString(i));
                }

            } catch (JSONException e) {
                Log.e("EdtList", "An error occured while parsing the stored Json", e);
            }
        }
    }


    public static String[] getEdTNames() {
        return EdTList.toArray(new String[EdTList.size()]);
    }





    // TODO : DEBUG

    public Menu optionsMenu;

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        optionsMenu = menu;

        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                makeAlertDialog("Supprimmer le fichier path ?", 1);

                return true;
            }
        });

        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                makeAlertDialog("Reset la liste des emplois du temps ?", 2);

                return false;
            }
        });

        return true;
    }


    public void makeAlertDialog(String msg, final int thing) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (thing) {
                    case 1:
                        resetPathFile();
                        break;
                    case 2:
                        resetEdTList();
                        break;
                }
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    public void resetEdTList() {
        Log.d("EdTList", "Resetting EdT List...");

        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_KEY_EdT_LIST, MODE_PRIVATE).edit();

        editor.remove(PREFERENCE_KEY_EdT_LIST);

        editor.apply();

        getEdTList();
        listAdapter.updateData();

        isPathFileModified = true;
    }


    public void resetPathFile() {
        pathFile = null;

        File file = new File(getExternalFilesDir(null), PATH_FILE_NAME);

        if (file.delete())
            Log.d("EdTList", "Path file deleted.");
        else
            Log.d("EdTList", "Couldn't delete path file.");

        pathFile = new PathFileExplorer(this);

        isPathFileModified = true;
    }
}
