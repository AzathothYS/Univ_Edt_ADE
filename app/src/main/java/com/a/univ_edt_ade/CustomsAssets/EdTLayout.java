package com.a.univ_edt_ade.CustomsAssets;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.LocaleDisplayNames;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.Space;
import android.widget.Toast;

import com.a.univ_edt_ade.EdTFile.Day;
import com.a.univ_edt_ade.EdTFile.Event;
import com.a.univ_edt_ade.EdTFile.JsonEdt;
import com.a.univ_edt_ade.EdTFile.Week;
import com.a.univ_edt_ade.R;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * View custom qui affiche l'EdT, gère le scrolling et le scaling, les changements d'orientation et
 * de mode d'affichage, etc...
 */

public class EdTLayout extends LinearLayout {

    public static JsonEdt jEdT;

    public VScrollView Vview;
    public HScrollView Hview;

    public int height, width;
    private int cornerWidth;
    private int daySpacing;


    public RelativeLayout[] Days = new RelativeLayout[7];
    public int dayDispIndex = -1;
    public boolean landscapeMode = false;

    private int initialDay = -1;


    public GestureDetector gestureListener;
    private boolean ignoreNextActions = false;
    private final float friction = 0.4f;    // facteur de réduction de la vitesse du fling

    private int potentialDayChange = -1;      // si on change vers DayDisp, dayDispIndex sera changé à cette valeur

    public ScaleGestureDetector scaleDetector;
    private Point pivot = new Point(0,0);
    private float scaleFactor = 1.f;
    private final float scaleFactorMAX = 1.2f, scaleFactorMIN = .8f;

    private long timeSinceLastChange = Calendar.getInstance().getTimeInMillis();

    private Thread ScaleReducer;
    private AtomicBoolean isScaleThreadRunning = new AtomicBoolean();


    private boolean isScreenInLandscape = false;


    public EdTLayout(Context context) {
        super(context);
        init(context);
    }

    public EdTLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EdTLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Constructeur appellé uniquement par l'activité implémentarice lors d'une restoration d'un état
     * précédent, après un chamgement d'orientation par exemple
     * @param previousState : un Bundle créé par 'getState'
     */
    public EdTLayout(Context context, Bundle previousState) {
        super(context);

        if (previousState != null) {
            dayDispIndex = previousState.getInt("dayDispIndex", -1);
            landscapeMode = previousState.getBoolean("landscapeMode", false);
            initialDay = previousState.getInt("initialDay", -1);
        }

        if (dayDispIndex >= 0) {
            initDayDispMode(context);
        } else if (landscapeMode) {
            initLandscapeMode(context);
        } else {
            init(context);
        }

        isScreenInLandscape = getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE;
    }

    // TODO: tout changer pour avoir les scrollviews DANS l'EdT, pour : afficher les jours et les heures en permanance, scaling DANS les scrollviews -> plus le bord blanc lorsque l'on dézoom,
    // plus dayDisp avec un frame layout ou non avec tous les jours affichés, mais on scroll horizontalement pour les parcourir (on change de jour après un certain déplacement)

    // TODO: rajouter les events méthode pour associer le fichier/liste d'event voulu, une autre appellée après chaque changement de state et onFinishInflate pour les afficher

    // TODO: remettre le scrolling là où il était lorque l'on passe de landscape à initial

    // TODO: nettoyer le code des init et le compacter -> quasi fait

    // TODO: Crash lors d'un changement de mode (à cause du scaling?) -> android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views

    // TODO: le background en mode landscape est faux

    // TODO: LE SCROLLING A RESET LORSQUE L'ON SE MET EN MODE LANDSCAPE PUTAIN

    // TODO: Mettre le Json parser dans un thread (Async?) car il y a une memory leak à chaque fois que l'on l'utilise
    // TODO: mais d'abord faire de JsonEdT un singleton ou l'avoir encapsulé dans un singleton

    // TODO: Créer mon propre GestureDetector, car c'est le seul moyen de régler le problème d'attente avant le scroll

