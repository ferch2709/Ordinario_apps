package com.example.fakewhatsapp.auth;

import com.google.gson.annotations.SerializedName;

public class ChatPreview {
    @SerializedName("conversacion_id")
    private String conversacionId;

    @SerializedName("otro_usuario_id")
    private String otroUsuarioId;

    @SerializedName("otro_nombre")
    private String otroNombre;

    @SerializedName("otro_foto")
    private String otroFoto;

    @SerializedName("ultimo_mensaje")
    private String ultimoMensaje;

    @SerializedName("fecha_ultimo_mensaje")
    private String fechaUltimoMensaje;

    // Getters
    public String getConversacionId() { return conversacionId; }
    public String getOtroUsuarioId() { return otroUsuarioId; }
    public String getOtroNombre() { return otroNombre != null ? otroNombre : "Usuario"; }
    public String getOtroFoto() { return otroFoto; }
    public String getUltimoMensaje() { return ultimoMensaje; }
    public String getFechaUltimoMensaje() { return fechaUltimoMensaje; }
}