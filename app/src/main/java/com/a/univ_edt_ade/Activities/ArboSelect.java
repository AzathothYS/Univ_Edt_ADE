package com.a.univ_edt_ade.Activities;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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


    private int height;


    public static String pathDisp = "\\"; // le texte affiché par 'cardRoot', pour nous situer dans l'arborescence
    public static AtomicBoolean didPathChanged = new AtomicBoolean(false);


    private ProgressBar loadingCircle;
    public static AtomicBoolean isLoading = new AtomicBoolean(true);


    private DataLoaderThread DataLoader;
    public final Handler ArboSelectHandler = new Handler(Looper.getMainLooper(), this);
    private Messenger toDataLoader;
    public static final int CUSTOM_MESSAGE = 24,
                            UPDATE_ADAPTER = 1,
                            SET_LOADING = 2,
                            NOTIFY_DATALOADER_HANDLER_INIT = 3,
                            UPDATE_PATH = 4;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayoutToActivity(R.layout.activity_arbo_select);
        setMenuItemChecked(R.id.Menu_arboSelect);


        ArboStorage.setFile(this); // il faut un 'Context' pour que 'ArboStorage' puisse obtenir son fichier
        initDataLoaderThread();


        final RecyclerView arboRList = (RecyclerView) findViewById(R.id.arboRList);
        arboRList.setHasFixedSize(true);

        RecyclerView.LayoutManager arboLayoutM = new LinearLayoutManager(this);
        arboRList.setLayoutManager(arboLayoutM);

        listAdapter = new ArboCardAdapter(arboItemList);
        listAdapter.setHasStableIds(false);
        arboRList.setAdapter(listAdapter);


        Point dimens = new Point(0,0);
        this.getWindowManager().getDefaultDisplay().getSize(dimens);
        height = dimens.y;

        arboRList.post(new Runnable() {
            @Override
            public void run() {
                if (arboRList.getHeight() < height) // TODO : faire prendre en compte la hauteur de la cardView Root, et à mettre à jour après chaque avancée dans l'arborescence
                    arboRList.setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        });

        initLoadingCircleUpdater();
        initCardPathUpdater();
    }

    private void initLoadingCircleUpdater() {
        loadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

        final Handler LoadingCircleThread = new Handler();
        final Runnable LoadingCircleUpdater = new Runnable() {
            @Override
            public void run() {
                if (isLoading.get()) {
                    if (loadingCircle.getVisibility() == View.GONE) {
                        loadingCircle.setVisibility(View.VISIBLE);
                    }

                } else {
                    if (loadingCircle.getVisibility() == View.VISIBLE) {
                        loadingCircle.setVisibility(View.GONE);
                    }
                }

                LoadingCircleThread.postDelayed(this, 20);
            }
        };
        LoadingCircleThread.postDelayed(LoadingCircleUpdater, 20);
    }

    private void initCardPathUpdater() {

        final CardView cardRoot = (CardView) findViewById(R.id.card_root);

        cardRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on est allé dans le dossier parent
                /*updateData();
                listAdapter.setNewData();

                pathDisp = "\\" + ArboExplorer.getPath();
                didPathChanged.set(true);*/
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

        final Runnable pathUpdater = new Runnable() {
            @Override
            public void run() {
                if (didPathChanged.get()) {
                    Log.d("pathUpdater", "Setting text of 'cardRoot' to : '" + pathDisp + "'");
                    ((TextView) cardRoot.getChildAt(1)).setText(pathDisp);
                    didPathChanged.set(false);
                }

                cardRoot.postDelayed(this, 20);
            }
        };
        cardRoot.postDelayed(pathUpdater, 20);
    }

    private void initDataLoaderThread() {
        DataLoader = new DataLoaderThread(ArboSelectHandler.obtainMessage(CUSTOM_MESSAGE));
        DataLoader.start(); // initialise le thread et 'ArboExplorer'
    }


    @Override
    public void onStop() {
        super.onStop();

        DataLoader.shutdown();
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CUSTOM_MESSAGE:
                break;

            default:
                return false;
        }

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

            case SET_LOADING:
                if (msg.arg2 == 0)
                    isLoading.set(false);
                else if (msg.arg2 == 1)
                    isLoading.set(true);
                break;

            case NOTIFY_DATALOADER_HANDLER_INIT:
                // le thread DataLoader nous notifie que son handler a été initialisé
                toDataLoader = new Messenger(DataLoaderThread.DataLoaderHandler);
                listAdapter.setMessenger();
                break;
        }

        return true;
    }
}
