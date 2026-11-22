package com.example.fakewhatsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fakewhatsapp.auth.ChatPreview;
import com.example.fakewhatsapp.auth.SessionManager;
import com.example.fakewhatsapp.databinding.ActivityMainBinding;
import com.example.fakewhatsapp.network.ApiClient;
import com.example.fakewhatsapp.network.MyFirebaseMessagingService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private ChatAdapter adapter;
    private final List<ChatPreview> listaChats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // 1. Verificación de sesión: si NO está logueado, redirigir al Login
        if (!sessionManager.isLoggedIn()) {
            irALogin();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Configurar Toolbar
        binding.mainToolbar.setTitle("FakeWhatsApp");
        setSupportActionBar(binding.mainToolbar);

        // 3. Configurar RecyclerView de Chats
        binding.rvChats.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(listaChats, chat -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("conversacion_id", chat.getConversacionId());
            intent.putExtra("destinatario_nombre", chat.getOtroNombre());
            intent.putExtra("destinatario_foto", chat.getOtroFoto());
            intent.putExtra("destinatario_id", chat.getOtroUsuarioId());
            startActivity(intent);
        });
        binding.rvChats.setAdapter(adapter);

        // 4. Botón flotante para abrir contactos y crear nuevo chat individual
        binding.fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ContactsActivity.class));
        });

        // 5. Configurar Notificaciones Push y canal FCM
        configurarFCM();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar conversaciones cada vez que regresemos a esta pantalla
        cargarConversaciones();
    }

    private void cargarConversaciones() {
        binding.progressBar.setVisibility(View.VISIBLE);

        String token = "Bearer " + sessionManager.getAccessToken();
        String myFilter = "eq." + sessionManager.getUserId();

        ApiClient.getChatApi().getMisChats(token, myFilter, "fecha_ultimo_mensaje.desc.nullslast")
                .enqueue(new Callback<List<ChatPreview>>() {
                    @Override
                    public void onResponse(Call<List<ChatPreview>> call, Response<List<ChatPreview>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            listaChats.clear();
                            listaChats.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            binding.tvEmptyChats.setVisibility(listaChats.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ChatPreview>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void configurarFCM() {
        // Crear canal de notificación de alta prioridad
        MyFirebaseMessagingService.crearCanalNotificacion(this);

        // Permiso de notificaciones en tiempo de ejecución para Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        // Obtener el token de Firebase del dispositivo y registrarlo en Supabase
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();
                        MyFirebaseMessagingService.enviarTokenASupabase(fcmToken, sessionManager);
                    }
                });
    }

    // Menú de opciones (tres puntos arriba a la derecha)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Nuevo Grupo");
        menu.add(0, 2, 1, "Mi Perfil");
        menu.add(0, 3, 2, "Cerrar sesión");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            startActivity(new Intent(this, CreateGroupActivity.class));
            return true;
        } else if (item.getItemId() == 2) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (item.getItemId() == 3) {
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        String token = "Bearer " + sessionManager.getAccessToken();
        ApiClient.getAuthApi().signOut(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                sessionManager.clearSession();
                irALogin();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sessionManager.clearSession();
                irALogin();
            }
        });
    }

    private void irALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}