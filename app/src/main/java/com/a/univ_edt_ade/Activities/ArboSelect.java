package com.a.univ_edt_ade.Activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.a.univ_edt_ade.R;
import com.a.univ_edt_ade.CustomsAssets.arboRListAdapter;

public class ArboSelect extends BaseActivity {

    private static String[] arboItemList= new String[5];

    static {
        arboItemList[0] = "Etudiants";
        arboItemList[1] = "Enseigants";
        arboItemList[2] = "Salles";
        arboItemList[3] = "Equipements";
        arboItemList[4] = "Autres";
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

        arboRListAdapter listAdapter = new arboRListAdapter(arboItemList, getResources().getColor(R.color.textRListLineColor));

        arboRList.setAdapter(listAdapter);
    }
}
