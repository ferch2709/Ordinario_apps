package com.example.fakewhatsapp.auth;

import com.google.gson.annotations.SerializedName;

public class CrearChatRequest {
    @SerializedName("p_usuario_1")
    private String usuario1;

    @SerializedName("p_usuario_2")
    private String usuario2;

    public CrearChatRequest(String usuario1, String usuario2) {
        this.usuario1 = usuario1;
        this.usuario2 = usuario2;
    }
}