package com.mavimdev.fitnessh.model;

import com.google.gson.annotations.SerializedName;

public class FitClube {
    @SerializedName("id")
    private String id;
//    @SerializedName("webid")
//    private String webid;
//    @SerializedName("date")
//    private String date;
    @SerializedName("title")
    private String title;
//    @SerializedName("cod")
//    private String cod;
//    @SerializedName("desc")
//    private String desc;
//    @SerializedName("photo")
//    private String photo;
//    @SerializedName("localidadeId")
//    private String localidadeId;
//    @SerializedName("localidadeDesc")
//    private String localidadeDesc;
//    @SerializedName("localidadeFiltro")
//    private String localidadeFiltro;
//    @SerializedName("mapa")
//    private String mapa;
//    @SerializedName("slideshow")
//    private String slideshow;
//    @SerializedName("nslideshow")
//    private String nslideshow;
//    @SerializedName("siteNSlideshow")
//    private String siteNSlideshow;
//    @SerializedName("vtour")
//    private String vtour;
//    @SerializedName("video")
//    private String video;
//    @SerializedName("videoMobile")
//    private String videoMobile;
//    @SerializedName("especificacoes")
//    private String especificacoes;
//    @SerializedName("contactos")
//    private String contactos;
//    @SerializedName("morada")
//    private String morada;
//    @SerializedName("lat")
//    private String lat;
//    @SerializedName("lon")
//    private String lon;
//    @SerializedName("email")
//    private String email;
//    @SerializedName("telefone")
//    private String telefone;
//    @SerializedName("horario")
//    private String horario;
//    @SerializedName("servicos")
//    private String servicos;
//    @SerializedName("maulas")
//    private String maulas;
//    @SerializedName("cprivados")
//    private String cprivados;
//    @SerializedName("aInicial")
//    private String aInicial;
//    @SerializedName("gOrientation")
//    private String gOrientation;
//    @SerializedName("fMoves")
//    private String fMoves;
//    @SerializedName("facebook")
//    private String facebook;
//    @SerializedName("nutricao_clinica")
//    private String nutricaoClinica;
//    @SerializedName("myhut")
//    private String myhut;
//    @SerializedName("state")
//    private String state;
//    @SerializedName("url")
//    private String url;

    public FitClube(String id) {
        this.id = id;
    }

    public FitClube(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