    /**
     * Ajoute dynamiquement les layout des jours et les espaces sur les côtés pour
     * afficher l'emploi du temps.
     * Ajoute un EdTBackground
     * Définit les GestureListners et le ScaleGestureListner
     * Définit la taille de la Layout
     */
    private void init(Context context) {

        Log.d("Debug", "Initializating EdTLayout... ------------ INITIAL STATE");


        FrameLayout.LayoutParams EdTLayoutP = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setLayoutParams(EdTLayoutP);
        this.setId(R.id.EdTLayout);


        LayoutParams spaceP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        cornerWidth = (int) getResources().getDimension(R.dimen.cornerWidth);

        LayoutParams daysP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.daySpacing),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        daySpacing = (int) getResources().getDimension(R.dimen.daySpacing);

        // on définit les dimensions de la relative layout parente des events
        ViewEvent.parentHourSpacing = (int) getResources().getDimension(R.dimen.hourSpacing);
        ViewEvent.parentWidth = daySpacing;
        ViewEvent.parentCornerHeight = (int) getResources().getDimension(R.dimen.cornerHeight);


        TypedArray daysIDs = getResources().obtainTypedArray(R.array.days_ids);

        this.addView(new Space(context), spaceP);

        for (int i=0; i<Days.length; i++) {
            Days[i] = new RelativeLayout(context);
            Days[i].setId(daysIDs.getResourceId(i, 0));

            this.addView(Days[i], daysP);
        }

        daysIDs.recycle();

        this.addView(new Space(context), spaceP);


        width = ((int) getResources().getDimension(R.dimen.daySpacing)) * 7
                + ((int) getResources().getDimension(R.dimen.cornerWidth)) * 2;
        height = ((int) getResources().getDimension(R.dimen.hourSpacing)) * 12
                + (int) getResources().getDimension(R.dimen.cornerHeight);

        globalInitialization(context);

