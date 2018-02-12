package com.a.univ_edt_ade.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.a.univ_edt_ade.ArboFile.ArboExplorer;
import com.a.univ_edt_ade.ArboFile.ArboStorage;
import com.a.univ_edt_ade.ArboFile.DataLoaderThread;
import com.a.univ_edt_ade.CustomsAssets.ArboCardAdapter;
import com.a.univ_edt_ade.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArboSelect extends BaseActivity implements Handler.Callback {

    public String EdTFileName = "";
    private int EdTPos;

    private LinkedList<ArboExplorer.PathEntry> initialPath = null;

    public static String[] arboItemList = new String[0];
    private ArboCardAdapter listAdapter;


    public static String pathDisp = "\\"; // le texte affiché par 'cardRoot', pour nous situer dans l'arborescence
    public static AtomicBoolean didPathChanged = new AtomicBoolean(false);


    private ProgressBar loadingCircle;
    public static AtomicBoolean isLoading = new AtomicBoolean(true);


    private DataLoaderThread DataLoader;
    public final Handler ArboSelectHandler = new Handler(Looper.getMainLooper(), this);
    private Messenger toDataLoader;
    public static final int CUSTOM_MESSAGE = 24,
                            UPDATE_ADAPTER = 1,
                            NOTIFY_DATALOADER_HANDLER_INIT = 2,
                            UPDATE_PATH = 3,
                            EMPTY_FILE_ERROR = 4,
                            FILE_CLICKED = 5,
                            FILE_CLICKED_OUTPUT = 6;

    private Boolean running = true; // désactive les 'threads' lorsque mis à false


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayoutToActivity(R.layout.activity_arbo_select);
        setMenuItemChecked(R.id.Menu_arboSelect);


        EdTFileName = getIntent().getStringExtra("EdTName");
        EdTPos = getIntent().getIntExtra("EdTPos", -1);

        if (getIntent().hasExtra("initialPath")) {
            try {
                JSONArray initialPathJson = new JSONArray(getIntent().getStringExtra("initialPath"));

                initialPath = new LinkedList<>();

                ArboExplorer.PathEntry pathEntry;
                for (int i=0;i<initialPathJson.length();i+=3) {
                    pathEntry = new ArboExplorer.PathEntry(
                            initialPathJson.getInt(i),
                            initialPathJson.getInt(i + 1),
                            initialPathJson.getString(i + 2));

                    initialPath.add(pathEntry);
                }

            } catch (JSONException e) {
                Log.e("ArboSelect", "An error occured while getting the stored path", e);
            }
        }


        // on force le fait que l'activité s'affiche en mode portrait
        if (Build.VERSION.SDK_INT >= 18)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        ArboStorage.setFile(this); // il faut un 'Context' pour que 'ArboStorage' puisse obtenir son fichier
        initDataLoaderThread();


        final RecyclerView arboRList = (RecyclerView) findViewById(R.id.arboRList);
        arboRList.setHasFixedSize(true);
        arboRList.setOverScrollMode(View.OVER_SCROLL_NEVER);

        RecyclerView.LayoutManager arboLayoutM = new LinearLayoutManager(this);
        arboRList.setLayoutManager(arboLayoutM);

        listAdapter = new ArboCardAdapter(arboItemList);
        listAdapter.setHasStableIds(false); // TODO : utile ???

        if (initialPath != null) {
            listAdapter.setSelectedItem(
                    initialPath.getFirst().INDEX,
                    initialPath.get(initialPath.size() - 1).LIGNE); // la ligne du dossier dans lequel se trouve la cible du path
        }

        arboRList.setAdapter(listAdapter);


        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("path")) {
                Log.d("ArboSelect", "Loaded path");
                pathDisp = savedInstanceState.getString("path");
            }
        }

        initLoadingCircleUpdater();
        initCardPathUpdater();
    }

    /**
     * Initialise le 'loadingCircle', le cercle qui tourne lorsque ça charge
     * Pour savoir si ça charge on utilise 'isLoading'
     * Un runnable mettant à jour la visibilité du cercle est relancé toutes les 20ms, tant que
     * 'running' est vrai
     */
    private void initLoadingCircleUpdater() {
        loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

        final Runnable LoadingCircleUpdater = new Runnable() {
            @Override
            public void run() {
                if (!running)
                    return;

                if (isLoading.get()) {
                    if (loadingCircle.getVisibility() == View.GONE) {
                        loadingCircle.setVisibility(View.VISIBLE);
                    }

                } else {
                    if (loadingCircle.getVisibility() == View.VISIBLE) {
                        loadingCircle.setVisibility(View.GONE);
                    }
                }

                loadingCircle.postDelayed(this, 20);
            }
        };
        loadingCircle.postDelayed(LoadingCircleUpdater, 20);
    }

    /**
     * Initialise 'cardRoot' : ajoute un OnClickListener pour aller dans le dossier parent,
     * éxécute un Runnable toutes les 20ms pour mettre à jour le path affiché
     */
    private void initCardPathUpdater() {

        final CardView cardRoot = (CardView) findViewById(R.id.card_root);

        if (!cardRoot.hasOnClickListeners()) {
            cardRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Message msg = Message.obtain();
                    msg.what = DataLoaderThread.CUSTOM_MESSAGE;
                    msg.arg1 = DataLoaderThread.MESSAGE_GOTO_PARENT;

                    try {
                        toDataLoader.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        final Runnable pathUpdater = new Runnable() {
            @Override
            public void run() {
                if (!running)
                    return;

                if (didPathChanged.get()) {
                    Log.d("pathUpdater", "Setting text of 'cardRoot' to : '" + pathDisp + "' - for : " + this.toString());
                    ((TextView) cardRoot.getChildAt(1)).setText(pathDisp); // TODO : voir pour un meilleur moyen de couper le texte

                    didPathChanged.set(false);
                }

                cardRoot.postDelayed(this, 20);
            }
        };
        cardRoot.postDelayed(pathUpdater, 20);


        if (!pathDisp.equals("\\"))
            didPathChanged.set(true);
    }

    /**
     * Initialise le DataLoader
     */
    private void initDataLoaderThread() {
        DataLoader = new DataLoaderThread(ArboSelectHandler.obtainMessage(CUSTOM_MESSAGE));
        DataLoader.start(); // initialise le thread et 'ArboExplorer'
    }


    @Override
    public void onBackPressed() {
        if (menuDrawer.isDrawerOpen(GravityCompat.START)) {
            // on ferme d'abord le menu si il est ouvert
            menuDrawer.closeDrawer(GravityCompat.START);

        } else if (ArboStorage.arbo.getPathLength() > 0) { // TODO : vérifier que c'est Thread-Safe
            // on va dans le dossier parent, comme on n'est pas dans le dossier root
            final Message msg = Message.obtain();
            msg.what = DataLoaderThread.CUSTOM_MESSAGE;
            msg.arg1 = DataLoaderThread.MESSAGE_GOTO_PARENT;

            try {
                toDataLoader.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else {
            // TODO : faire un message de confirmation avant d'annuler le choix du fichier <- est-ce vraiment nécesaire???? (non)
            super.onBackPressed();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (initialPath != null) {
            final RecyclerView arboRList = (RecyclerView) findViewById(R.id.arboRList);

            Point dims = new Point();
            getWindowManager().getDefaultDisplay().getSize(dims);

            ((LinearLayoutManager) arboRList.getLayoutManager()).scrollToPositionWithOffset(initialPath.getFirst().INDEX, dims.y >> 1);
            //arboRList.scrollToPosition(initialPath.getFirst().INDEX);
        }
    }

    /**
     * L'activité est stoppée, on ferme le DataLoader Thread
     */
    @Override
    public void onStop() {
        super.onStop();

        running = false;

        DataLoader.shutdown();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (DataLoader.isAlive())
            DataLoader.shutdown();

        if (running)
            running = false;
    }

    /**
     * Sauvegarde le path actuel, pour la prochaine ouverture de l'activité
     * Comme l'arborescence se trouve dans un singleton, la position est conservée, on ne
     * sauvegarde que le path par simplicité
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("path", pathDisp);
        super.onSaveInstanceState(outState);
    }

    /**
     * Re-initialise le DataLoaderThread, le CircleUpdater et le CardPathUpdater après que l'activité soit stoppée
     */
    @Override
    public void onRestart() {
        super.onRestart();

        running = true;

        // tous les threads on étés tués
        initDataLoaderThread();
        initCardPathUpdater();
        initLoadingCircleUpdater();
    }

    /**
     * Utilisé pour communiquer avec le DataLoaderThread, qui s'occupe des opérations dans le fichier
     * de l'arborescence
     * Si 'msg.what' ne vaut pas 'CUSTOM_MESSAGE', alors le message est ignoré
     *
     * 'msg.arg1' contient l'action à réaliser :
     * 'UPDATE_ADAPTER' : on met à jour les données de la recyclerView
     *
     * 'UPDATE_PATH' : on met à jour le path et on le notifie à 'cardRoot' via 'didPathChanged'
     *
     * 'NOTIFY_DATALOADER_HANDLER_INIT' : on peut initialiser le Messenger utilisé pour communiquer
     * avec le DataLoader
     *
     * 'EMPTY_FILE_ERROR' : le dossier où l'on voulait aller est vide
     */
    @Override
    public boolean handleMessage(Message msg) {
        if (!(msg.what == CUSTOM_MESSAGE))
            return false;

        switch (msg.arg1) {
            case UPDATE_ADAPTER:
                if (msg.obj != null) {
                    arboItemList = (String[]) msg.obj;

                    listAdapter.setNewData(); // autorisé seulement dans ce Thread

                    if (listAdapter.selectedFolderLine == ArboStorage.arbo.getLineNb()) { // TODO : idem que plus haut, vérifier si c'est Thread-safe (théoriquement, oui)
                        listAdapter.areWeInTheSelectedItemsFolder = true;
                    }
                }

                isLoading.set(false);
                break;

            case UPDATE_PATH:
                if (msg.obj != null) {
                    pathDisp = (String) msg.obj;
                    didPathChanged.set(true);
                }
                break;

            case NOTIFY_DATALOADER_HANDLER_INIT:
                // le thread DataLoader nous notifie que son handler a été initialisé
                toDataLoader = new Messenger(DataLoaderThread.DataLoaderHandler);
                listAdapter.setMessenger(ArboSelectHandler);

                if (initialPath != null) {
                    // on a un path initial à suivre

                    final Message message = Message.obtain();
                    message.what = DataLoaderThread.CUSTOM_MESSAGE;
                    message.arg1 = DataLoaderThread.MESSAGE_INIT_PATH;

                    message.obj = initialPath;

                    try {
                        toDataLoader.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                break;

            case EMPTY_FILE_ERROR:
                // on a tenté d'ouvrir un dossier vide
                Log.d("ArboSelect", "Dossier vide!");
                Snackbar.make(findViewById(R.id.arboRList), "Dossier vide", Snackbar.LENGTH_SHORT).show();
                isLoading.set(false);
                break;

            case FILE_CLICKED:
                // on veut sélectionner un fichier
                Log.d("ArboSelect", "Fichier cliqué!");

                popupWindow(msg.arg2, (String) msg.obj);

                break;

            case FILE_CLICKED_OUTPUT:
                // on a sélectionné un fichier, et le DataLoader nous retourne le path vers ce fichier
                isLoading.set(false);

                if (msg.obj != null) {
                    Log.d("ArboSelect","DataLoader returned a file output!");

                    Intent returnIntent = new Intent();
                    Bundle path = new Bundle();
                    path.putSerializable(EdTList.RESULT_PATH, (LinkedList<ArboExplorer.PathEntry>) msg.obj);
                    returnIntent.putExtras(path);

                    returnIntent.putExtra(EdTList.RESULT_NAME, EdTFileName);
                    returnIntent.putExtra(EdTList.RESULT_TYPE, EdTList.RESULT_PATH_ADDED);
                    returnIntent.putExtra(EdTList.RESULT_INDEX, EdTPos);

                    setResult(Activity.RESULT_OK, returnIntent);

                    Log.d("ArboSelect", "Finishing activity...");

                    finish();

                } else {
                    Log.d("ArboSelect", "The file selection returned nothing");
                }

                break;
        }

        return true;
    }



    private void popupWindow(final int adapterPosition, String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(R.layout.file_select_dialog);
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final Message msg = Message.obtain();
                        msg.what = DataLoaderThread.CUSTOM_MESSAGE;
                        msg.arg1 = DataLoaderThread.MESSAGE_FILE_CLICKED;
                        msg.arg2 = adapterPosition;

                        try {
                            toDataLoader.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        isLoading.set(true);

                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        fileName = fileName.replaceFirst("__", ""); // on enlève la marque des fichiers ('__')

        ((TextView) dialog.findViewById(R.id.text_dialog_File)).setText(fileName);
        ((TextView) dialog.findViewById(R.id.text_dialog_Title)).setText(EdTFileName);
    }
}
