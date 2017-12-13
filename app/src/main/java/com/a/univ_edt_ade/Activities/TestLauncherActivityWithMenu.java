package com.a.univ_edt_ade.Activities;

import android.os.Bundle;

import com.a.univ_edt_ade.Activities.BaseActivity;
import com.a.univ_edt_ade.R;

public class TestLauncherActivityWithMenu extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addLayoutToActivity(R.layout.test_content);
        setMenuItemChecked(R.id.Menu_Main);
    }
}
