package com.a.univ_edt_ade;

import android.content.Intent;
import android.graphics.Point;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.a.univ_edt_ade.CustomsAssets.CustomToolbar;
import com.a.univ_edt_ade.CustomsAssets.DebugCustomLayout;
import com.a.univ_edt_ade.CustomsAssets.EdTBackground;
import com.a.univ_edt_ade.CustomsAssets.HScrollView;
import com.a.univ_edt_ade.CustomsAssets.MenuDrawer;
import com.a.univ_edt_ade.CustomsAssets.VScrollView;
import com.a.univ_edt_ade.CustomsAssets.ViewEvent;
import com.a.univ_edt_ade.EdTFile.Day;
import com.a.univ_edt_ade.EdTFile.Event;
import com.a.univ_edt_ade.EdTFile.JsonEdt;
import com.a.univ_edt_ade.EdTFile.Week;

import java.util.Calendar;

public class EdtDisplay extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    JsonEdt edtFile;

    private HScrollView Hview;
    private VScrollView Vview;
    private LinearLayout LLayout;

    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.f;

    private boolean ignoreNextActions = false;
    private float currentX, currentY, mx, my;

    private RelativeLayout[] Days = new RelativeLayout[7];

    private MenuItem MenuItemEDT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_edt_display);

        Log.d("Debug", "Launching app...");


        CustomToolbar toolbar = (CustomToolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        MenuDrawer drawer = (MenuDrawer) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NavigationView dChild = (NavigationView) drawer.getChildAt(1);
        MenuItemEDT = dChild.getMenu().getItem(0);


        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBar);
        appBarLayout.requestDisallowInterceptTouchEvent(true);




        Hview = (HScrollView) findViewById(R.id.HScrollView);
        Vview = (VScrollView) findViewById(R.id.VScrollView);
        LLayout = (LinearLayout) findViewById(R.id.LLayout);

        LLayout.requestDisallowInterceptTouchEvent(true);

        Hview.setOverScrollMode(HorizontalScrollView.OVER_SCROLL_NEVER);
        Vview.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

        int i = 0;
        Days[i++] = (RelativeLayout) findViewById(R.id.Lundi);
        Days[i++] = (RelativeLayout) findViewById(R.id.Mardi);
        Days[i++] = (RelativeLayout) findViewById(R.id.Mercredi);
        Days[i++] = (RelativeLayout) findViewById(R.id.Jeudi);
        Days[i++] = (RelativeLayout) findViewById(R.id.Vendredi);
        Days[i++] = (RelativeLayout) findViewById(R.id.Samedi);
        Days[i] = (RelativeLayout) findViewById(R.id.Dimanche);


        edtFile = new JsonEdt(this);

        LLayout.setBackground(new EdTBackground(
                (int) getResources().getDimension(R.dimen.daySpacing),
                (int) getResources().getDimension(R.dimen.hourSpacing),
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.cornerHeight),
                getResources().getColor(R.color.hourLine),
                getResources().getColor(R.color.dayLine),
                getResources().getColor(R.color.hourFontColor),
                true,
                -1,
                null));


        ViewTreeObserver viewTreeObserver = LLayout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                //Vview.childHeight = LLayout.getHeight();
                //Hview.childWidth = LLayout.getWidth();

                //Log.d("VScrollView", "Vview dims : height = " + Vview.getHeight() + " width = " + Vview.getWidth());
                //Log.d("HScrollView", "Hview dims : height = " + Hview.getHeight() + " width = " + Hview.getWidth());
                //Log.d("LLayout", "LL dims : height = " + LLayout.getHeight() + " - width = " + LLayout.getWidth());
                //Log.d("DaysLundi", "Lundi " + Days[0].getId() + " dims : height = " + Days[0].getHeight() + " - width = " + Days[0].getWidth());

                Point dims = new Point();
                getWindowManager().getDefaultDisplay().getSize(dims);

                Log.d("Debug", "Dims : width=" + LLayout.getWidth() + " - height=" + LLayout.getHeight());
                Log.d("Debug", "Mesured Dims : width=" + LLayout.getMeasuredWidth() + " - height=" + LLayout.getMeasuredHeight());
                Log.d("Debug", "Window Dims : width=" + dims.x + " - height=" + dims.y);


                for (int i=0;i<Days[0].getChildCount();i++) {
                    //Log.d("DaysChildren", "View " + ((ViewEvent) Days[0].getChildAt(i)).hoursToString() + " : height = " + Days[0].getChildAt(i).getHeight() + " - width = " + Days[0].getChildAt(i).getWidth());
                    //Log.d("DaysChildren", "View " + Days[0].getChildAt(i).getClass() + " : height = " + Days[0].getChildAt(i).getHeight() + " - width = " + Days[0].getChildAt(i).getWidth());
                }

                LLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        json_appear();
    }

    public void json_appear() {

        Toast.makeText(this, "MAKING JSON MAGIC", Toast.LENGTH_SHORT).show();
        Snackbar.make(Hview, "MAKING JSON MAGIC", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        /*
        edtFile.getJSONedt();
        json_view.setText(edtFile.edtInString);

        try {
            JSONObject edtfileinfo = (JSONObject) edtFile.edtJson.get(1);

            sum_view.setText(edtfileinfo.get("file_sum").toString());
        }
        catch (JSONException e){
            Log.e("Exception", "Invalid index : 1 " + e.toString(), e);
        }
        GVTEST = (GridView) findViewById(R.id.Edt_gridTEST);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, data);

        GVTEST.setAdapter(adapter);*/

        //Intent ToTestGrid = new Intent(this, TestNestedScrollView.class);
        //startActivity(ToTestGrid);

        JsonEdt jEdT = new JsonEdt(this);
        jEdT.getJSONedt();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 9);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        Log.d("Debug", "TODAY is " + "2017-09-05" + " and week nb is " + "36");

        Week today = jEdT.getWeek(36, 2017, true);
        Day[] WeekDays = today.getDays();
        Event[] Events;

        int i = 0;
        for (Day day : WeekDays) {
            if (day != null) {
                Events = day.getEvents();
                for (Event event : Events) {
                    addEventToDay(event, i);
                }
            }
            i++;
        }
    }

    private void addEventToDay(Event event, int dayIndex) {
        ViewEvent dispEvent = new ViewEvent(this);
        dispEvent.fromEvent(event);
        dispEvent.setBackground();

        Log.d("DebugAddEventToDay", "dispEvent : start = " + dispEvent.start + ", length = " + dispEvent.length);
        //Log.d("DebugAddEventToDay", "layout : height = " + Days[dayIndex].getMinimumHeight() + ", width = " + Days[dayIndex].getMinimumWidth());

        ViewGroup.MarginLayoutParams MLP = new ViewGroup.MarginLayoutParams(dispEvent.getMinimumWidth(), dispEvent.getMinimumHeight());
        MLP.setMargins(0, dispEvent.getStart(), 0, 0);

        RelativeLayout.LayoutParams RLP = new RelativeLayout.LayoutParams(MLP);

        Days[dayIndex].addView(dispEvent, RLP);
    }



    @Override
    public void onResume() {
        super.onResume();

        MenuItemEDT.setChecked(true);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.light_menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.EdT_disp) {

        } else if (id == R.id.arboSelect) {
            //Intent toArboSelect = new Intent(this, ArboSelect.class);
            //startActivity(toArboSelect);

            Intent toDebugCustomLayout = new Intent(this, TestNestedScrollView.class);
            startActivity(toDebugCustomLayout);

        } else if (id == R.id.options) {
            Intent toTestNavDrawer = new Intent(this, TestNavDrawer.class);
            startActivity(toTestNavDrawer);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Log.d("Debug", "touched!");

        // conditions qui empêche de gérer le 2ème doigt si il se retrouve tout seul
        if (event.getAction() == 6) {
            ignoreNextActions = true;
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ignoreNextActions = false;
        }
        else if (ignoreNextActions) {
            return true;
        }

        scaleDetector.onTouchEvent(event);
        if (LLayout.getScaleX() != scaleFactor) {
            LLayout.setScaleX(scaleFactor);
            Hview.setScaleY(scaleFactor);

            Hview.computeScroll();
            Vview.computeScroll();

        } else {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN: {
                    mx = event.getX();
                    my = event.getY();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    currentX = event.getX();
                    currentY = event.getY();

                    Hview.scrollBy((int) (mx - currentX), (int) (my - currentY));
                    Vview.scrollBy((int) (mx - currentX), (int) (my - currentY));

                    mx = currentX;
                    my = currentY;
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    currentX = event.getX();
                    currentY = event.getY();
                    Vview.scrollBy((int) (mx - currentX), (int) (my - currentY));
                    Hview.scrollBy((int) (mx - currentX), (int) (my - currentY));
                    break;
                }
            }
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 1.5f));

            return true;
        }
    }
}
