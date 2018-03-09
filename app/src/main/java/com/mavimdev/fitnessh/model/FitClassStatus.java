package com.mavimdev.fitnessh.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by migue on 04/03/2018.
 */

public class FitClassStatus {

    public FitClassStatus(String status) {
        this.status = status;
    }

    @SerializedName("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
