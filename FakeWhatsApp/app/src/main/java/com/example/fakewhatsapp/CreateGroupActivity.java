package com.example.fakewhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fakewhatsapp.auth.CrearGrupoRequest;
import com.example.fakewhatsapp.auth.Perfil;
import com.example.fakewhatsapp.auth.SessionManager;
import com.example.fakewhatsapp.databinding.ActivityCreateGroupBinding;
import com.example.fakewhatsapp.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupActivity extends AppCompatActivity {

    private ActivityCreateGroupBinding binding;
    private SessionManager sessionManager;
    private GroupContactAdapter adapter;
    private final List<Perfil> listaContactos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.toolbarGroup.setTitle("Nuevo Grupo");
        setSupportActionBar(binding.toolbarGroup);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbarGroup.setNavigationOnClickListener(v -> finish());

        binding.rvGroupContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupContactAdapter(listaContactos);
        binding.rvGroupContacts.setAdapter(adapter);

        binding.btnCreateGroupSubmit.setOnClickListener(v -> crearGrupo());

        cargarContactos();
    }

    private void cargarContactos() {
        String token = "Bearer " + sessionManager.getAccessToken();
        ApiClient.getProfileApi().getContactos(token, "*", "neq." + sessionManager.getUserId()).enqueue(new Callback<List<Perfil>>() {
            @Override
            public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaContactos.clear();
                    listaContactos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Perfil>> call, Throwable t) {}
        });
    }

    private void crearGrupo() {
        String nombre = binding.etGroupName.getText().toString().trim();
        List<String> seleccionados = adapter.getSeleccionados();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe un nombre para el grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (seleccionados.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un miembro", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + sessionManager.getAccessToken();
        CrearGrupoRequest request = new CrearGrupoRequest(nombre, sessionManager.getUserId(), seleccionados);

        ApiClient.getChatApi().crearGrupo(token, request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String grupoId = response.body();
                    Intent intent = new Intent(CreateGroupActivity.this, ChatActivity.class);
                    intent.putExtra("conversacion_id", grupoId);
                    intent.putExtra("destinatario_nombre", nombre);
                    intent.putExtra("destinatario_foto", "");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Error al crear grupo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(CreateGroupActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}