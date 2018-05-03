package com.mavimdev.fitnessh.model;

import com.google.gson.annotations.SerializedName;
import com.mavimdev.fitnessh.util.ClassState;

/**
 * Created by migue on 18/02/2018.
 */

public class FitClass {
    @SerializedName("id")
    private String id;
    @SerializedName("date")
    private String date;
    // reserved class
    @SerializedName("rdata")
    private String rdata;
    // reserved class
    @SerializedName("mdata")
    private String mdata;
    @SerializedName("horario")
    private String horario;
    // reserved class
    @SerializedName("mhorario")
    private String mhorario;
    @SerializedName("duracao")
    private String duracao;
    // reserved class
    @SerializedName("mduracao")
    private String mduracao;
    @SerializedName("local")
    private String local;
    @SerializedName("localn")
    private String localn;
    @SerializedName("aulan")
    private String aulan;
    @SerializedName("aulal")
    private String aulal;
    @SerializedName("aulad")
    private String aulad;
    @SerializedName("aulapp")
    private String aulapp;
    @SerializedName("aulatm")
    private String aulatm;
    @SerializedName("aulars")
    private String aulars;
    @SerializedName("profn")
    private String profn;
    @SerializedName("profp")
    private String profp;
    @SerializedName("vagas")
    private Integer vagas;
    @SerializedName("moveweek")
    private String moveweek;
    // reserved class - club name
    @SerializedName("title")
    private String title;
    // chosen club - daily classes
    private String ctitle;
    // id de aula reservada
    private String aid;
    // class status
    private ClassState classState;

    public FitClass() {
    }

    public FitClass(String id, String date, String horario, String duracao, String local, String localn, String aulan, String aulal, String aulad, String aulapp, String aulatm, String aulars, String profn, String profp, Integer vagas, String moveweek) {
        this.id = id;
        this.date = date;
        this.horario = horario;
        this.duracao = duracao;
        this.local = local;
        this.localn = localn;
        this.aulan = aulan;
        this.aulal = aulal;
        this.aulad = aulad;
        this.aulapp = aulapp;
        this.aulatm = aulatm;
        this.aulars = aulars;
        this.profn = profn;
        this.profp = profp;
        this.vagas = vagas;
        this.moveweek = moveweek;
    }

    public FitClass(String id, String date, String horario, String duracao, String aulan, String localn, String title) {
        this.id = id;
        this.date = date;
        this.horario = horario;
        this.duracao = duracao;
        this.localn = localn;
        this.aulan = aulan;
        this.title = title;
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

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getLocaln() {
        return localn;
    }

    public void setLocaln(String localn) {
        this.localn = localn;
    }

    public String getAulan() {
        return aulan;
    }

    public void setAulan(String aulan) {
        this.aulan = aulan;
    }

    public String getAulad() {
        return aulad;
    }

    public void setAulad(String aulad) {
        this.aulad = aulad;
    }

    public String getAulapp() {
        return aulapp;
    }

    public void setAulapp(String aulapp) {
        this.aulapp = aulapp;
    }

    public String getAulatm() {
        return aulatm;
    }

    public void setAulatm(String aulatm) {
        this.aulatm = aulatm;
    }

    public String getAulars() {
        return aulars;
    }

    public void setAulars(String aulars) {
        this.aulars = aulars;
    }

    public String getProfn() {
        return profn;
    }

    public void setProfn(String profn) {
        this.profn = profn;
    }

    public String getProfp() {
        return profp;
    }

    public void setProfp(String profp) {
        this.profp = profp;
    }

    public Integer getVagas() {
        return vagas;
    }

    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }

    public String getAulal() {
        return aulal;
    }

    public void setAulal(String aulal) {
        this.aulal = aulal;
    }

    public String getMoveweek() {
        return moveweek;
    }

    public void setMoveweek(String moveweek) {
        this.moveweek = moveweek;
    }

    public String getRdata() {
        return rdata;
    }

    public void setRdata(String rdata) {
        this.rdata = rdata;
    }

    public String getMdata() {
        return mdata;
    }

    public void setMdata(String mdata) {
        this.mdata = mdata;
    }

    public String getMhorario() {
        return mhorario;
    }

    public void setMhorario(String mhorario) {
        this.mhorario = mhorario;
    }

    public String getMduracao() {
        return mduracao;
    }

    public void setMduracao(String mduracao) {
        this.mduracao = mduracao;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCtitle() {
        return ctitle;
    }

    public void setCtitle(String ctitle) {
        this.ctitle = ctitle;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public ClassState getClassState() {
        return classState;
    }

    public void setClassState(ClassState classState) {
        this.classState = classState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FitClass fitClass = (FitClass) o;
        // if same class
        if (id.equals(fitClass.id)) return true;
        // if different class description
        if (!aulan.equals(fitClass.aulan)) return false;
        // if different clubs
        if (ctitle != null || title != null) {
            if (!(ctitle != null ? ctitle : title).equals(fitClass.title != null ? fitClass.title : fitClass.ctitle))
                return false;
        }
        // if both daily classes
        if (horario != null && fitClass.horario != null && !id.equals(fitClass.id)) return false;
        // if both reserved classes
        if (mhorario != null && fitClass.mhorario != null && !id.equals(fitClass.id)) return false;
        // if daily compared with reserved
        if (date != null && !date.equals(fitClass.rdata)) return false;
        if (horario != null && !horario.equals(fitClass.mhorario)) return false;
        // if reserved compared with daily
        if (rdata != null && !rdata.equals(fitClass.date)) return false;
        if (mhorario != null && !mhorario.equals(fitClass.horario)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (horario != null ? horario.hashCode() : 0);
        result = 31 * result + (aulan != null ? aulan.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
