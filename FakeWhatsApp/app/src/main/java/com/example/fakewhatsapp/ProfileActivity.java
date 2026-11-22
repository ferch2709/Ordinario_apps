package com.example.fakewhatsapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fakewhatsapp.auth.Perfil;
import com.example.fakewhatsapp.auth.SessionManager;
import com.example.fakewhatsapp.databinding.ActivityProfileBinding;
import com.example.fakewhatsapp.network.ApiClient;
import com.example.fakewhatsapp.network.SupabaseClientConfig;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager sessionManager;
    private Uri selectedImageUri = null;
    private String fotoUrlActual = null;

    // Selector de fotos moderno de Android
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.imgAvatar.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.imgAvatar.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.btnSaveProfile.setOnClickListener(v -> guardarDatos());

        cargarDatosPerfil();
    }

    private void cargarDatosPerfil() {
        String token = "Bearer " + sessionManager.getAccessToken();
        String myFilter = "eq." + sessionManager.getUserId();

        ApiClient.getProfileApi().getMiPerfil(token, myFilter).enqueue(new Callback<List<Perfil>>() {
            @Override
            public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Perfil perfil = response.body().get(0);
                    binding.etProfileName.setText(perfil.getNombre());
                    binding.etProfileStatus.setText(perfil.getEstadoInfo());
                    fotoUrlActual = perfil.getFotoUrl();

                    if (fotoUrlActual != null && !fotoUrlActual.isEmpty()) {
                        Glide.with(ProfileActivity.this).load(fotoUrlActual).into(binding.imgAvatar);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Perfil>> call, Throwable t) {}
        });
    }

    private void guardarDatos() {
        String nombre = binding.etProfileName.getText().toString().trim();
        String estado = binding.etProfileStatus.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveProfile.setEnabled(false);

        if (selectedImageUri != null) {
            subirFotoYGuardar(nombre, estado);
        } else {
            actualizarEnBaseDeDatos(nombre, estado, fotoUrlActual);
        }
    }

    private void subirFotoYGuardar(String nombre, String estado) {
        try {
            // Forma segura de leer la imagen completa en memoria
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] bytes = byteBuffer.toByteArray();
            inputStream.close();

            // Nombre único para el archivo
            String fileName = "avatar_" + sessionManager.getUserId() + "_" + System.currentTimeMillis() + ".jpg";
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            String token = "Bearer " + sessionManager.getAccessToken();

            ApiClient.getProfileApi().uploadAvatar(token, "image/jpeg", fileName, requestBody)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                // URL pública de la foto en Supabase
                                String urlPublica = SupabaseClientConfig.SUPABASE_URL + "storage/v1/object/public/avatars/" + fileName;
                                actualizarEnBaseDeDatos(nombre, estado, urlPublica);
                            } else {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.btnSaveProfile.setEnabled(true);
                                try {
                                    String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Desconocido";
                                    Toast.makeText(ProfileActivity.this, "Error " + response.code() + ": " + errorMsg, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Error " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSaveProfile.setEnabled(true);
                            Toast.makeText(ProfileActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSaveProfile.setEnabled(true);
            Toast.makeText(ProfileActivity.this, "Error al procesar la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarEnBaseDeDatos(String nombre, String estado, String fotoUrl) {
        String token = "Bearer " + sessionManager.getAccessToken();
        String myFilter = "eq." + sessionManager.getUserId();

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("estado_info", estado);
        if (fotoUrl != null) body.put("foto_url", fotoUrl);

        ApiClient.getProfileApi().updatePerfil(token, myFilter, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveProfile.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "Error al guardar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveProfile.setEnabled(true);
            }
        });
    }
}