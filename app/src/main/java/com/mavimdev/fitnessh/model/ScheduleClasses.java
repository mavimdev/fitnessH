package com.mavimdev.fitnessh.model;

/**
 * Created by migue on 27/03/2018.
 */

public class ScheduleClasses {
    private String id;
    private String date;


    public ScheduleClasses() {
    }

    public ScheduleClasses(String id, String date) {
        this.id = id;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
