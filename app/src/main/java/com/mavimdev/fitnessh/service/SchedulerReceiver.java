package com.mavimdev.fitnessh.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.activity.MainActivity;
import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;
import com.mavimdev.fitnessh.util.StorageHelper;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by migue on 12/03/2018.
 */

public class SchedulerReceiver extends BroadcastReceiver {

    @SuppressLint("CheckResult")
    @Override
    public void onReceive(Context context, Intent intent) {
//        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.fitnessh");
//        if (!wl.isHeld()) {
//            wl.acquire();
//        }

        AtomicInteger attemptsCount = new AtomicInteger();
        Log.i("SchedulerReceiver", "Schedule received");
        if (!FitHelper.SCHEDULE_INTENT_ACTION.equals(intent.getAction())) {
            return;
        }
        String fitClassId = intent.getStringExtra(FitHelper.COM_MAVIM_FITNESS_FIT_CLASS_ID);
        if (fitClassId == null) {
            throw new InvalidParameterException();
        }

        // reserve the class
        RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class)
                .bookClass(FitHelper.clientId, fitClassId, FitHelper.RESERVATION_PASSWORD)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .repeatWhen(observable -> observable.delay(FitHelper.ATTEMPTS_SECONDS_REPEAT, TimeUnit.SECONDS))
                .takeWhile(response -> response.get(0).getStatus().equals(FitHelper.CLASS_NOT_AVAILABLE))
                .takeUntil(observable -> attemptsCount.get() >= FitHelper.MAX_ATTEMPTS)
                .subscribe(response -> {
                            attemptsCount.getAndIncrement();
                            if (!response.get(0).getStatus().equals(FitHelper.CLASS_NOT_AVAILABLE)) {
                                Log.i("Booking ScheduleClass", response.get(0).getStatus());
                            }
                            if (response.get(0).getStatus().equals(FitHelper.CLASS_RESERVED)) {
                                this.notifyUser(context, fitClassId);
                                StorageHelper.removeScheduleClass(context, fitClassId);
                            }
                        }, err -> Log.e("Booking ScheduleClass", "Erro a reservar a aula: " + err.getMessage())
                );

//        wl.release();
    }

    private void notifyUser(Context context, String fitClassId) {
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

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "SCHEDULE_NOTIFICATION")
                .setSmallIcon(R.drawable.ic_thumb_up)
                .setContentTitle(context.getString(R.string.class_reserved))
                .setContentText(fitClass.getTitle() + " - " + fitClass.getHorario() + " - " + fitClass.getAulan())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(Integer.valueOf(fitClassId), mBuilder.build());
    }
}
