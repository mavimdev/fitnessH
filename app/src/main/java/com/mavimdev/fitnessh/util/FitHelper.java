package com.mavimdev.fitnessh.util;

import android.graphics.Color;
import android.widget.CompoundButton;

import com.mavimdev.fitnessh.model.FitClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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


    public static void classifyClass(FitClass fit) {
        // check state of class
        Calendar now = Calendar.getInstance();
        Calendar classDate = Calendar.getInstance();
        try {
            classDate.setTime(new SimpleDateFormat("yyyy-MM-dd|H:mm")
                    .parse(fit.getDate().concat("|").concat(fit.getHorario())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (fit.getClassState() == null) {
            if (now.after(classDate)) {
                fit.setClassState(ClassState.EXPIRED);
            } else if (classDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)
                    && fit.getVagas() == 0) {
                fit.setClassState(ClassState.SOLD_OUT);
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
        } else if (fclass.getClassState() == ClassState.SOLD_OUT) {
            tgBtnReserveClass.setTextColor(Color.RED);
            tgBtnReserveClass.setText("ESGOTADA");
        } else if (fclass.getClassState() == ClassState.UNAVAILABLE) {
            tgBtnReserveClass.setTextColor(Color.GRAY);
            tgBtnReserveClass.setText("INDISPONIVEL");
        }
    }

}
