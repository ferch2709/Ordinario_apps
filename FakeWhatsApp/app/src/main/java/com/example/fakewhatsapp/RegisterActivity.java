package com.example.fakewhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fakewhatsapp.auth.AuthRequest;
import com.example.fakewhatsapp.auth.AuthResponse;
import com.example.fakewhatsapp.auth.SessionManager;
import com.example.fakewhatsapp.databinding.ActivityRegisterBinding;
import com.example.fakewhatsapp.network.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.btnRegister.setOnClickListener(v -> registrarUsuario());
        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        // Pasamos el nombre en la metadata para el trigger de SQL
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", name);

        AuthRequest request = new AuthRequest(email, password, data);

        ApiClient.getAuthApi().signUp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();

                    // Si no exige confirmación de correo, ya nos devuelve token
                    if (auth.getAccessToken() != null) {
                        sessionManager.saveSession(auth.getAccessToken(), auth.getUser().getId(), auth.getUser().getEmail());
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Cuenta creada con éxito. Inicia sesión.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Error al registrar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}