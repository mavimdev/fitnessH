//package com.mavimdev.fitnessh.service;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.PowerManager;
//
//import com.mavimdev.fitnessh.util.FitHelper;
//
//public class DataReceiver extends BroadcastReceiver {
//
//    private final String TAG = "FirebaseDataReceiver";
//
//    public void onReceive(Context context, Intent intent) {
//        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        boolean isScreenOn = pm.isInteractive();
//        if (isScreenOn == false) {
//            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE
//                    , "MyLock");
//            if (!wl.isHeld()) {
//                wl.acquire(10000);
//            }
//            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
//            if (!wl_cpu.isHeld()) {
//                wl_cpu.acquire(10000);
//            }
//        }
//        //Redirect particular screen after receiving notification, this is like ola driver app concept accepting driver request
//        FitHelper.notifyUser(context, "DataReceiver 3", "");
//    }
//}
