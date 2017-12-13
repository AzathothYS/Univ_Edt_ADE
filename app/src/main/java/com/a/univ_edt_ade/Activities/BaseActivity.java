package com.a.univ_edt_ade.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.a.univ_edt_ade.CustomsAssets.CustomCoordinatorLayout;
import com.a.univ_edt_ade.CustomsAssets.CustomToolbar;
import com.a.univ_edt_ade.CustomsAssets.MenuDrawer;
import com.a.univ_edt_ade.R;
import com.a.univ_edt_ade.TestGridView;

/**
 * Activité servant de base à toutes les autres
 * Implémente la même toolbar et le même drawer pour toutes les activités
 * Gère les inputs du drawer
 */
public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MenuDrawer menuDrawer;
    private NavigationView navView;
    private ActionBarDrawerToggle drawerToggle;
    private CustomToolbar toolbar;
    private AppBarLayout appBarLayout;
    public CustomCoordinatorLayout mainLayout;

    private int itemIDtoCheck = 0;

    /**
     * le classique 'onCreate', où on rajoute l'init du drawer
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        onCreateDrawer();
    }

    /**
     * Initialise le drawer
     */
    void onCreateDrawer(){
        menuDrawer = (MenuDrawer) findViewById(R.id.menu_drawer);
        navView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (CustomToolbar) findViewById(R.id.toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBar);
        mainLayout = (CustomCoordinatorLayout) findViewById(R.id.mainLayout);

        setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(this, menuDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.close_drawer);

        menuDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navView.setNavigationItemSelectedListener(this);
    }

    /**
     * Ferme le drawer si il est ouvert, sinon comportement classique
     */
    @Override
    public void onBackPressed() {
        if (menuDrawer.isDrawerOpen(GravityCompat.START)) {
            menuDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * ouvre le petit menu de la toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.light_menu_toolbar, menu);
        return true;
    }

    /**
     * Gère les inputs fait dans le petit menu
     * // TODO
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    /**
     * Lance l'activité selectionnée depuis le menu, si celle-ci n'est pas déjà ouverte
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Intent activityStarterPack = null;

        if (item.getItemId() == itemIDtoCheck) {
            Log.d("BaseActivity", "Item selevted is the same as current activity! BAD!");

        } else {
            switch (item.getItemId()) {
                case R.id.Menu_Main:
                    activityStarterPack = new Intent(this, TestLauncherActivityWithMenu.class);
                    break;

                case R.id.Menu_EdT:
                    activityStarterPack = new Intent(this, EdTDisplay.class);
                    break;

                case R.id.Menu_arboSelect:
                    activityStarterPack = new Intent(this, ArboSelect.class);
                    break;

                case R.id.Menu_options:
                    activityStarterPack = new Intent(this, TestEdtDisplay.class);
                    break;

                case R.id.Menu_gridSelect:
                    activityStarterPack = new Intent(this, TestGridView.class);
                    break;
            }
        }

        if (activityStarterPack != null)
            startActivity(activityStarterPack);

        menuDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Appellée à l'init de l'activité implémentatrice, permet de check l'item dans le menu correspondant
     * à celle-ci
     * Permet à 'onNavigationItemSelected' de savoir si l'activité est ouverte ou non
     */
    public void setMenuItemChecked(@IdRes int itemID) {
        itemIDtoCheck = itemID;
    }

    /**
     * Check l'item défini, correspondant à celui qui lance l'activité en cours
     */
    @Override
    public void onResume() {
        super.onResume();
        navView.setCheckedItem(itemIDtoCheck);
    }

    /**
     * Permet de facilement ajouter une layout depuis Xml dans l'activité
     */
    public void addLayoutToActivity(@LayoutRes int layoutID) {
        mainLayout.addView(getLayoutInflater().inflate(layoutID, mainLayout, false), 0);
    }
}
