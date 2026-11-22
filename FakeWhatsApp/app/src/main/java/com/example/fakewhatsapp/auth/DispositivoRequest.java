package com.example.fakewhatsapp.auth;

import com.google.gson.annotations.SerializedName;

public class DispositivoRequest {
    @SerializedName("perfil_id")
    private String perfilId;

    @SerializedName("fcm_token")
    private String fcmToken;

    @SerializedName("modelo_dispositivo")
    private String modeloDispositivo;

    public DispositivoRequest(String perfilId, String fcmToken, String modeloDispositivo) {
        this.perfilId = perfilId;
        this.fcmToken = fcmToken;
        this.modeloDispositivo = modeloDispositivo;
    }
}