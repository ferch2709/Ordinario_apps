package com.example.fakewhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fakewhatsapp.auth.CrearChatRequest;
import com.example.fakewhatsapp.auth.Perfil;
import com.example.fakewhatsapp.auth.SessionManager;
import com.example.fakewhatsapp.databinding.ActivityContactsBinding;
import com.example.fakewhatsapp.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsActivity extends AppCompatActivity {

    private ActivityContactsBinding binding;
    private SessionManager sessionManager;
    private ContactAdapter adapter;
    private final List<Perfil> listaContactos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // 1. Configurar barra superior (Toolbar)
        binding.toolbar.setTitle("Seleccionar contacto");
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 2. Configurar RecyclerView y acción al tocar un contacto
        binding.rvContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(listaContactos, perfilSeleccionado -> {
            crearOabrirConversacion(perfilSeleccionado);
        });
        binding.rvContacts.setAdapter(adapter);

        // 3. Consultar usuarios en Supabase
        consultarUsuarios();
    }

    private void consultarUsuarios() {
        binding.progressBar.setVisibility(View.VISIBLE);

        String token = "Bearer " + sessionManager.getAccessToken();
        String notMyId = "neq." + sessionManager.getUserId();

        ApiClient.getProfileApi().getContactos(token, "*", notMyId).enqueue(new Callback<List<Perfil>>() {
            @Override
            public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    listaContactos.clear();
                    listaContactos.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (listaContactos.isEmpty()) {
                        Toast.makeText(ContactsActivity.this, "No hay otros contactos registrados", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ContactsActivity.this, "Error al cargar contactos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Perfil>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ContactsActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearOabrirConversacion(Perfil perfilSeleccionado) {
        binding.progressBar.setVisibility(View.VISIBLE);

        String token = "Bearer " + sessionManager.getAccessToken();
        CrearChatRequest request = new CrearChatRequest(sessionManager.getUserId(), perfilSeleccionado.getId());

        ApiClient.getChatApi().obtenerOCrearChat(token, request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    String conversacionId = response.body();

                    // Abrir la pantalla de chat con los datos del contacto
                    Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                    intent.putExtra("conversacion_id", conversacionId);
                    intent.putExtra("destinatario_nombre", perfilSeleccionado.getNombre());
                    intent.putExtra("destinatario_foto", perfilSeleccionado.getFotoUrl());
                    startActivity(intent);

                    // Cerramos la lista de contactos para que quede el chat activo
                    finish();
                } else {
                    Toast.makeText(ContactsActivity.this, "Error al crear la conversación", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ContactsActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}