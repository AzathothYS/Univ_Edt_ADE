package com.a.univ_edt_ade.CustomsAssets;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.a.univ_edt_ade.R;

/**
 * Created by 7 on 13/12/2017.
 */

public class ArboCardAdapter extends RecyclerView.Adapter<ArboCardAdapter.ViewHolder> {

    private String[] names;
    private boolean[] isFolder;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        public ViewHolder(CardView view) {
            super(view);
            cardView = view;

        }
    }

    public ArboCardAdapter(String[] names, boolean[] isFolder) {
        this.names = names;
        this.isFolder = isFolder;
    }

    @Override
    public ArboCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.arbo_card_item, parent, false);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (!isFolder[position]) // c'est un fichier, on change l'image en celle du fichier
            ((ImageView) holder.cardView.getChildAt(0)).setImageResource(R.drawable.ic_description_black_24dp);

        ((TextView) holder.cardView.getChildAt(1)).setText(names[position]);
    }

    @Override
    public int getItemCount() {
        return names.length;
    }
}
