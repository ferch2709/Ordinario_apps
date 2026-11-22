package com.example.fakewhatsapp.auth;

import com.google.gson.annotations.SerializedName;

public class Mensaje {
    @SerializedName("id")
    private String id;

    @SerializedName("conversacion_id")
    private String conversacionId;

    @SerializedName("emisor_id")
    private String emisorId;

    @SerializedName("contenido")
    private String contenido;

    @SerializedName("tipo_mensaje")
    private String tipoMensaje;

    @SerializedName("media_url")
    private String mediaUrl;

    @SerializedName("estado")
    private String estado;

    @SerializedName("creado_en")
    private String creadoEn;

    // Getters
    public String getId() { return id; }
    public String getConversacionId() { return conversacionId; }
    public String getEmisorId() { return emisorId; }
    public String getContenido() { return contenido; }
    public String getTipoMensaje() { return tipoMensaje != null ? tipoMensaje : "texto"; }
    public String getMediaUrl() { return mediaUrl; }
    public String getEstado() { return estado; }
    public String getCreadoEn() { return creadoEn; }
}