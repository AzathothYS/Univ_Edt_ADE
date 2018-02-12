package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.a.univ_edt_ade.ArboFile.ArboExplorer;
import com.a.univ_edt_ade.R;

import java.util.LinkedList;

/**
 * Created by 7 on 21/01/2018.
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileItemHolder> {

    private final int EdTpos;
    private String[] fileList;
    private final FileListCallback activityCallback;

    public FileListAdapter(String[] data, int position, Context context) {
        activityCallback = (FileListCallback) context;

        fileList = data;

        Log.d("FileListAdapter", "Created with file list length : " + fileList.length);

        EdTpos = position;
    }

    public class FileItemHolder extends RecyclerView.ViewHolder {
        public CardView cardView;

        public FileItemHolder(CardView v) {
            super(v);

            cardView = v;
        }
    }

    @Override
    public FileItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.file_select_list_card_item, parent, false);

        return new FileItemHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final FileItemHolder holder, int position) {

        if (position < fileList.length) {
            ((TextView) holder.cardView.getChildAt(1)).setText(fileList[position]);

        } else {
            ((ImageView) holder.cardView.getChildAt(0)).setImageResource(R.drawable.ic_add_24dp);
            ((TextView) holder.cardView.getChildAt(1)).setText("Ajouter un fichier...");
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = holder.getAdapterPosition();

                if (pos < fileList.length) {
                    activityCallback.launchNewActivity(pos, EdTInfoFragment.titleString,
                            activityCallback.getPathAt(EdTpos, pos));
                } else {
                    activityCallback.launchNewActivity(pos, "New", null);
                }
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Log.d("FileListAdapter", "stop click");

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.length + 1;
    }


    public void updateData(String[] data) {
        fileList = data;

        notifyDataSetChanged();
    }


    public interface FileListCallback {
        void launchNewActivity(int position, String name, @Nullable LinkedList<ArboExplorer.PathEntry> initialPath);

        LinkedList<ArboExplorer.PathEntry> getPathAt(int EdTIndex, int position);
    }
}
