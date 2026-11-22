package com.example.fakewhatsapp.network;

import com.example.fakewhatsapp.auth.Perfil;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProfileApi {

    // 1. Consultar todos los contactos menos a mí mismo
    @GET("rest/v1/perfiles")
    Call<List<Perfil>> getContactos(
            @Header("Authorization") String bearerToken,
            @Query("select") String select,
            @Query("id") String notMyIdFilter // Ejemplo: "neq.mi-uuid"
    );

    // 2. Consultar mi propio perfil
    @GET("rest/v1/perfiles")
    Call<List<Perfil>> getMiPerfil(
            @Header("Authorization") String bearerToken,
            @Query("id") String myIdFilter // Ejemplo: "eq.mi-uuid"
    );

    // 3. Actualizar nombre, estado o foto de mi perfil
    @PATCH("rest/v1/perfiles")
    Call<ResponseBody> updatePerfil(
            @Header("Authorization") String bearerToken,
            @Query("id") String myIdFilter,
            @Body Map<String, Object> body
    );

    // 4. Subir imagen binaria al bucket 'avatars' de Supabase Storage
    @POST("storage/v1/object/avatars/{imageName}")
    Call<ResponseBody> uploadAvatar(
            @Header("Authorization") String bearerToken,
            @Header("Content-Type") String contentType,
            @Path("imageName") String imageName,
            @Body RequestBody imageBytes
    );

    // Actualizar última conexión
    @PATCH("rest/v1/perfiles")
    Call<ResponseBody> actualizarUltimaConexion(
            @Header("Authorization") String bearerToken,
            @Query("id") String myIdFilter,
            @Body Map<String, Object> body
    );

    // 5. Guardar o actualizar token en la tabla 'dispositivos' (upsert)
    @POST("rest/v1/dispositivos")
    Call<okhttp3.ResponseBody> registrarDispositivo(
            @Header("Authorization") String bearerToken,
            @Header("Prefer") String prefer, // "resolution=merge-duplicates"
            @Body com.example.fakewhatsapp.auth.DispositivoRequest request
    );
}