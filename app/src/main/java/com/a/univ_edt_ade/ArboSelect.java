package com.a.univ_edt_ade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

public class ArboSelect extends AppCompatActivity {

    private static String[] arboItemList= new String[5];

    static {
        arboItemList[0] = "Etudiants";
        arboItemList[1] = "Enseigants";
        arboItemList[2] = "Salles";
        arboItemList[3] = "Equipements";
        arboItemList[4] = "Autres";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arbo_select);

        RecyclerView arboRList = (RecyclerView) findViewById(R.id.arboRList);
        arboRList.setHasFixedSize(true);

        RecyclerView.LayoutManager arboLayoutM = new LinearLayoutManager(this);
        arboRList.setLayoutManager(arboLayoutM);

        arboRListAdapter listAdapter = new arboRListAdapter(arboItemList, getResources().getColor(R.color.textRListLineColor));

        arboRList.setAdapter(listAdapter);
    }
}
