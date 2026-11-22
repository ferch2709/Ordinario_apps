package com.example.fakewhatsapp.network;

import com.example.fakewhatsapp.auth.ChatPreview;
import com.example.fakewhatsapp.auth.CrearChatRequest;
import com.example.fakewhatsapp.auth.CrearGrupoRequest;
import com.example.fakewhatsapp.auth.EnviarMensajeRequest;
import com.example.fakewhatsapp.auth.Mensaje;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApi {

    @POST("rest/v1/rpc/obtener_o_crear_chat")
    Call<String> obtenerOCrearChat(
            @Header("Authorization") String bearerToken,
            @Body CrearChatRequest request
    );

    @POST("rest/v1/rpc/crear_grupo")
    Call<String> crearGrupo(
            @Header("Authorization") String bearerToken,
            @Body CrearGrupoRequest request
    );

    @GET("rest/v1/vista_chats")
    Call<List<ChatPreview>> getMisChats(
            @Header("Authorization") String bearerToken,
            @Query("mi_usuario_id") String miIdFilter,
            @Query("order") String order
    );

    @GET("rest/v1/mensajes")
    Call<List<Mensaje>> getHistorialMensajes(
            @Header("Authorization") String bearerToken,
            @Query("conversacion_id") String conversacionIdFilter,
            @Query("order") String order
    );

    @POST("rest/v1/mensajes")
    Call<Void> enviarMensaje(
            @Header("Authorization") String bearerToken,
            @Header("Prefer") String prefer,
            @Body EnviarMensajeRequest request
    );

    @DELETE("rest/v1/mensajes")
    Call<ResponseBody> eliminarMensaje(
            @Header("Authorization") String bearerToken,
            @Query("id") String idFilter
    );

    @PATCH("rest/v1/mensajes")
    Call<ResponseBody> marcarComoLeidos(
            @Header("Authorization") String bearerToken,
            @Query("conversacion_id") String conversacionFilter,
            @Query("emisor_id") String notMyIdFilter,
            @Body Map<String, Object> body
    );

    @POST("storage/v1/object/chat-media/{imageName}")
    Call<ResponseBody> uploadChatImage(
            @Header("Authorization") String bearerToken,
            @Header("Content-Type") String contentType,
            @Path("imageName") String imageName,
            @Body RequestBody imageBytes
    );
}