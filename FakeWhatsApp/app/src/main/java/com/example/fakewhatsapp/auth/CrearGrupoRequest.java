package com.example.fakewhatsapp.auth;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CrearGrupoRequest {
    @SerializedName("p_nombre_grupo")
    private String nombreGrupo;

    @SerializedName("p_creador_id")
    private String creadorId;

    @SerializedName("p_miembros_ids")
    private List<String> miembrosIds;

    public CrearGrupoRequest(String nombreGrupo, String creadorId, List<String> miembrosIds) {
        this.nombreGrupo = nombreGrupo;
        this.creadorId = creadorId;
        this.miembrosIds = miembrosIds;
    }
}