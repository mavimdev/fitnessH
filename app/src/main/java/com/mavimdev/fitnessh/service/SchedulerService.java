package com.mavimdev.fitnessh.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.mavimdev.fitnessh.util.FitHelper;

/**
 * Created by migue on 12/03/2018.
 */

public class SchedulerService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FitHelper.notifyUser(this, "SchedulerService: 2", "");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
