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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class StorageHelper {

    /**
     * Adds a schedule class to the exist ones
     *
     * @param context
     * @param fitClass
     * @return
     */
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
                fitClass.getDuracao(), fitClass.getAulan(), fitClass.getLocaln(), fitClass.getTitle());
        scheduleClasses.add(fClass);
        // saves schedule classes
        try {
            saveScheduleClasses(context, scheduleClasses);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * saves the schedules classes on file
     *
     * @param context
     * @param scheduleClasses
     * @throws IOException
     */
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

    /**
     * loads the schedule classes from file
     *
     * @param context
     * @return
     * @throws IOException
     */
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

    /**
     * Removes a schedule class from file
     *
     * @param context
     * @param fitClassId
     * @return
     */
    public static boolean removeScheduleClass(Context context, String fitClassId) {
        // loads schedule classes
        List<FitClass> scheduleClasses;
        try {
            scheduleClasses = loadScheduleClasses(context);
        } catch (IOException e) {
            return false;
        }
        // removes the schedule class
        boolean changes = false;
        Iterator<FitClass> it = scheduleClasses.iterator();
        while (it.hasNext()) {
            if (it.next().getId().equals(fitClassId)) {
                it.remove();
                changes = true;
                break;
            }
        }
        // if no changes made
        if (!changes) {
            return true;
        }
        // saves the schedule classes
        try {
            saveScheduleClasses(context, scheduleClasses);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * returns a schedule class from file
     *
     * @param context
     * @param fitClassId
     * @return
     * @throws IOException
     */
    public static FitClass loadScheduleClass(Context context, String fitClassId) throws IOException {
        List<FitClass> fclasses = loadScheduleClasses(context);
        for (FitClass fclass : fclasses) {
            if (fclass.getId().equals(fitClassId)) {
                return fclass;
            }
        }
        return null;
    }

    // remove schedule classes not valid anymore
    public static void cleanAndMergeClasses(Context context, List<FitClass> scheduleClasses, ArrayList<FitClass> reservedClassesAux) {
        List<FitClass> classesToRemove = new ArrayList<>();
        Iterator<FitClass> it = scheduleClasses.iterator();

        while (it.hasNext()) {
            FitClass f = it.next();
            // remove schedule classes before 1 hour ago
            try {
                Calendar hourAgo = Calendar.getInstance();
                hourAgo.add(Calendar.HOUR_OF_DAY, -1);
                if (FitHelper.getClassDate(f).before(hourAgo)) {
                    classesToRemove.add(f);
                    it.remove();
                }
            } catch (ParseException e) {
                continue;
            }
            // remove if the class is already reserved
            if (reservedClassesAux.contains(f)) {
                classesToRemove.add(f);
                it.remove();
            }
        }

        // remove schedule from storage
        for (FitClass f : classesToRemove) {
            StorageHelper.removeScheduleClass(context, f.getId());
        }
    }
}
