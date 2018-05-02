package com.mavimdev.fitnessh.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.CompoundButton;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.activity.MainActivity;
import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.model.FitClient;
import com.mavimdev.fitnessh.model.FitClube;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by migue on 04/03/2018.
 */

public class FitHelper {
    // shared preferences
    private static final String SP_FAVORITE_CLUB_ID = "SP_FAVORITE_CLUB_ID";
    private static final String SP_CLIENT_ID = "SP_CLIENT_ID";
    private static final String SP_PACK_FITNESS_HUT = "SP_PACK_FITNESS_HUT";
    private static final String SP_FAVORITE_CLUB_TITLE = "SP_FAVORITE_CLUB_TITLE";
    // user details
    public static String fitnessHutClubId;
    public static String fitnessHutClubTitle;
    public static String fitnessFavoriteClubId;
    private static String fitnessFavoriteClubTitle;
    public static String packFitnessHut;
    public static String clientId;
    // constants
    public static final int TODAY_CLASSES_TAB = 0;
    public static final int TOMORROW_CLASSES_TAB = 1;
    public static final int RESERVED_CLASSES_TAB = 2;
    public static final int HOURS_BEFORE_RESERVATION = 10;
    public static final int MAX_ATTEMPTS = 15;
    public static final int ATTEMPTS_SECONDS_REPEAT = 7;
    public final static String RESERVATION_PASSWORD = "e94b10f0da8d42095ca5c20927416de5";
    // config
    public static final String SCHEDULE_INTENT_ACTION = "com.mavim.ACTION_SCHEDULE";
    public static final String COM_MAVIM_FITNESS_FIT_CLASS_ID = "com.mavim.fitnessH.fitClassId";
    public static final String SCHEDULE_INFO_FILE = "scheduleclasses.fit";
    public static final String CLASS_NOT_AVAILABLE = "NÃO PODE RESERVAR A AULA! AULA INDISPONÍVEL.";
    public static final String CLASS_RESERVED = "AULA RESERVADA";
    public static final String SUCCESS = "success";
    public static final int REQUEST_FAVORITE_CODE = 1;
    public static final String REFRESH_CLASSES = "fit_refresh_classes";


    public static void classifyClass(FitClass fit) throws ParseException {
        // check state of class
        Calendar now = Calendar.getInstance();
        Calendar classDate = Calendar.getInstance();

        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd|H:mm", Locale.getDefault());
        classDate.setTime(dateFormat.parse((fit.getDate() != null ? fit.getDate() : fit.getRdata()) .concat("|")
                .concat(fit.getHorario() != null ? fit.getHorario() : fit.getMhorario())));

        Calendar afterReservationHours = Calendar.getInstance();
        afterReservationHours.add(Calendar.HOUR_OF_DAY, HOURS_BEFORE_RESERVATION);

        if (fit.getClassState() != ClassState.RESERVED && fit.getClassState() != ClassState.SCHEDULE) {
            if (now.after(classDate)) {
                fit.setClassState(ClassState.EXPIRED);
            } else if (classDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
                if (fit.getVagas() != null && fit.getVagas() > 0) {
                    fit.setClassState(ClassState.AVAILABLE);
                } else if (fit.getVagas() != null && classDate.before(afterReservationHours)) {
                    fit.setClassState(ClassState.SOLD_OUT);
                } else {
                    fit.setClassState(ClassState.UNAVAILABLE);
                }
            } else if (classDate.after(now) && classDate.get(Calendar.DAY_OF_MONTH) != now.get(Calendar.DAY_OF_MONTH)) {
                fit.setClassState(ClassState.UNAVAILABLE);
            }
        }
    }


    public static void tintClass(FitClass fclass, CompoundButton swBtnReserveClass) {
        if (fclass.getClassState() == ClassState.AVAILABLE) {
            swBtnReserveClass.setTextColor(Color.GREEN);
            swBtnReserveClass.setText(R.string.class_status_available);
            swBtnReserveClass.setChecked(false);
        } else if (fclass.getClassState() == ClassState.RESERVED) {
            swBtnReserveClass.setTextColor(Color.BLUE);
            swBtnReserveClass.setText(R.string.class_status_reserved);
            swBtnReserveClass.setChecked(true);
        } else if (fclass.getClassState() == ClassState.SCHEDULE) {
            swBtnReserveClass.setTextColor(Color.YELLOW);
            swBtnReserveClass.setText(R.string.class_status_schedule);
            swBtnReserveClass.setChecked(true);
        } else if (fclass.getClassState() == ClassState.EXPIRED) {
            swBtnReserveClass.setTextColor(Color.GRAY);
            swBtnReserveClass.setText(R.string.class_status_expired);
            swBtnReserveClass.setChecked(false);
            swBtnReserveClass.setEnabled(false);
        } else if (fclass.getClassState() == ClassState.SOLD_OUT) {
            swBtnReserveClass.setTextColor(Color.RED);
            swBtnReserveClass.setText(R.string.class_status_soldout);
            swBtnReserveClass.setChecked(false);
        } else if (fclass.getClassState() == ClassState.UNAVAILABLE) {
            swBtnReserveClass.setTextColor(Color.GRAY);
            swBtnReserveClass.setText(R.string.class_status_unavailable);
            swBtnReserveClass.setChecked(false);
        }
    }


