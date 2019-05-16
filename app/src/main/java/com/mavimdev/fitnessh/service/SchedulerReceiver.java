package com.mavimdev.fitnessh.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.mavimdev.fitnessh.BuildConfig;
import com.mavimdev.fitnessh.model.FitStatus;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;
import com.mavimdev.fitnessh.util.StorageHelper;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by migue on 12/03/2018.
 */

public class SchedulerReceiver extends BroadcastReceiver {

    @SuppressLint("CheckResult")
    @Override
    public void onReceive(Context context, Intent intent) {
//        FitHelper.notifyUser(context, "SchedulerReceiver1", "", "SchedulerReceiver1");
        PowerManager.WakeLock wakefullLock = null, wakeLock = null;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isInteractive()) {
            wakefullLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "com.fitnessh.fulllock");
            if (!wakefullLock.isHeld()) {
                wakefullLock.acquire(120000);
            }
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.fitnessh.partlock");
            if (!wakefullLock.isHeld()) {
                wakeLock.acquire(120000);
            }
        }
        AtomicInteger attemptsCount = new AtomicInteger();
        String fitClassId = intent.getStringExtra(FitHelper.COM_MAVIM_FITNESS_FIT_CLASS_ID);
        String clientId = intent.getStringExtra(FitHelper.COM_MAVIM_FITNESS_FIT_CLIENT_ID);

        // reserve the class
        try {
            RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class)
                    .bookClass(clientId, fitClassId, FitHelper.RESERVATION_PASSWORD)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .repeatWhen(observable -> observable.delay(FitHelper.attemptsSecondsRepeat, TimeUnit.SECONDS))
                    .takeUntil((Predicate<? super ArrayList<FitStatus>>) response -> !response.get(0).getStatus().equalsIgnoreCase(FitHelper.CLASS_NOT_AVAILABLE))
                    .takeUntil(observable -> attemptsCount.get() >= FitHelper.MAX_ATTEMPTS)
                    .subscribe(response -> {
                                attemptsCount.getAndIncrement();
                                if (BuildConfig.DEBUG) Log.i("Booking ScheduleClass", "Trying to book class - attempt: " + attemptsCount.get());
                                if (!response.get(0).getStatus().equals(FitHelper.CLASS_NOT_AVAILABLE)) {
                                    if (BuildConfig.DEBUG) Log.i("Booking ScheduleClass", response.get(0).getStatus());
                                    FitHelper.notifyUserFitClassFromStorage(context, fitClassId, response.get(0).getStatus());
                                }
                                if (response.get(0).getStatus().equalsIgnoreCase(FitHelper.CLASS_RESERVED)) {
                                    FitHelper.notifyUserFitClassFromStorage(context, fitClassId, response.get(0).getStatus());
                                    StorageHelper.removeScheduleClass(context, fitClassId);
                                }
                            }, err -> {
                                if (BuildConfig.DEBUG) Log.e("Fitness H - Booking", "Erro a reservar a aula: " + err.getMessage());
                                FitHelper.notifyUserFitClassFromStorage(context, fitClassId, "Erro a reservar a aula: " + err.getMessage());
                            }
                    );
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("Fitness H - Booking", "Erro a reservar a aula: " + e.getMessage());
            FitHelper.notifyUser(context, "Exception: " + fitClassId, "error: " + e.getMessage(), "error");
        }

        if (wakeLock != null && wakeLock.isHeld())

        {
            wakeLock.release();
        }
        if (wakefullLock != null && wakefullLock.isHeld())

        {
            wakefullLock.release();
        }
    }
}
