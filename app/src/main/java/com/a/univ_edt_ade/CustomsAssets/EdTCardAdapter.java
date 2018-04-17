package com.a.univ_edt_ade.CustomsAssets;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.a.univ_edt_ade.R;

/**
 * Created by 7 on 14/01/2018.
 */

public class EdTCardAdapter extends FancyListAdapter {

    private String[] EdTList;

    public EdTCardAdapter(String[] EdTList) {
        super(R.layout.edt_list_card, R.drawable.edt_list_card_background);

        this.EdTList = EdTList;
    }


    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (position < EdTList.length) {
            ((TextView) holder.cardView.getChildAt(1)).setText(EdTList[position]);

        } else {
            // la cardView supplémentaire utilisée pour rajouter un nouvel emploi du temps
            ((TextView) holder.cardView.getChildAt(1)).setText("Nouveau...");
        }
    }


    @Override
    public void itemOnClick(View v, int adapterPostion) {
        ((edtAdapterCallback) v.getContext())
                .makeEdTInfoFrag(adapterPostion,
                        adapterPostion < EdTList.length ? EdTList[adapterPostion] : "Nouvel Emploi du Temps");
    }

    @Override
    public void trashViewOnClick(View v, int adapterPosition) {
        ((edtAdapterCallback) v.getContext()).deleteEdTAt(adapterPosition);
    }


    @Override
    public int getItemCount() {
        return EdTList.length + 1;
    }


    public void updateData() {
        Log.d("EdTCardAdapter", "Updating data...");

        EdTList = com.a.univ_edt_ade.Activities.EdTList.getEdTNames();

        notifyDataSetChanged();
    }


    public interface edtAdapterCallback {
        void makeEdTInfoFrag(int pos, String name);

        void deleteEdTAt(int pos);
    }
}