    public static Calendar calculateEnrollmentClassDate(FitClass fitClass) throws ParseException {
        Calendar classDate = getClassDate(fitClass);
        // get x hours before class - when the enrollment starts
        classDate.add(Calendar.HOUR, -FitHelper.HOURS_BEFORE_RESERVATION);
        return classDate;
    }


    public static boolean markIfReserved(FitClass fit, ArrayList<FitClass> reservedClasses) {
        for (FitClass rfit : reservedClasses) {
            if (rfit.equals(fit)) {
                fit.setClassState(ClassState.RESERVED);
                fit.setAid(rfit.getId());
                return true;
            }
        }
        return false;
    }


    public static boolean markIfSchedule(FitClass fit, List<FitClass> scheduleClasses) {
        for (FitClass sfit : scheduleClasses) {
            if (sfit.getId().equals(fit.getId())) {
                fit.setClassState(ClassState.SCHEDULE);
                return true;
            }
        }
        return false;
    }


    public static void saveClient(Context context, FitClient fitClient) {
        fitnessHutClubId = fitClient.getMyhutClubId();
        fitnessFavoriteClubId = fitClient.getMyhutClubId();
        fitnessHutClubTitle = fitClient.getMyhutClubName();
        fitnessFavoriteClubTitle = fitClient.getMyhutClubName();
        packFitnessHut = fitClient.getMyhutPack();
        clientId = fitClient.getMyhutId();

        SharedPreferences.Editor sharedPrefEditor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
        sharedPrefEditor.putString(SP_CLIENT_ID, clientId);
        sharedPrefEditor.putString(SP_FAVORITE_CLUB_ID, fitnessFavoriteClubId);
        sharedPrefEditor.putString(SP_FAVORITE_CLUB_TITLE, fitnessFavoriteClubTitle);
        sharedPrefEditor.putString(SP_PACK_FITNESS_HUT, packFitnessHut);
        sharedPrefEditor.apply();

    }


    public static void clearSharedPreferences(Context context) {
        SharedPreferences.Editor sharedPrefEditor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
        sharedPrefEditor.clear();
        sharedPrefEditor.apply();
    }


    public static void clearUserInfo() {
        fitnessHutClubId = "";
        fitnessHutClubTitle = "";
        fitnessFavoriteClubId = "";
        fitnessFavoriteClubTitle = "";
        packFitnessHut = "";
        clientId = "";
    }


    public static void loadSharedPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);        // client id
        if (clientId == null) {
            clientId = sharedPref.getString(FitHelper.SP_CLIENT_ID, "");
            fitnessHutClubId = sharedPref.getString(FitHelper.SP_FAVORITE_CLUB_ID, "");
            fitnessFavoriteClubId = sharedPref.getString(FitHelper.SP_FAVORITE_CLUB_ID, "");
            fitnessHutClubTitle = sharedPref.getString(FitHelper.SP_FAVORITE_CLUB_TITLE, "");
            fitnessFavoriteClubTitle = sharedPref.getString(FitHelper.SP_FAVORITE_CLUB_TITLE, "");
            packFitnessHut = sharedPref.getString(FitHelper.SP_PACK_FITNESS_HUT, "");
        }
    }


    public static void saveClub(Context context, FitClube club) {
        fitnessFavoriteClubId = club.getId();
        fitnessFavoriteClubTitle = club.getTitle();
        SharedPreferences.Editor sharedPrefEditor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
        sharedPrefEditor.putString(SP_FAVORITE_CLUB_ID, club.getId());
        sharedPrefEditor.putString(SP_FAVORITE_CLUB_TITLE, club.getTitle());
        sharedPrefEditor.apply();
    }

    public static Calendar getClassDate(FitClass fitClass) throws ParseException {
        Calendar classDate = Calendar.getInstance();
        classDate.setTime(new SimpleDateFormat("yyyy-MM-dd|H:mm", Locale.getDefault())
                .parse(fitClass.getDate().concat("|").concat(fitClass.getHorario())));
        return classDate;
    }


    public static void notifyUserFitClassFromStorage(Context context, String fitClassId) {
        FitClass fitClass;
        try {
            fitClass = StorageHelper.loadScheduleClass(context, fitClassId);
        } catch (IOException e) {
            Log.e("FitnessH", "Error loading schedule class from storage");
            return;
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String contextTitle = fitClass != null ? fitClass.getTitle() + " - " + fitClass.getHorario() + " - " + fitClass.getAulan() : "";

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "SCHEDULE_NOTIFICATION")
                .setSmallIcon(R.drawable.ic_thumb_up)
                .setContentTitle(context.getString(R.string.class_reserved))
                .setContentText(contextTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(Integer.valueOf(fitClassId), mBuilder.build());
    }


    public static void notifyUser(Context context, String title, String message) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "SCHEDULE_NOTIFICATION")
                .setSmallIcon(R.drawable.ic_thumb_up)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(ThreadLocalRandom.current().nextInt(1000, 10000), mBuilder.build());
    }
}