        Log.d("Debug", "EdTView init completed.");
    }

    /**
     * Appelée lorsque la view restore un état précédent
     * Initialise alors la layout directement en mode DayDisp
     */
    private void initDayDispMode(Context context) {

        // TODO: implémenter un truc pour scroller d'un jour à l'autre, peut-être avec une autre layout?

        Log.d("Debug", "Initializating EdTLayout... ------------ DAYDISP MODE");


        FrameLayout.LayoutParams EdTLayoutP = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setLayoutParams(EdTLayoutP);
        this.setId(R.id.EdTLayout);


        Point size = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(size);

        width = size.x;
        height = ((int) getResources().getDimension(R.dimen.hourSpacing)) * 12
                + (int) getResources().getDimension(R.dimen.cornerHeight);

        LayoutParams spaceP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        cornerWidth = (int) getResources().getDimension(R.dimen.cornerWidth);

        // la journée prendra toute la place restante
        LayoutParams dayP = new LayoutParams(
                width - 2 * spaceP.width,
                (int) getResources().getDimension(R.dimen.hoursHeight));

        daySpacing = (int) getResources().getDimension(R.dimen.daySpacing);

        // on définit les dimensions de la relative layout parente des events
        ViewEvent.parentHourSpacing = (int) getResources().getDimension(R.dimen.hourSpacing);
        ViewEvent.parentWidth = dayP.width;
        ViewEvent.parentCornerHeight = (int) getResources().getDimension(R.dimen.cornerHeight);


        TypedArray daysIDs = getResources().obtainTypedArray(R.array.days_ids);

        this.addView(new Space(context), spaceP);

        // on initialise toujours la totalité des jours, mais on va n'en afficher qu'un
        for (int i=0; i<Days.length; i++) {
            Days[i] = new RelativeLayout(context);
            Days[i].setId(daysIDs.getResourceId(i, 0));
        }

        daysIDs.recycle();

        this.addView(Days[dayDispIndex], dayP);

        this.addView(new Space(context), spaceP);


        globalInitialization(context);

        Log.d("Debug", "EdTView (DayDisp Mode) init completed.");
    }

    /**
     * Appelée lorsque la view restore un état précédent
     * Initialise alors la layout directement en mode Landscape
     */
    private void initLandscapeMode(Context context) {

        Log.d("Debug", "Initializating EdTLayout... ------------ LANDSCAPE MODE");


        FrameLayout.LayoutParams EdTLayoutP = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setLayoutParams(EdTLayoutP);
        this.setId(R.id.EdTLayout);


        LayoutParams spaceP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        cornerWidth = (int) getResources().getDimension(R.dimen.cornerWidth);

        LayoutParams daysP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.daySpacing),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        daySpacing = (int) getResources().getDimension(R.dimen.daySpacing);


        Point size = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(size);

        width = size.x;
        height = size.y;

        //on doit faire en sorte que tout l'emploi du temps puisse être affiché à l'écran en même temps

        if (daySpacing * 7 + 2 * cornerWidth > width) {
            while (daySpacing * 7 + 2 * cornerWidth > width) {
                daySpacing--;
            }
            Log.d("EdTLayout", "Init Landscape : previous dayspacing=" + daysP.width + " - now=" + daySpacing);

        } else if (daySpacing * 7 + 2 * cornerWidth < width) {
            while (daySpacing * 7 + 2 * cornerWidth < width) {
                daySpacing++;
            }
            daySpacing--;
            Log.d("EdTLayout", "Init Landscape : previous dayspacing=" + daysP.width + " - now=" + daySpacing);

        } else {
            Log.d("EdTLayout", "Init Landscape : EdT width matched with window width! Much wow!");
        }

        daysP.width = daySpacing;

        int hourHeight = (int) getResources().getDimension(R.dimen.hourSpacing);
        int cornerHeight = (int) getResources().getDimension(R.dimen.cornerHeight);

        if (hourHeight * 12 + cornerHeight > height) {
            while (hourHeight * 12 + cornerHeight > height) {
                hourHeight--;
            }
            Log.d("EdTLayout", "Init Landscape : previous hoursHeight=" + daysP.height + " - now=" + (hourHeight * 12 + cornerHeight));

        } else if (hourHeight * 12 + cornerHeight < height) {
            while (hourHeight * 12 + cornerHeight < height) {
                hourHeight++;
            }
            hourHeight--;
            Log.d("EdTLayout", "Init Landscape : previous hoursHeight=" + daysP.height + " - now=" + (hourHeight * 12 + cornerHeight));

        } else {
            Log.d("EdTLayout", "Init Landscape : EdT height matched with window height! Much wow!");
        }

        daysP.height = hourHeight * 12 + cornerHeight;
        spaceP.height = daysP.height;

        // on définit les dimensions de la relative layout parente des events
        ViewEvent.parentHourSpacing = hourHeight;
        ViewEvent.parentWidth = daySpacing;
        ViewEvent.parentCornerHeight = cornerHeight;


        TypedArray daysIDs = getResources().obtainTypedArray(R.array.days_ids);

        this.addView(new Space(context), spaceP);

        for (int i=0; i<Days.length; i++) {
            Days[i] = new RelativeLayout(context);
            Days[i].setId(daysIDs.getResourceId(i, 0));

            this.addView(Days[i], daysP);
        }

        daysIDs.recycle();

        this.addView(new Space(context), spaceP);


        globalInitialization(context);

        Log.d("Debug", "EdTView init (Landscape Mode) completed.");
    }

    /**
     * Actions communes à toutes les initialisations
     */
    private void globalInitialization(Context context) {

        this.setBackground(new EdTBackground(
                daySpacing,
                ViewEvent.parentHourSpacing,
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.cornerHeight),
                getResources().getColor(R.color.hourLine),
                getResources().getColor(R.color.dayLine),
                getResources().getColor(R.color.hourFontColor),
                !landscapeMode,
                dayDispIndex,
                null));

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        gestureListener = new GestureDetector(context, new GestureListener());

        createScaleReducer();

        displayEvents(); // TODO: Asynctask dans le cas où cela prendrait trop de ressources
    }

    /**
     * Change les paramètres de la layout pour se mettre en mode DayDisp
     * L'init est alors déjà faite.
     */
    private void changeToDayDispMode(Context context) {
        Log.d("Debug", "Resetting EdTLayout...");

        this.removeAllViewsInLayout();
        this.setBackgroundResource(0);  // reset background

        Log.d("Debug", "Changing to DayDisp EdTLayout...  ------------ DAYDISP STATE");

        Point size = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(size);

        width = size.x;
        height = ((int) getResources().getDimension(R.dimen.hourSpacing)) * 12
                + (int) getResources().getDimension(R.dimen.cornerHeight);

        LayoutParams spaceP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        // la journée prendra toute la place restante
        LayoutParams dayP = new LayoutParams(
                width - 2 * spaceP.width,
                (int) getResources().getDimension(R.dimen.hoursHeight));


        // on définit les dimensions de la relative layout parente des events
        ViewEvent.parentHourSpacing = (int) getResources().getDimension(R.dimen.hourSpacing);
        ViewEvent.parentWidth = dayP.width;
        ViewEvent.parentCornerHeight = (int) getResources().getDimension(R.dimen.cornerHeight);


        this.addView(new Space(context), spaceP);

        this.addView(Days[dayDispIndex], dayP);

        this.addView(new Space(context), spaceP);

        this.setBackground(new EdTBackground(
                (int) getResources().getDimension(R.dimen.daySpacing),
                (int) getResources().getDimension(R.dimen.hourSpacing),
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.cornerHeight),
                getResources().getColor(R.color.hourLine),
                getResources().getColor(R.color.dayLine),
                getResources().getColor(R.color.hourFontColor),
                true,
                dayDispIndex,
                null));

        displayEvents();

        Log.d("EdTLayout", "DayDisp mode ACTIVATED with day n°" + dayDispIndex);
        invalidate();       // TODO: necessaire ?
    }

    /**
     * Change les paramètres de la layout pour se mettre en mode Landscape
     * L'init est alors déjà faite.
     */
    private void changeToLandscapeMode(Context context) {

        Log.d("Debug", "Resetting EdTLayout...");

        this.removeAllViewsInLayout();
        this.setBackgroundResource(0);  // reset background

        Log.d("Debug", "Changing to Landscape EdTLayout...  ------------ Landscape STATE");

        LayoutParams spaceP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        cornerWidth = (int) getResources().getDimension(R.dimen.cornerWidth);

        LayoutParams daysP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.daySpacing),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        daySpacing = (int) getResources().getDimension(R.dimen.daySpacing);


        Point size = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(size);

        width = size.x;
        height = size.y;

        //on doit faire en sorte que tout l'emploi du temps puisse être affiché à l'écran en même temps

        if (daySpacing * 7 + 2 * cornerWidth > width) {
            while (daySpacing * 7 + 2 * cornerWidth > width) {
                daySpacing--;
            }
            Log.d("EdTLayout", "Init Landscape : previous dayspacing=" + daysP.width + " - now=" + daySpacing);

        } else if (daySpacing * 7 + 2 * cornerWidth < width) {
            while (daySpacing * 7 + 2 * cornerWidth < width) {
                daySpacing++;
            }
            daySpacing--;
            Log.d("EdTLayout", "Init Landscape : previous dayspacing=" + daysP.width + " - now=" + daySpacing);

        } else {
            Log.d("EdTLayout", "Init Landscape : EdT width matched with window width! Much wow!");
        }

        daysP.width = daySpacing;

        int hourHeight = (int) getResources().getDimension(R.dimen.hourSpacing);
        int cornerHeight = (int) getResources().getDimension(R.dimen.cornerHeight);

        if (hourHeight * 12 + cornerHeight > height) {
            while (hourHeight * 12 + cornerHeight > height) {
                hourHeight--;
            }
            Log.d("EdTLayout", "Init Landscape : previous hoursHeight=" + daysP.height + " - now=" + (hourHeight * 12 + cornerHeight));

        } else if (hourHeight * 12 + cornerHeight < height) {
            while (hourHeight * 12 + cornerHeight < height) {
                hourHeight++;
            }
            hourHeight--;
            Log.d("EdTLayout", "Init Landscape : previous hoursHeight=" + daysP.height + " - now=" + (hourHeight * 12 + cornerHeight));

        } else {
            Log.d("EdTLayout", "Init Landscape : EdT height matched with window height! Much wow!");
        }

        daysP.height = hourHeight * 12 + cornerHeight;
        spaceP.height = daysP.height;

        // on définit les dimensions de la relative layout parente des events
        ViewEvent.parentHourSpacing = hourHeight;
        ViewEvent.parentWidth = daySpacing;
        ViewEvent.parentCornerHeight = cornerHeight;


        this.addView(new Space(context), spaceP);

        for (RelativeLayout Day : Days)
            this.addView(Day, daysP);

        this.addView(new Space(context), spaceP);


        this.setBackground(new EdTBackground(
                daySpacing,
                hourHeight,
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.cornerHeight),
                getResources().getColor(R.color.hourLine),
                getResources().getColor(R.color.dayLine),
                getResources().getColor(R.color.hourFontColor),
                false,
                -1,
                null));

        Log.d("EdTLayout", "Landscape mode ACTIVATED");

        displayEvents();

        invalidate();
    }

    /**
     *Change les paramètres de la layout pour se mettre en mode initial
     * L'init est alors déjà faite.
     */
    private void changeToInitialMode(Context context) {

        Log.d("Debug", "Resetting EdTLayout...");

        this.removeAllViewsInLayout();
        this.setBackgroundResource(0);  // reset background

        Log.d("Debug", "Changing to the initial EdTLayout...  ------------ INITIAL STATE");

        LayoutParams spaceP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.cornerWidth),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        cornerWidth = (int) getResources().getDimension(R.dimen.cornerWidth);

        LayoutParams daysP = new LayoutParams(
                (int) getResources().getDimension(R.dimen.daySpacing),
                (int) getResources().getDimension(R.dimen.hoursHeight));

        daySpacing = (int) getResources().getDimension(R.dimen.daySpacing);

        // on définit les dimensions de la relative layout parente des events
        ViewEvent.parentHourSpacing = (int) getResources().getDimension(R.dimen.hourSpacing);
        ViewEvent.parentWidth = daySpacing;
        ViewEvent.parentCornerHeight = (int) getResources().getDimension(R.dimen.cornerHeight);


        this.addView(new Space(context), spaceP);

        for (RelativeLayout Day : Days)
            this.addView(Day, daysP);

        this.addView(new Space(context), spaceP);


        this.setBackground(new EdTBackground(
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


        width = ((int) getResources().getDimension(R.dimen.daySpacing)) * 7
                + ((int) getResources().getDimension(R.dimen.cornerWidth)) * 2;
        height = ((int) getResources().getDimension(R.dimen.hourSpacing)) * 12
                + (int) getResources().getDimension(R.dimen.cornerHeight);

        displayEvents();

        Log.d("EdTLayout", "Initial mode ACTIVATED");
        invalidate();
    }

    /**
     * Change les params de la layout pour faire la sortir du mode DayDisp, donc en mode initial
     */
    private void exitDayDispMode(Context context) {

        Log.d("EdTLayout", "DayDisp mode DEACTIVATED");

        initialDay = dayDispIndex; // on se rappelle du jour affiché pour scroller sur lui en mode initial
        dayDispIndex = -1;

        changeToInitialMode(context);
    }

    /**
     * Change les params de la layout pour faire la sortir du mode Landscape, donc en mode initial
     */
    private void exitLandscapeMode(Context context) {
        Log.d("EdTLayout", "Landscape mode DEACTIVATED");

        changeToInitialMode(context);
    }

    /**
     * Crée le thread qui s'occupe de remettre le scaling à 1 après un scaling pas suffisament important
     * pour changer d'orientation.
     * Le changement de scale est nécessaire à l'utilisateur pour qu'il associe bien les mouvements
     * pour changer d'orientation.
     */
    private void createScaleReducer() {

        isScaleThreadRunning.set(false);

        ScaleReducer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(25);
                        if (isScaleThreadRunning.get() && scaleFactor != 1.f) {

                            // gaussienne, plus le scalefactor est proche de 1, plus il va aller vite
                            //scaleFactor += (scaleFactor < 1.f ? 1 : -1) * 0.05f * Math.exp(- Math.pow(scaleFactor - 1.f, 2) * 200);
                            scaleFactor += (scaleFactor < 1.f) ? 0.01 : -0.01;
                            if (scaleFactor > 0.99f && scaleFactor < 1.01f) {
                                scaleFactor = 1.f;         // on l'arrondit
                                isScaleThreadRunning.set(false);    // il a terminé sa tâche
                            }

                            Vview.setScaleX(scaleFactor);
                            Vview.setScaleY(scaleFactor);

                            Log.d("ScaleThread", "new scale : " + scaleFactor);
                        }

                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        Log.d("ScaleThread", "Finished.");
                        break;
                    }
                }
            }
        }, "EdTScaler");

        Log.d("Scaling", "Start of thread");
        ScaleReducer.start();
    }

    /**
     * Ajoute les events des jours affichés à l'écran aux layouts
     */
    private void displayEvents() {
        //Snackbar.make(Hview, "MAKING JSON MAGIC", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show();

        Toast.makeText(getContext(), "MAKING JSON MAGIC", Toast.LENGTH_SHORT).show();

        if (jEdT == null) {
            Log.d("EdTLayout", "Récupération du Json");
            jEdT = new JsonEdt(getContext());
            jEdT.getJSONedt();
        }

        Log.d("Debug", "TODAY is " + "2017-09-05" + " and week nb is " + "36");

        Week today = jEdT.getWeek(36, 2017, true);
        Day[] WeekDays = today.getDays();
        Event[] Events;

        // TODO: ajouter un hachurage lorsque le jour est vide ? ou griser le jour ? genre un peu ?
        if (dayDispIndex >= 0) {
            // on ajoute les event de seulement le jour affiché
            if (WeekDays[dayDispIndex] != null) {
                Events = WeekDays[dayDispIndex].getEvents();
                for (Event event : Events) {
                    addEventToDay(event, dayDispIndex);
                }
            }

        } else {
            // on ajoute les events de tous les jours
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
    }

    /**
     * Ajoute un event à un jour
     */
    private void addEventToDay(Event event, int dayIndex) {
        ViewEvent dispEvent = new ViewEvent(getContext());
        dispEvent.fromEvent(event);
        dispEvent.setBackground();

        Log.d("DebugAddEventToDay", "dispEvent : start = " + dispEvent.getStart() + ", length = " +  dispEvent.getMinimumHeight());

        ViewGroup.MarginLayoutParams MLP = new ViewGroup.MarginLayoutParams(dispEvent.getMinimumWidth(), dispEvent.getMinimumHeight());
        MLP.setMargins(0, dispEvent.getStart(), 0, 0);

        RelativeLayout.LayoutParams RLP = new RelativeLayout.LayoutParams(MLP);

        Days[dayIndex].addView(dispEvent, RLP);
    }


    /**
     * Initialise le scroll horizontal pour mettre le jour d'aujourd'hui au milieu de l'écran
     *
     * Méthode appellée à la toute fin de la création de la view, lorsque que les chlidren ont été
     * créés et inflated, on peut donc changer le scroll des scrollViews, etc...
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        Log.d("EdTLayout", "layouting...  --------------------------------------");

        if (dayDispIndex < 0 && !landscapeMode) {
            // uniquement en mode initial

            int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

            if (initialDay < 0) {
                // on centre le jour d'aujourd'hui
                Hview.smoothScrollTo(cornerWidth + Calendar.getInstance().get(Calendar.DAY_OF_WEEK) * daySpacing - screenWidth / 2 + daySpacing / 2 , 0);
            } else {
                // on centre le jour que l'on souhaite
                Hview.smoothScrollTo(cornerWidth + initialDay * daySpacing - screenWidth / 2 + daySpacing / 2, 0);
            }
        }
    }


    /**
     * Définit une taille fixe de l'EdT qui va être plus grande que l'écran.
     * 'width' et 'height' sont déterminés à l'init, en fonction des dimentions des heures et jours.
     * Les paramètres sont inutiles.
     * L'appel à la méthode du parent est important pour les child de l'EdTLayout
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }


    /**
     * Décide vers quel Listener envoyer l'event
     * Définit le pivot dans le cas d'un mouvement à plusieurs doigts
     * Empêche les problèmes d'affichage lorsque l'on passe de 2 doigts à 1
     */
    public void handleTouchEvents(MotionEvent event) {

        if (event.getPointerCount() > 1) {

            if (potentialDayChange == -1) {
                // le pivot et le jour vers lequel basculer n'ont pas été définis

                // on prend le milieu des 2 pointeurs
                pivot.x = (int) (event.getX(0) + event.getX(1)) / 2;
                pivot.y = (int) (event.getY(0) + event.getY(1)) / 2;

                if (pivot.x + Hview.getScrollX() > cornerWidth && pivot.x + Hview.getScrollX() < width - cornerWidth) {
                    // le pivot se trouve dans l'un des jours affichés
                    potentialDayChange = (int) Math.floor((float) (pivot.x + Hview.getScrollX() - cornerWidth) / daySpacing);
                }
            }

            scaleDetector.onTouchEvent(event);
        }
        else {

            if (event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                // après un mouvement à 2 doigts, le 1er est levé -> on ignore les mouvements du 2ème jusque au moment où il est levé
                ignoreNextActions = true;
                return;
            }
            else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ignoreNextActions = false;
            }
            else if (ignoreNextActions) {
                return;
            }

            gestureListener.onTouchEvent(event);
        }

    }

    /**
     * Laisse l'activité implémentatrice envoyer les events au gesture listener
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    /**
     * Gère les mouvements envoyés par l'activité implémentatrice en les envoyants aux ScrollViews
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean flingHappened = false;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            Hview.scrollBy((int) distanceX, 0);
            Vview.scrollBy(0, (int) distanceY);

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Hview.fling(- (int) (velocityX * friction * 1.2));
            Vview.fling(- (int) (velocityY * friction));

            flingHappened = true;

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {

            if (flingHappened) {
                // si un mouvment de fling a eu lieu, on l'arrête
                Hview.smoothScrollTo(Hview.getScrollX(),0);
                Vview.smoothScrollTo(0,Vview.getScrollY());

                flingHappened = false;
            }

            return true;
        }
    }

    /**
     * Gère les mouvements multi-touch, mais ne prend en compte que les mouvements de zoom.
     * Fait changer l'état de la layout et l'orientation de l'activité en fonction du zoom
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private int jourZoome = -1;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaleThreadRunning.set(false);    // comme on va changer le scalefactor, il faut que tout soit thread-safe

            // on définit le pivot du scaling et le jour sur lequel on zoom au début de chaque scaling
            setPivotX(pivot.x);
            setPivotY(pivot.y);
            jourZoome = potentialDayChange;

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (detector.getScaleFactor() < 0.01f)
                return false;   // on attend que on a un changement important pour le traiter

            if ((detector.getScaleFactor() > 1 && dayDispIndex < 0) || (detector.getScaleFactor() < 1 && !landscapeMode)) {
                // on zoom que si on n'est pas en DayDisp, ou on dézoom que si on n'est pas en Landscape mode

                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(scaleFactorMIN, Math.min(scaleFactor, scaleFactorMAX));

                Vview.setPivotX(pivot.x);
                Vview.setPivotY(pivot.y);
                Vview.setScaleX(scaleFactor);
                Vview.setScaleY(scaleFactor);
            }

            // cooldown de 0.5 sec
            if (Calendar.getInstance().getTimeInMillis() < timeSinceLastChange + 500)
                return true;

            if (scaleFactor == scaleFactorMAX && dayDispIndex == -1) {
                // on zoom

                if (!landscapeMode) {
                    // on est dans le mode initial
                    // on veut changer l'affichage pour n'afficher qu'un seul jour

                    if (jourZoome < 0) {
                        Log.d("EdTScale", "potentialDayChange est négatif!");
                        return true;
                    }

                    Log.d("EdTLayout", "---------------------- initial -> DayDisp");

                    dayDispIndex = jourZoome;

                    // on reset le scrolling
                    Hview.scrollTo(0, 0);
                    Vview.scrollTo(0, 0);

                    if (needToChangeOrientation(1))
                        setContextOrientation(1);
                    else
                        changeToDayDispMode(getContext());

                } else {
                    // on est en mode landscape
                    // on veut passer en mode initial

                    Log.d("EdTLayout", "---------------------- landscape -> initial");

                    landscapeMode = false;

                    // pas besoin de savoir si il aura un changement d'orientation,
                    // il faut juste avoir prédéfini le state avant pour s'assurer que si il y a
                    // changment d'orientation, le state sera gardé.
                    exitLandscapeMode(getContext());
                    setContextOrientation(0);
                }
            } else if (scaleFactor == scaleFactorMIN && !landscapeMode) {
                // on dézoom

                if (dayDispIndex >= 0) {
                    // on est en mode DayDisp
                    // on veut passer en mode initial

                    Log.d("EdTLayout", "---------------------- DayDisp -> initial");

                    // pas besoin de savoir si il aura un changement d'orientation,
                    // il faut juste avoir prédéfini le state avant pour s'assurer que si il y a
                    // changment d'orientation, le state sera gardé.
                    exitDayDispMode(getContext());
                    setContextOrientation(0);

                } else {
                    // on est en mode initial
                    // on veut passer en mode landscape

                    Log.d("EdTLayout", "---------------------- initial -> landscape");

                    landscapeMode = true;

                    // on reset le scrolling
                    Hview.scrollTo(0, 0);
                    Vview.scrollTo(0, 0);

                    if (needToChangeOrientation(-1))
                        setContextOrientation(-1);
                    else
                        changeToLandscapeMode(getContext());
                }
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            Log.d("ScaleDetector", "scale ended with scaleFactor=" + scaleFactor);

            if (scaleFactor <= scaleFactorMAX && scaleFactor >= scaleFactorMIN) {
                // on lance un thread que si on ne change pas d'orientation
                isScaleThreadRunning.set(true);
            } else {
                Log.d("ScaleDetector", "didn't started the thread : factor=" + scaleFactor + " - MIN=" + scaleFactorMIN + " - MAX=" + scaleFactorMAX);
            }
            jourZoome = -1;
            potentialDayChange = -1;
        }
    }


    /**
     * change l'orientation de l'activité implémentatrice
     * @param mode : 1 = portrait, 0 = libre, -1 = paysage
     */
    public void setContextOrientation(int mode) {
        switch (mode) {
            case 1:
                Log.d("EdTLayout", "changed orientation to portrait");
                if (Build.VERSION.SDK_INT >= 18)
                    ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                else
                    ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case -1:
                Log.d("EdTLayout", "changed orientation to landscape");
                if (Build.VERSION.SDK_INT >= 18)
                    ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                else
                    ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                Log.d("EdTLayout", "changed orientation to default");
                ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
        }

        timeSinceLastChange = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Détermine si il faut changer l'orientation de l'activité
     * Fixe automatiquement l'orientation si elle était en mode défault (SCREEN_ORIENTATION_UNSPECIFIED)
     * @param modeWanted : 1 = portrait, -1 = paysage ('libre' n'est pas pris en compte)
     */
    public boolean needToChangeOrientation(int modeWanted) {
        switch (((Activity) getContext()).getRequestedOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                if (modeWanted == 1)
                    return false;
                break;

            case ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT:
                if (modeWanted == 1)
                    return false;
                break;

            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                if (modeWanted == -1)
                    return false;
                break;

            case ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE:
                if (modeWanted == -1)
                    return false;
                break;

            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (modeWanted == 1) {
                        // on change quand même l'orientation mais on retourne 'false', car on est déjà bon
                        if (Build.VERSION.SDK_INT >= 18)
                            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                        else
                            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        return false;
                    }

                } else if (modeWanted == -1) {
                    // on est déjà en mode portrait, mais il faut le fixer
                    if (Build.VERSION.SDK_INT >= 18)
                        ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                    else
                        ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    return false;
                }
                break;
        }
        return true;
    }

    // TODO: à supprimmer ?
    /**
     * https://stackoverflow.com/a/29392593/8662187
     * Méthode qui retourne systématiquement la bonne orientation de l'écran
     */
    protected int getScreenOrientation() {
        Display getOrient = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();

        getOrient.getSize(size);

        int orientation;
        if (size.x < size.y) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        }
        else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }

        return orientation;
    }

    /**
     * On détruit le thread ScaleReducer lorsque la view va se faire détruire
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ScaleReducer.interrupt();
    }

    /**
     * 'Sauvegarde' le state des paramètres de la layout, utilisé par l'activité implémentatrice
     * lorsque qu'il y a destruction des views
     * @return : Bundle avec les paramètres dedans
     */
    public Bundle getState() {
        Bundle state = new Bundle();

        Log.d("ADyingEdTLayout", "Sauvegarde du state!");

        state.putInt("dayDispIndex", dayDispIndex);
        state.putBoolean("landscapeMode", landscapeMode);
        state.putInt("initialDay", initialDay);

        return state;
    }
}
