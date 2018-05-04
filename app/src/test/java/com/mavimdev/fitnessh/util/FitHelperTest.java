package com.mavimdev.fitnessh.util;

import android.provider.CalendarContract;

import com.mavimdev.fitnessh.model.FitClass;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CancellationException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class FitHelperTest {

    @Test
    public void classifyClass() throws ParseException {
        // date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");

        /* test Reserved class */
        FitClass reservedClass = new FitClass();
        reservedClass.setDate(dateFormat.format(new Date()));
        reservedClass.setHorario(timeFormat.format(new Date()));
        reservedClass.setClassState(ClassState.RESERVED);
        FitHelper.classifyClass(reservedClass);
        assertEquals(ClassState.RESERVED, reservedClass.getClassState());

        /* test Expired class */
        // one hour before now
        FitClass expiredClass = new FitClass();
        expiredClass.setDate(dateFormat.format(new Date()));
        Calendar pastClass = Calendar.getInstance();
        pastClass.add(Calendar.HOUR_OF_DAY, -1);
        expiredClass.setHorario(timeFormat.format(pastClass.getTime()));
        expiredClass.setVagas(0);
        FitHelper.classifyClass(expiredClass);
        assertEquals(ClassState.EXPIRED, expiredClass.getClassState());
        // one day before now
        pastClass = Calendar.getInstance();
        pastClass.add(Calendar.DAY_OF_MONTH, -1);
        expiredClass.setDate(dateFormat.format(pastClass.getTime()));
        expiredClass.setHorario(timeFormat.format(new Date()));
        FitHelper.classifyClass(expiredClass);
        assertEquals(ClassState.EXPIRED, expiredClass.getClassState());

        /* test Available class */
        FitClass availableClass = new FitClass();
        availableClass.setDate(dateFormat.format(new Date()));
        Calendar futureHour = Calendar.getInstance(); futureHour.add(Calendar.HOUR_OF_DAY, 1);
        availableClass.setHorario(timeFormat.format(futureHour.getTime()));
        availableClass.setVagas(1);
        FitHelper.classifyClass(availableClass);
        assertEquals(ClassState.AVAILABLE, availableClass.getClassState());

        /* test sold out class */
        FitClass soldOutClass = new FitClass();
        soldOutClass.setDate(dateFormat.format(new Date()));
        futureHour = Calendar.getInstance(); futureHour.add(Calendar.HOUR_OF_DAY, 1);
        soldOutClass.setHorario(timeFormat.format(futureHour.getTime()));
        soldOutClass.setVagas(0);
        FitHelper.classifyClass(soldOutClass);
        assertEquals(ClassState.SOLD_OUT, soldOutClass.getClassState());

        /* test unavailable class */
        // tomorrow
        FitClass unavailableClass = new FitClass();
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DAY_OF_MONTH, 1);
        futureDate.add(Calendar.HOUR_OF_DAY, 1);
        unavailableClass.setDate(dateFormat.format(futureDate.getTime()));
        unavailableClass.setHorario(timeFormat.format(futureDate.getTime()));
        FitHelper.classifyClass(unavailableClass);
        assertEquals(ClassState.UNAVAILABLE, unavailableClass.getClassState());

        // tomorrow on an hour before now
        Calendar futureDate2 = Calendar.getInstance();
        futureDate2.add(Calendar.DAY_OF_MONTH, 1);
        futureDate2.add(Calendar.HOUR_OF_DAY, -1);
        unavailableClass.setHorario(timeFormat.format(futureDate2.getTime()));
        FitHelper.classifyClass(unavailableClass);
        assertEquals(ClassState.UNAVAILABLE, unavailableClass.getClassState());

        // after 10 hours without free places
        Calendar futureToday = Calendar.getInstance();
        futureToday.add(Calendar.HOUR_OF_DAY, FitHelper.HOURS_BEFORE_RESERVATION + 1);
        unavailableClass.setDate(dateFormat.format(futureToday.getTime()));
        unavailableClass.setHorario(timeFormat.format(futureToday.getTime()));
        unavailableClass.setVagas(0);
        FitHelper.classifyClass(unavailableClass);
        assertEquals(ClassState.UNAVAILABLE, unavailableClass.getClassState());

        // tomorrow less than 10 hours from "now"
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 21);
        FitClass tomorrowClass = new FitClass();
        Calendar tomorrowDate = Calendar.getInstance();
        tomorrowDate.add(Calendar.DAY_OF_MONTH, 1);
        tomorrowDate.set(Calendar.HOUR_OF_DAY, 6);
        tomorrowClass.setDate(dateFormat.format(tomorrowDate.getTime()));
        tomorrowClass.setHorario(timeFormat.format(tomorrowDate.getTime()));
        tomorrowClass.setVagas(1);
        FitHelper.classifyClass(tomorrowClass);
        assertEquals(ClassState.AVAILABLE, tomorrowClass.getClassState());
    }
}