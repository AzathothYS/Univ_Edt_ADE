package com.a.univ_edt_ade.Activities;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.a.univ_edt_ade.CustomsAssets.ArboCardAdapter;
import com.a.univ_edt_ade.R;
import com.a.univ_edt_ade.CustomsAssets.arboRListAdapter;

public class ArboSelect extends BaseActivity {

    private static String[] arboItemList = new String[8];
    private static boolean[] isItemAFolder = new boolean[8];

    static {
        int i = 0;
        arboItemList[i++] = "Etudiants";    isItemAFolder[i] = true;
        arboItemList[i++] = "groupe 42";    isItemAFolder[i] = false;
        arboItemList[i++] = "Enseigants";   isItemAFolder[i] = true;
        arboItemList[i++] = "Salles";       isItemAFolder[i] = true;
        arboItemList[i++] = "groupe 69";    isItemAFolder[i] = false;
        arboItemList[i++] = "Equipements";  isItemAFolder[i] = true;
        arboItemList[i++] = "Autres";       isItemAFolder[i] = true;
        arboItemList[i] = "dodo";           isItemAFolder[i] = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLayoutToActivity(R.layout.activity_arbo_select);
        setMenuItemChecked(R.id.Menu_arboSelect);

        RecyclerView arboRList = (RecyclerView) findViewById(R.id.arboRList);
        arboRList.setHasFixedSize(true);

        RecyclerView.LayoutManager arboLayoutM = new LinearLayoutManager(this);
        arboRList.setLayoutManager(arboLayoutM);

        ArboCardAdapter listAdapter = new ArboCardAdapter(arboItemList, isItemAFolder);

        arboRList.setAdapter(listAdapter);

    }
}
