package com.mavimdev.fitnessh.util;

import android.graphics.Color;
import android.widget.CompoundButton;

import com.mavimdev.fitnessh.model.FitClass;

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
    public static final String SP_FAVORITE_CLUB_ID = "SP_FAVORITE_CLUB_ID";
    public static final String SP_CLIENT_ID = "SP_CLIENT_ID";

    // user details - organize this
    public static String FITNESS_HUT_CLUB_ID;
    public static String FITNESS_HUT_CLUB_TITLE;
    public static  String PACK_FITNESS_HUT = "FOU__N__";
    public static  String CLIENT_ID;

    public static final String SCHEDULE_INTENT_ACTION = "com.mavim.ACTION_SCHEDULE";
    public static final String COM_MAVIM_FITNESS_FIT_CLASS_ID = "com.mavim.fitnessH.fitClassId";
    public static final String SCHEDULE_INFO_FILE = "scheduleclasses.fit";
    public static final String CLASS_NOT_AVAILABLE = "NÃO PODE RESERVAR A AULA! AULA INDISPONÍVEL.";
    public static final String CLASS_RESERVED = "AULA RESERVADA.";
    public static final int MAX_ATTEMPTS = 15;
    public static final int ATTEMPTS_SECONDS_REPEAT = 7;


    public static void classifyClass(FitClass fit) throws ParseException {
        // check state of class
        Calendar now = Calendar.getInstance();
        Calendar classDate = Calendar.getInstance();

        SimpleDateFormat dateFormat;

        if (ClassState.RESERVED.equals(fit.getClassState()) && fit.getMdata() != null) {
            dateFormat = new SimpleDateFormat("d MMM|H:mm");
            classDate.setTime(dateFormat.parse(fit.getMdata().concat("|").concat(fit.getMhorario())));
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd|H:mm");
            classDate.setTime(dateFormat.parse(fit.getDate().concat("|").concat(fit.getHorario())));
        }

        Calendar afterReservationHours = Calendar.getInstance();
        afterReservationHours.add(Calendar.HOUR_OF_DAY, HOURS_BEFORE_RESERVATION);

        if (fit.getClassState() != ClassState.RESERVED && fit.getClassState() != ClassState.SCHEDULE) {
            if (now.after(classDate)) {
                fit.setClassState(ClassState.EXPIRED);
            } else if (classDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
                if (fit.getVagas() > 0) {
                    fit.setClassState(ClassState.AVAILABLE);
                } else if (classDate.before(afterReservationHours)) {
                    fit.setClassState(ClassState.SOLD_OUT);
                } else {
                    fit.setClassState(ClassState.UNAVAILABLE);
                }
            } else if (classDate.get(Calendar.DAY_OF_MONTH) > now.get(Calendar.DAY_OF_MONTH)) {
                fit.setClassState(ClassState.UNAVAILABLE);
            }
        }
    }


    public static void tintClass(FitClass fclass, CompoundButton swBtnReserveClass) {
        if (fclass.getClassState() == ClassState.AVAILABLE) {
            swBtnReserveClass.setTextColor(Color.GREEN);
            swBtnReserveClass.setText("DISPONIVEL");
            swBtnReserveClass.setChecked(false);
        } else if (fclass.getClassState() == ClassState.RESERVED) {
            swBtnReserveClass.setTextColor(Color.BLUE);
            swBtnReserveClass.setText("RESERVADA");
            swBtnReserveClass.setChecked(true);
        } else if (fclass.getClassState() == ClassState.SCHEDULE) {
            swBtnReserveClass.setTextColor(Color.YELLOW);
            swBtnReserveClass.setText("AGENDADA");
            swBtnReserveClass.setChecked(true);
        } else if (fclass.getClassState() == ClassState.EXPIRED) {
            swBtnReserveClass.setTextColor(Color.GRAY);
            swBtnReserveClass.setText("EXPIRADA");
            swBtnReserveClass.setChecked(false);
            swBtnReserveClass.setEnabled(false);
        } else if (fclass.getClassState() == ClassState.SOLD_OUT) {
            swBtnReserveClass.setTextColor(Color.RED);
            swBtnReserveClass.setText("ESGOTADA");
            swBtnReserveClass.setChecked(false);
        } else if (fclass.getClassState() == ClassState.UNAVAILABLE) {
            swBtnReserveClass.setTextColor(Color.GRAY);
            swBtnReserveClass.setText("INDISPONIVEL");
            swBtnReserveClass.setChecked(false);
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


    public static boolean markIfReserved(FitClass fit, ArrayList<FitClass> reservedClasses) {
        for (FitClass rfit : reservedClasses) {
            if (rfit.equals(fit)) {
                fit.setClassState(ClassState.RESERVED);
                fit.setAid(rfit.getId());
                return true;
            }
        }
        return false;
    }


    public static boolean markIfSchedule(FitClass fit, List<FitClass> scheduleClasses) {
        for (FitClass sfit : scheduleClasses) {
            if (sfit.getId().equals(fit.getId())) {
                fit.setClassState(ClassState.SCHEDULE);
                return true;
            }
        }
        return false;
    }
}
