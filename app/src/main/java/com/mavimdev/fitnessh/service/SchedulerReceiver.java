package com.mavimdev.fitnessh.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.mavimdev.fitnessh.model.FitStatus;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;
import com.mavimdev.fitnessh.util.StorageHelper;

import java.security.InvalidParameterException;
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
        FitHelper.notifyUser(context, "SchedulerReceived", "");
        Log.i("SchedulerReceiver", "Schedule received");

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.fitnessh");
        if (!wakeLock.isHeld()) {
            wakeLock.acquire(15000);
        }


        AtomicInteger attemptsCount = new AtomicInteger();
        if (!FitHelper.SCHEDULE_INTENT_ACTION.equals(intent.getAction())) {
            return;
        }
        String fitClassId = intent.getStringExtra(FitHelper.COM_MAVIM_FITNESS_FIT_CLASS_ID);
        if (fitClassId == null) {
            throw new InvalidParameterException();
        }
        Log.i("Booking ScheduleClass", "start booking");
        // reserve the class
        RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class)
                .bookClass(FitHelper.clientId, fitClassId, FitHelper.RESERVATION_PASSWORD)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .repeatWhen(observable -> observable.delay(FitHelper.ATTEMPTS_SECONDS_REPEAT, TimeUnit.SECONDS))
                .takeUntil((Predicate<? super ArrayList<FitStatus>>) response -> !response.get(0).getStatus().equalsIgnoreCase(FitHelper.CLASS_NOT_AVAILABLE))
                .takeUntil(observable -> attemptsCount.get() >= FitHelper.MAX_ATTEMPTS)
                .subscribe(response -> {
                            attemptsCount.getAndIncrement();
                            Log.i("Booking ScheduleClass", "Trying to book class - attempt: " + attemptsCount.get());
                            if (!response.get(0).getStatus().equals(FitHelper.CLASS_NOT_AVAILABLE)) {
                                Log.i("Booking ScheduleClass", response.get(0).getStatus());
                            }
                            if (response.get(0).getStatus().equalsIgnoreCase(FitHelper.CLASS_RESERVED)) {
                                FitHelper.notifyUserFitClassFromStorage(context, fitClassId);
                                StorageHelper.removeScheduleClass(context, fitClassId);
                            }
                        }, err -> Log.e("Booking ScheduleClass", "Erro a reservar a aula: " + err.getMessage())
                );

        wakeLock.release();
    }
}
