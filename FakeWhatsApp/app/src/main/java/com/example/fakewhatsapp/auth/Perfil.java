package com.example.fakewhatsapp.auth;

import com.google.gson.annotations.SerializedName;

public class Perfil {
    @SerializedName("id")
    private String id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("estado_info")
    private String estadoInfo;

    @SerializedName("foto_url")
    private String fotoUrl;

    @SerializedName("telefono")
    private String telefono;

    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre != null ? nombre : "Sin Nombre"; }
    public String getEstadoInfo() { return estadoInfo != null ? estadoInfo : "¡Hola! Estoy usando FakeWhatsApp"; }
    public String getFotoUrl() { return fotoUrl; }
    public String getTelefono() { return telefono; }
}
