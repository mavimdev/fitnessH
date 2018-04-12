package com.mavimdev.fitnessh.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mavimdev.fitnessh.model.FitClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StorageHelper {

    public static boolean addScheduleClass(Context context, FitClass fitClass) {
        // loads schedule classes
        List<FitClass> scheduleClasses;
        try {
            scheduleClasses = loadScheduleClasses(context);
        } catch (IOException e) {
            return false;
        }
        // adds the new schedule class
        FitClass fClass = new FitClass(fitClass.getId(), fitClass.getDate(), fitClass.getHorario(),
                fitClass.getDuracao(), fitClass.getAulan(), fitClass.getLocaln());
        scheduleClasses.add(fClass);
        // saves schedule classes
        try {
            saveScheduleClasses(context, scheduleClasses);
        } catch (IOException e) {
            return false;
        }

        return true;
    }


    private static void saveScheduleClasses(Context context, List<FitClass> scheduleClasses) throws IOException {
        Gson gson = new GsonBuilder().create();
        // saves the schedule classes on file
        String classesJson = gson.toJson(scheduleClasses, new TypeToken<List<FitClass>>() {
        }.getType());
        try (FileOutputStream fos = context.openFileOutput(FitHelper.SCHEDULE_INFO_FILE, Context.MODE_PRIVATE)) {
            fos.write(classesJson.getBytes());
        } catch (IOException e) {
            Log.e(FitHelper.class.getSimpleName(), e.getMessage());
            throw e;
        }
    }


    public static List<FitClass> loadScheduleClasses(Context context) throws IOException {
        File fitFile = new File(context.getFilesDir(), FitHelper.SCHEDULE_INFO_FILE);
        String fileContent = new String();
        // read the file if exists.
        if (fitFile.exists()) {
            try (FileInputStream fis = context.openFileInput(FitHelper.SCHEDULE_INFO_FILE)) {
                byte[] data = new byte[(int) fitFile.length()];
                fis.read(data);
                fileContent = new String(data, "UTF-8");
            } catch (IOException e) {
                Log.e(FitHelper.class.getSimpleName(), e.getMessage());
                throw e;
            }
        }
        // converts the content of file to list object
        Gson gson = new GsonBuilder().create();
        List<FitClass> scheduleClasses =
                gson.fromJson(fileContent, new TypeToken<List<FitClass>>() {
                }.getType());
        if (scheduleClasses == null) {
            scheduleClasses = new ArrayList<>();
        }
        return scheduleClasses;
    }


    public static boolean removeScheduleClass(Context context, String fitClassId) {
        // loads schedule classes
        List<FitClass> scheduleClasses;
        try {
            scheduleClasses = loadScheduleClasses(context);
        } catch (IOException e) {
            return false;
        }
        // removes the schedule class
        Iterator<FitClass> it = scheduleClasses.iterator();
        while (it.hasNext()) {
            if (it.next().getId().equals(fitClassId)) {
                it.remove();
                break;
            }
        }
        // saves the schedule classes
        try {
            saveScheduleClasses(context, scheduleClasses);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
