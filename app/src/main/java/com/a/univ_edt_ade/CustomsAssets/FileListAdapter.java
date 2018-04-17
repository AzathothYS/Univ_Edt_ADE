package com.a.univ_edt_ade.CustomsAssets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.a.univ_edt_ade.ArboFile.ArboExplorer;
import com.a.univ_edt_ade.R;

import java.util.LinkedList;

/**
 * Created by 7 on 21/01/2018.
 */

public class FileListAdapter extends FancyListAdapter {

    private final int EdTpos;
    private String[] fileList;
    private final FileListCallback activityCallback;

    public FileListAdapter(String[] data, int position, Context context) {
        super(R.layout.file_select_list_card_item, R.drawable.edt_list_card_background);

        activityCallback = (FileListCallback) context;

        fileList = data;

        Log.d("FileListAdapter", "Created with file list length : " + fileList.length);

        EdTpos = position;
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (position < fileList.length) {
            ((TextView) holder.cardView.getChildAt(1)).setText(fileList[position]);

        } else {
            ((TextView) holder.cardView.getChildAt(1)).setText("Ajouter un fichier...");
        }
    }


    @Override
    public void itemOnClick(View v, int adapterPostion) {
        if (adapterPostion < fileList.length) {
            activityCallback.launchNewActivity(adapterPostion, EdTInfoFragment.titleString,
                    activityCallback.getPathAt(EdTpos, adapterPostion));
        } else {
            activityCallback.launchNewActivity(adapterPostion, EdTInfoFragment.titleString, null);
        }
    }

    @Override
    public void trashViewOnClick(View v, int adapterPosition) {
        activityCallback.deletePathAt(EdTpos, adapterPosition, fileList[adapterPosition]);
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
        void deletePathAt(int EdTpos, int position, String name);

        void launchNewActivity(int position, String name, @Nullable LinkedList<ArboExplorer.PathEntry> initialPath);

        LinkedList<ArboExplorer.PathEntry> getPathAt(int EdTIndex, int position);
    }
}
