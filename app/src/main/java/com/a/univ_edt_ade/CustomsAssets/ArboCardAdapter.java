package com.a.univ_edt_ade.CustomsAssets;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.a.univ_edt_ade.Activities.ArboSelect;
import com.a.univ_edt_ade.ArboFile.DataLoaderThread;
import com.a.univ_edt_ade.R;

/**
 * Adapter pour le contenu de 'ArboSelect'
 * Chaque item est une 'CardView' avec une image dépendant du type de l'objet, si c'est un fichier ou
 * un dossier.
 */
public class ArboCardAdapter extends RecyclerView.Adapter<ArboCardAdapter.ViewHolder> {

    private String[] names;
    public Messenger toDataHolder;
    public Messenger toMainThread;

    private int selectedIndex;
    public int selectedFolderLine;
    public boolean areWeInTheSelectedItemsFolder = false;

    public ArboCardAdapter(String[] names) {
        this.names = names;
    }

    public void setMessenger(Handler ArboSelectHandler) {
        this.toDataHolder = new Messenger(DataLoaderThread.DataLoaderHandler);

        this.toMainThread = new Messenger(ArboSelectHandler);
    }

    public void setSelectedItem(int index, int line) {
        selectedIndex = index;
        selectedFolderLine = line;
        areWeInTheSelectedItemsFolder = true;
    }

    /**
     * Le ViewHolder utilisé pour tous les objets de la liste
     * Comme ils sont réutilisés à travers les différentes itérations, on garde en mémoire si le VH
     * était un VH d'un dossier ou fichier, pour savoir si on a besoin de changer l'image lorsque
     * la base de données est mise à jour
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        public boolean isFolder;
        public ViewHolder(CardView view) {
            super(view);
            cardView = view;
            isFolder = true;
        }
    }

    @Override
    public ArboCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.arbo_card_item, parent, false);

        return new ViewHolder(cardView);
    }

    /**
     * Initialisation d'un VH
     * On change l'image de la cardView si le type du VH (dossier ou fichier) est différent du
     * précédent.
     * Si c'est un dossier on lui rajoute un OnClickListener qui va envoyer un message au
     * DataLoaderThread pour aller dans ce dossier
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (names[position].contains("__")) {
            // le nom du fichier est stocké avec un '__' au début, on l'enlève avant de l'afficher
            ((TextView) holder.cardView.getChildAt(1)).setText(names[position].substring(3));

            if (holder.isFolder) {
                // c'était un dossier, on change l'image en celle du fichier
                ((ImageView) holder.cardView.getChildAt(0)).setImageResource(R.drawable.ic_description_black_24dp);

                holder.isFolder = false;
            }

        } else {
            // c'est un dossier
            ((TextView) holder.cardView.getChildAt(1)).setText(names[position]);

            if (!holder.isFolder) {
                // ce holder était un fichier précédament, on change l'image en celle du dossier
                ((ImageView) holder.cardView.getChildAt(0)).setImageResource(R.drawable.ic_folder_black_24dp);

                holder.isFolder = true;
            }
        }

        // on reset le listener à chaque fois
        holder.cardView.setOnClickListener(
                new ItemOnClickListener(
                        holder.getAdapterPosition(),
                        holder.isFolder,
                        holder.isFolder ? null : names[position]));

        if (areWeInTheSelectedItemsFolder && position == selectedIndex) {
            holder.cardView.setBackgroundColor(0x6800badf);
            holder.setIsRecyclable(false);
        }
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    public void setNewData() {
        names = ArboSelect.arboItemList;

        if (areWeInTheSelectedItemsFolder)
            areWeInTheSelectedItemsFolder = false;

        Log.d("ArboAdapter", "New data set : " + names.length);

        notifyDataSetChanged();
    }


    private class ItemOnClickListener implements View.OnClickListener {

        private final int adapterPosition;
        private boolean isFolder = true;

        public String name = ""; // utilisé uniquement pour les fichiers

        public ItemOnClickListener(int adapterPosition, boolean isFolder, @Nullable String name) {
            this.adapterPosition = adapterPosition;
            this.isFolder = isFolder;
            this.name = name;
        }

        @Override
        public void onClick(View v) {

            Log.d("recylCardView", "Touched cardView " + (isFolder ? "(folder)" : "(file)" ) + " at " + adapterPosition);

            final Message msg = Message.obtain();
            msg.what = isFolder ? DataLoaderThread.CUSTOM_MESSAGE : ArboSelect.CUSTOM_MESSAGE;
            msg.arg1 = isFolder ? DataLoaderThread.MESSAGE_CHILD_CLICKED : ArboSelect.FILE_CLICKED;
            msg.arg2 = adapterPosition;

            if (!isFolder)
                msg.obj = name;

            try {
                if (isFolder)
                    toDataHolder.send(msg);
                else
                    toMainThread.send(msg);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
