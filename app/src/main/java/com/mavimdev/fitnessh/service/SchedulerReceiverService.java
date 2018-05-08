package com.mavimdev.fitnessh.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

/**
 * Created by migue on 12/03/2018.
 */

public class SchedulerReceiverService extends BroadcastReceiver {

    @SuppressLint("CheckResult")
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SchedulerService.class));
        startWakefulService(context, intent);
    }
}
