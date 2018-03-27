package com.mavimdev.fitnessh.util;

import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.CompoundButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.model.ScheduleClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by migue on 04/03/2018.
 */

public class FitHelper {
    public final static int HOURS_BEFORE_RESERVATION = 10;
    public final static String RESERVATION_PASSWORD = "e94b10f0da8d42095ca5c20927416de5";

    // user details - organize this
    public static final String FITNESS_HUT_CLUB_ID = "30";
    public static final String FITNESS_HUT_CLUB_TITLE = "Antas";
    public static final String PACK_FITNESS_HUT = "FOU__N__";
    public static final String CLIENT_ID = "216053";
    public static final long BOOK_REPEATING_TIME = 5 * 1000;
    public static final String SCHEDULE_INTENT_ACTION = "com.mavim.ACTION_SCHEDULE";
    public static final String COM_MAVIM_FITNESS_FIT_CLASS_ID = "com.mavim.fitnessH.fitClassId";
    public static final String SCHEDULE_INFO_FILE_PATH = "/FitData/scheduleclasses.fit";


    public static void classifyClass(FitClass fit) throws ParseException {
        // check state of class
        Calendar now = Calendar.getInstance();
        Calendar classDate = Calendar.getInstance();
        classDate.setTime(new SimpleDateFormat("yyyy-MM-dd|H:mm")
                .parse(fit.getDate().concat("|").concat(fit.getHorario())));

        if (fit.getClassState() != ClassState.RESERVED) {
            if (now.after(classDate)) {
                fit.setClassState(ClassState.EXPIRED);
            } else if (classDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
                if (fit.getVagas() > 0) {
                    fit.setClassState(ClassState.AVAILABLE);
                } else {
                    fit.setClassState(ClassState.SOLD_OUT);
                }
            } else if (classDate.get(Calendar.DAY_OF_MONTH) > now.get(Calendar.DAY_OF_MONTH)) {
                fit.setClassState(ClassState.UNAVAILABLE);
            }
        }
    }

    public static boolean checkIfReserved(FitClass fit, ArrayList<FitClass> reservedClasses) {
        for (FitClass rfit : reservedClasses) {
            if (rfit.equals(fit)) {
                fit.setClassState(ClassState.RESERVED);
                fit.setAid(rfit.getId());
                return true;
            }
        }
        return false;
    }

    public static void tintClass(FitClass fclass, CompoundButton tgBtnReserveClass) {
        if (fclass.getClassState() == ClassState.RESERVED) {
            tgBtnReserveClass.setTextColor(Color.GREEN);
            tgBtnReserveClass.setText("RESERVADA");
        } else if (fclass.getClassState() == ClassState.EXPIRED) {
            tgBtnReserveClass.setTextColor(Color.GRAY);
            tgBtnReserveClass.setText("EXPIRADA");
            tgBtnReserveClass.setEnabled(false);
        } else if (fclass.getClassState() == ClassState.SOLD_OUT) {
            tgBtnReserveClass.setTextColor(Color.RED);
            tgBtnReserveClass.setText("ESGOTADA");
        } else if (fclass.getClassState() == ClassState.UNAVAILABLE) {
            tgBtnReserveClass.setTextColor(Color.GRAY);
            tgBtnReserveClass.setText("INDISPONIVEL");
        }
    }

    public static Calendar calculateEnrollmentClassDate(FitClass fitClass) throws ParseException {
        Calendar classDate = Calendar.getInstance();
        classDate.setTime(new SimpleDateFormat("yyyy-MM-dd|H:mm")
                .parse(fitClass.getDate().concat("|").concat(fitClass.getHorario())));
        // get x hours before class - when the enrollment starts
        classDate.add(Calendar.HOUR, -FitHelper.HOURS_BEFORE_RESERVATION);
        return classDate;
    }

    public static boolean saveScheduleClassToStorage(FitClass fitClass) {
        File fitFile = new File(Environment.getDataDirectory()
                + SCHEDULE_INFO_FILE_PATH);

        String fileContent = new String();
        // read the file if exists. if not, creates one.
        if (fitFile.exists()) {
            try (FileInputStream fis = new FileInputStream(fitFile)) {
                byte[] data = new byte[(int) fitFile.length()];
                fis.read(data);
                fileContent = new String(data, "UTF-8");
            } catch (IOException e) {
                Log.e(FitHelper.class.getSimpleName(), e.getMessage());
                return false;
            }
        } else {
            fitFile.getParentFile().mkdirs();
            try {
                fitFile.createNewFile();
            } catch (IOException e) {
                Log.e(FitHelper.class.getSimpleName(), e.getMessage());
                return false;
            }
        }
        // converts the content of file to list object
        Gson gson = new GsonBuilder().create();
        List<ScheduleClasses> scheduleClasses =
                gson.fromJson(fileContent, new TypeToken<List<ScheduleClasses>>() {}.getType());
        // adds the new schedule class if doesn't exists
        ScheduleClasses fClass = new ScheduleClasses(fitClass.getId(), fitClass.getDate());
        if (!scheduleClasses.contains(fClass)) {
            scheduleClasses.add(fClass);
        }
        // saves again the schedule classes on file
        String classesJson = gson.toJson(scheduleClasses, new TypeToken<List<ScheduleClasses>>() {}.getType());
        try (FileOutputStream fos = new FileOutputStream(fitFile)) {
            fos.write(classesJson.getBytes());
        } catch (IOException e) {
            Log.e(FitHelper.class.getSimpleName(), e.getMessage());
            return false;
        }

        return true;
    }
}
