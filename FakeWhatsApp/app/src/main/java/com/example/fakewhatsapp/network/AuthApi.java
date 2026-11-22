package com.example.fakewhatsapp.network;

import com.example.fakewhatsapp.auth.AuthRequest;
import com.example.fakewhatsapp.auth.AuthResponse;
import com.example.fakewhatsapp.auth.RecoverRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApi {

    // Registro
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body AuthRequest request);

    // Inicio de sesión
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(@Body AuthRequest request);

    // Recuperación de contraseña
    @POST("auth/v1/recover")
    Call<ResponseBody> recoverPassword(@Body RecoverRequest request);

    // Cerrar sesión
    @POST("auth/v1/logout")
    Call<ResponseBody> signOut(@Header("Authorization") String bearerToken);
}