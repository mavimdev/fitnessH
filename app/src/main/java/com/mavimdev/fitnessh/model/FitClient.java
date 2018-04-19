package com.mavimdev.fitnessh.model;

import com.google.gson.annotations.SerializedName;

public class FitClient {
    @SerializedName("result")
    private String result;
    @SerializedName("myhut_id")
    private String myhutId;
    @SerializedName("myhut_pack")
    private String myhutPack;
    @SerializedName("myhut_name")
    private String myhutName;
    @SerializedName("myhut_nsocio")
    private String myhutNsocio;
    @SerializedName("myhut_club_name")
    private String myhutClubName;
    @SerializedName("myhut_club_id")
    private String myhutClubId;
    @SerializedName("myhut_clubfav")
    private String myhutClubfav;
    @SerializedName("myhut_clinica")
    private String myhutClinica;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMyhutId() {
        return myhutId;
    }

    public void setMyhutId(String myhutId) {
        this.myhutId = myhutId;
    }

    public String getMyhutPack() {
        return myhutPack;
    }

    public void setMyhutPack(String myhutPack) {
        this.myhutPack = myhutPack;
    }

    public String getMyhutName() {
        return myhutName;
    }

    public void setMyhutName(String myhutName) {
        this.myhutName = myhutName;
    }

    public String getMyhutNsocio() {
        return myhutNsocio;
    }

    public void setMyhutNsocio(String myhutNsocio) {
        this.myhutNsocio = myhutNsocio;
    }

    public String getMyhutClubName() {
        return myhutClubName;
    }

    public void setMyhutClubName(String myhutClubName) {
        this.myhutClubName = myhutClubName;
    }

    public String getMyhutClubId() {
        return myhutClubId;
    }

    public void setMyhutClubId(String myhutClubId) {
        this.myhutClubId = myhutClubId;
    }

    public String getMyhutClubfav() {
        return myhutClubfav;
    }

    public void setMyhutClubfav(String myhutClubfav) {
        this.myhutClubfav = myhutClubfav;
    }

    public String getMyhutClinica() {
        return myhutClinica;
    }

    public void setMyhutClinica(String myhutClinica) {
        this.myhutClinica = myhutClinica;
    }
}
