package com.a.univ_edt_ade.ArboFile;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.a.univ_edt_ade.Activities.ArboSelect;

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
                            MESSAGE_UPDATE_DATA = 1,
                            MESSAGE_CHILD_CLICKED = 2,
                            MESSAGE_GOTO_PARENT = 3;

    private boolean running = true;
    private final Object mutex = new Object();


    public DataLoaderThread(Message msg) {
        toMainThread = new Messenger(msg.getTarget());
    }


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


    public void shutdown() {
        running = false;

        synchronized (mutex) {
            mutex.notifyAll();
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CUSTOM_MESSAGE:
                break;

            default:
                return false;
        }


        String[] output = null;
        String newPath = null;


        switch (msg.arg1) {
            case MESSAGE_UPDATE_DATA:
                ArboSelect.isLoading.set(true);

                output = ArboStorage.arbo.obtainFolderContents();

                break;

            case MESSAGE_CHILD_CLICKED:
                ArboSelect.isLoading.set(true);

                ArboStorage.arbo.goIntoChildFolder(msg.arg2);
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
        }

        if (output != null)
            sendSimpleMsg(ArboSelect.UPDATE_ADAPTER, output);

        if (newPath != null)
            sendSimpleMsg(ArboSelect.UPDATE_PATH, newPath);

        return true;
    }

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
