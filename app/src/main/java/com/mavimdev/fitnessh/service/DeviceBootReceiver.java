package com.mavimdev.fitnessh.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.util.FitHelper;
import com.mavimdev.fitnessh.util.StorageHelper;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by migue on 13/03/2018.
 */

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        List<FitClass> scheduleClasses = null;

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("FitnessH", "device boot received");
            // read from schedule file
            try {
                scheduleClasses = StorageHelper.loadScheduleClasses(context);
            } catch (IOException e) {
                Log.e("FitnessH", "Error reading schedule classes from file");
            }

            // for each class create schedule
            if (scheduleClasses != null) {
                Log.i("FitnessH", "read " + scheduleClasses.size() + " schedule classes.");
                Intent scheduleIntent = new Intent(context, SchedulerReceiver.class);
                scheduleIntent.setAction(FitHelper.SCHEDULE_INTENT_ACTION);

                for (FitClass fclass : scheduleClasses) {
                    // schedule the enrollment
                    Calendar classEnrollmentDate;
                    try {
                        classEnrollmentDate = FitHelper.calculateEnrollmentClassDate(fclass);
                    } catch (ParseException e) {
                        Log.e("FitnessH", "Error parsing class date");
                        continue;
                    }

                    scheduleIntent.putExtra(FitHelper.COM_MAVIM_FITNESS_FIT_CLASS_ID, fclass.getId());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(fclass.getId()), scheduleIntent, 0);
                    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                    if (manager != null) {
                        // sets the schedule
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Wakes up the device in Doze Mode
                            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, classEnrollmentDate.getTimeInMillis(), pendingIntent);
                        } else {
                            // Wakes up the device in Idle Mode
                            manager.setExact(AlarmManager.RTC_WAKEUP, classEnrollmentDate.getTimeInMillis(), pendingIntent);
                        }
                    }
                }
            }
        }
    }
}