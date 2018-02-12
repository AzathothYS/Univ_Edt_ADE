package com.a.univ_edt_ade.ArboFile;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.a.univ_edt_ade.Activities.ArboSelect;

import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Thread dont la tâche consiste à assurer le lien entre le fichier de l'arborescence d'ADE et
 * 'ArboSelect" qui affiche le contenu du fichier dans une recycler list.
 * Comme le fichier en fait plusieurs dizaines de milliers de lignes, on effectue toutes les
 * opérations de lecture dans ce thread.
 *
 * Basé sur : http://stephendnicholas.com/posts/android-handlerthread
 */

public class DataLoaderThread extends Thread implements Handler.Callback {

    public static Handler DataLoaderHandler;
    private Messenger toMainThread;
    public static final int CUSTOM_MESSAGE = 42,
                            MESSAGE_CHILD_CLICKED = 1,
                            MESSAGE_INIT_PATH = 2,
                            MESSAGE_GOTO_PARENT = 3,
                            MESSAGE_FILE_CLICKED = 4;

    private boolean running = true;
    private final Object mutex = new Object();


    public DataLoaderThread(Message msg) {
        toMainThread = new Messenger(msg.getTarget());
    }

    /**
     * On initialise le nouveau Thread, on notifie le Thread parent de l'initialisation, puis
     * on initialise l'Arborescence et on notifie encore le Thread parent
     */
    @Override
    public void run() {
        HandlerThread thread = new HandlerThread("DataLoader");
        thread.start();

        DataLoaderHandler = new Handler(thread.getLooper(), this);

        sendSimpleMsg(ArboSelect.NOTIFY_DATALOADER_HANDLER_INIT, null);


        if (ArboStorage.arbo == null) {
            // l'arborescence n'a pas été initialisée
            ArboStorage.setArborescence();

            DataLoaderHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("DataLoader", "Sending message...");
                    String[] output = ArboStorage.arbo.obtainFolderContents();
                    sendSimpleMsg(ArboSelect.UPDATE_ADAPTER, output);
                }
            }, 10);
        }

        while (running) {
            synchronized (mutex) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {}
            }
        }

        thread.quit();
    }

    /**
     * Appelée lorsque le Thread parent s'arrête
     * On ferme alors ce Thread proprement
     */
    public void shutdown() {
        running = false;

        synchronized (mutex) {
            mutex.notifyAll();
        }
    }

    /**
     * Un autre Thread nous a envoyé un mesage, si ce n'est pas un message pour s'occuper de
     * l'arborescence (tag CUSTOM_MESSAGE), alors on l'ignore
     *
     * 'arg1' du message est le type d'action que l'on veut faire :
     *
     * 'CHILD_CLICKED' est appelé par un dossier d'un VH de 'ArboCardAdapter', on déplace le
     * pointeur de l'arborescence dans le n-ième (msg.arg2) enfant du dossier parent
     *
     * 'INIT_PATh' est appelé lorsque le ArboSelect et le DataLoader sont initialisés, on suit alors
     * ce chemin initial pour que ce dossier soit
     *
     * 'GOTO_PARENT' est appelé par 'cardRoot' dans 'ArboSelect' lorsque l'on veut aller dans le
     * dossier parent
     *
     * On retourne alors (si on a fati quelque chose) au Thread parent le nouveau path et le contenu
     * du nouveau dossier
     */
    @Override
    public boolean handleMessage(Message msg) {
        if (!(msg.what == CUSTOM_MESSAGE))
            return false;

        String[] output = null;
        String newPath = null;

        switch (msg.arg1) {
            case MESSAGE_CHILD_CLICKED:
                ArboSelect.isLoading.set(true);

                if (!ArboStorage.arbo.goIntoChildFolder(msg.arg2)) {
                    // le nouveau dossier est vide
                    sendSimpleMsg(ArboSelect.EMPTY_FILE_ERROR, null);
                    break; // on ne fait rien d'autre
                }

                output = ArboStorage.arbo.obtainFolderContents();

                newPath = "\\" + ArboExplorer.getPath();

                break;

            case MESSAGE_INIT_PATH:
                ArboSelect.isLoading.set(true);

                Log.d("DataLoader", "Following initial path...");

                ArboStorage.arbo.followPath((LinkedList<ArboExplorer.PathEntry>) msg.obj);

                output = ArboStorage.arbo.obtainFolderContents();

                newPath = "\\" + ArboExplorer.getPath();

                break;

            case MESSAGE_GOTO_PARENT:

                if (ArboStorage.arbo.goToParentFolder()) {
                    ArboSelect.isLoading.set(true);

                    // on est allé dans le dossier parent, sinon on y était déjà, et on ne fait rien

                    output = ArboStorage.arbo.obtainFolderContents();

                    newPath = "\\" + ArboExplorer.getPath();

                } else {
                    Log.d("DataLoader", "Nous sommes déjà dans le dossier root.");
                }

                break;

            case MESSAGE_FILE_CLICKED:

                Log.d("DataLoader", "Fichier n°" + msg.arg2 + " sélectionné");

                LinkedList<ArboExplorer.PathEntry> pathOutput = ArboStorage.arbo.selectChildFile(msg.arg2);

                sendSimpleMsg(ArboSelect.FILE_CLICKED_OUTPUT, pathOutput);

                break;
        }

        if (output != null)
            sendSimpleMsg(ArboSelect.UPDATE_ADAPTER, output);

        if (newPath != null)
            sendSimpleMsg(ArboSelect.UPDATE_PATH, newPath);

        return true;
    }

    /**
     * Envoie un message au Thread parent
     */
    private void sendSimpleMsg(int arg, @Nullable Object obj) {
        // on envoie un message demandant de mettre à jour l'adapter de la liste
        final Message msg = Message.obtain();
        msg.what = ArboSelect.CUSTOM_MESSAGE;
        msg.arg1 = arg;

        if (obj != null)
            msg.obj = obj;

        try {
            toMainThread.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
