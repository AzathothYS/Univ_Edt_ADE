package com.a.univ_edt_ade.Activities;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.a.univ_edt_ade.ArboFile.ArboExplorer;
import com.a.univ_edt_ade.CustomsAssets.FileListAdapter;
import com.a.univ_edt_ade.CustomsAssets.EdTInfoFragment;
import com.a.univ_edt_ade.R;

import java.util.LinkedList;

public class TestLauncherActivityWithMenu extends BaseActivity implements EdTInfoFragment.EdTInfoCallback, FileListAdapter.FileListCallback {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addLayoutToActivity(R.layout.test_content);
        setMenuItemChecked(R.id.Menu_Main);

        CheckBox boxybox = (CheckBox) findViewById(R.id.checkBox);

        boxybox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                makeDialog();
                /*if (isChecked)
                    makeFrag();

                else
                    destroyFrag();*/
            }
        });
    }

    @Override
    public LinkedList<ArboExplorer.PathEntry> getPathAt(int EdTpos, int pos) {
        return null;
    }

    public void makeDialog() {
        EdTInfoFragment.newInstance(0, "TITRE", null).show(getSupportFragmentManager(), "frag_test");
    }

    public void makeFrag() {
        TestFrag frag = new TestFrag();

        getSupportFragmentManager().beginTransaction().add(R.id.act_frame, frag).commit();
    }

    public void destroyFrag() {
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.act_frame)).commit();
    }

    @Override
    public void deletePathAt(int EdTpos, int position, String name) {

    }

    public void onFinishEditDialog(int pos, String inputText) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void launchNewActivity(int pos, String name, LinkedList<ArboExplorer.PathEntry> lol) {
        Toast.makeText(this, "EFSZRHTSR IT WORKS : " + pos + " = " + name, Toast.LENGTH_SHORT).show();
    }
}
