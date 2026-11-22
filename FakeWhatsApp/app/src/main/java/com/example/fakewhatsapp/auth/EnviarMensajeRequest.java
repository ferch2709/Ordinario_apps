package com.example.fakewhatsapp.auth;

import com.google.gson.annotations.SerializedName;

public class EnviarMensajeRequest {
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

    public EnviarMensajeRequest(String conversacionId, String emisorId, String contenido) {
        this.conversacionId = conversacionId;
        this.emisorId = emisorId;
        this.contenido = contenido;
        this.tipoMensaje = "texto";
        this.mediaUrl = null;
    }

    public EnviarMensajeRequest(String conversacionId, String emisorId, String contenido, String tipoMensaje, String mediaUrl) {
        this.conversacionId = conversacionId;
        this.emisorId = emisorId;
        this.contenido = contenido;
        this.tipoMensaje = tipoMensaje;
        this.mediaUrl = mediaUrl;
    }
}