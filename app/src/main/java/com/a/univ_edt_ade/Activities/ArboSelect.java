package com.a.univ_edt_ade.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.a.univ_edt_ade.ArboFile.ArboStorage;
import com.a.univ_edt_ade.ArboFile.DataLoaderThread;
import com.a.univ_edt_ade.CustomsAssets.ArboCardAdapter;
import com.a.univ_edt_ade.R;

import java.util.concurrent.atomic.AtomicBoolean;

public class ArboSelect extends BaseActivity implements Handler.Callback {

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
                            EMPTY_FILE_ERROR = 4;

    private Boolean running = true; // désactive les 'threads' lorsque mis à false


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayoutToActivity(R.layout.activity_arbo_select);
        setMenuItemChecked(R.id.Menu_arboSelect);


        ArboStorage.setFile(this); // il faut un 'Context' pour que 'ArboStorage' puisse obtenir son fichier
        initDataLoaderThread();


        final RecyclerView arboRList = (RecyclerView) findViewById(R.id.arboRList);
        arboRList.setHasFixedSize(true);
        arboRList.setOverScrollMode(View.OVER_SCROLL_NEVER);


        RecyclerView.LayoutManager arboLayoutM = new LinearLayoutManager(this);
        arboRList.setLayoutManager(arboLayoutM);

        listAdapter = new ArboCardAdapter(arboItemList);
        listAdapter.setHasStableIds(false);
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

    /**
     * L'activité est stoppée, on ferme le DataLoader Thread
     */
    @Override
    public void onStop() {
        super.onStop();

        running = false;

        DataLoader.shutdown();
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
     * 'EMPTY_FILE_ERROR' : le dossier où l'on vouleit aller est vide
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
                listAdapter.setMessenger();
                break;

            case EMPTY_FILE_ERROR:
                // on a tenté d'ouvrir un dossier vide
                Log.d("ArboSelect", "Dossier vide!");
                Snackbar.make(findViewById(R.id.arboRList), "Dossier vide", Snackbar.LENGTH_SHORT).show();
                isLoading.set(false);
                break;
        }

        return true;
    }
}
