package com.example.fakewhatsapp;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.fakewhatsapp.auth.EnviarMensajeRequest;
import com.example.fakewhatsapp.auth.Mensaje;
import com.example.fakewhatsapp.auth.Perfil;
import com.example.fakewhatsapp.auth.SessionManager;
import com.example.fakewhatsapp.databinding.ActivityChatBinding;
import com.example.fakewhatsapp.network.ApiClient;
import com.example.fakewhatsapp.network.RealtimeManager;
import com.example.fakewhatsapp.network.SupabaseClientConfig;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private SessionManager sessionManager;
    private MessageAdapter adapter;
    private final List<Mensaje> listaMensajes = new ArrayList<>();
    private final List<Mensaje> listaMensajesCompleta = new ArrayList<>();
    private RealtimeManager realtimeManager;

    private String conversacionId;
    private String destinatarioNombre;
    private String destinatarioFoto;
    private String destinatarioId;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) subirYEnviarImagen(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        conversacionId = getIntent().getStringExtra("conversacion_id");
        destinatarioNombre = getIntent().getStringExtra("destinatario_nombre");
        destinatarioFoto = getIntent().getStringExtra("destinatario_foto");
        destinatarioId = getIntent().getStringExtra("destinatario_id");

        binding.tvDestinatarioNombre.setText(destinatarioNombre != null ? destinatarioNombre : "Chat");
        if (destinatarioFoto != null && !destinatarioFoto.isEmpty()) {
            Glide.with(this).load(destinatarioFoto).into(binding.imgDestinatario);
        }

        // Tocar el avatar para ver la foto de perfil en grande
        binding.imgDestinatario.setOnClickListener(v -> mostrarFotoAmpliada(destinatarioFoto, destinatarioNombre));

        setSupportActionBar(binding.chatToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.chatToolbar.setNavigationOnClickListener(v -> finish());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);

        // Adaptador con eliminación en clic largo
        adapter = new MessageAdapter(listaMensajes, sessionManager.getUserId(), (mensaje, position) -> {
            mostrarDialogoEliminar(mensaje, position);
        });
        binding.rvMessages.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> enviarMensajeTexto());
        binding.btnAttach.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        configurarDeteccionEscritura();
        consultarHistorial();
        marcarMensajesComoLeidos();
        consultarUltimaConexion();
        activarTiempoReal();
    }

    private void mostrarFotoAmpliada(String urlFoto, String nombre) {
        if (urlFoto == null || urlFoto.isEmpty()) return;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_foto_perfil);
        ImageView iv = dialog.findViewById(R.id.ivFullPhoto);
        TextView tv = dialog.findViewById(R.id.tvDialogName);
        tv.setText(nombre);
        Glide.with(this).load(urlFoto).into(iv);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void mostrarDialogoEliminar(Mensaje mensaje, int position) {
        if (!mensaje.getEmisorId().equals(sessionManager.getUserId())) {
            Toast.makeText(this, "Solo puedes eliminar tus propios mensajes", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar mensaje")
                .setMessage("¿Deseas eliminar este mensaje para todos?")
                .setPositiveButton("Eliminar para todos", (d, w) -> {
                    String token = "Bearer " + sessionManager.getAccessToken();
                    ApiClient.getChatApi().eliminarMensaje(token, "eq." + mensaje.getId()).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                if (position >= 0 && position < listaMensajes.size()) {
                                    listaMensajes.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Buscar mensaje...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarMensajes(newText);
                return true;
            }
        });
        return true;
    }

    private void filtrarMensajes(String texto) {
        List<Mensaje> filtrados = new ArrayList<>();
        if (texto.isEmpty()) {
            filtrados.addAll(listaMensajesCompleta);
        } else {
            for (Mensaje m : listaMensajesCompleta) {
                if (m.getContenido() != null && m.getContenido().toLowerCase().contains(texto.toLowerCase())) {
                    filtrados.add(m);
                }
            }
        }
        adapter.actualizarLista(filtrados);
    }

    private void subirYEnviarImagen(Uri imageUri) {
        Toast.makeText(this, "Enviando imagen...", Toast.LENGTH_SHORT).show();
        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
            byte[] bytes = byteBuffer.toByteArray();
            is.close();

            String fileName = "chat_" + System.currentTimeMillis() + ".jpg";
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            String token = "Bearer " + sessionManager.getAccessToken();

            ApiClient.getChatApi().uploadChatImage(token, "image/jpeg", fileName, requestBody).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        String urlPublica = SupabaseClientConfig.SUPABASE_URL + "storage/v1/object/public/chat-media/" + fileName;
                        EnviarMensajeRequest req = new EnviarMensajeRequest(conversacionId, sessionManager.getUserId(), "", "imagen", urlPublica);
                        ApiClient.getChatApi().enviarMensaje(token, "return=minimal", req).enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> c, Response<Void> r) {}
                            @Override public void onFailure(Call<Void> c, Throwable t) {}
                        });
                    }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void configurarDeteccionEscritura() {
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (realtimeManager != null) realtimeManager.emitirEscribiendo(s.length() > 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void activarTiempoReal() {
        realtimeManager = new RealtimeManager(conversacionId, sessionManager.getUserId(), new RealtimeManager.RealtimeCallback() {
            @Override
            public void onNuevoMensaje(Mensaje mensaje) {
                for (Mensaje m : listaMensajesCompleta) {
                    if (m.getId() != null && m.getId().equals(mensaje.getId())) return;
                }
                listaMensajesCompleta.add(mensaje);
                listaMensajes.add(mensaje);
                adapter.notifyItemInserted(listaMensajes.size() - 1);
                binding.rvMessages.smoothScrollToPosition(listaMensajes.size() - 1);
                if (!mensaje.getEmisorId().equals(sessionManager.getUserId())) marcarMensajesComoLeidos();
            }

            @Override
            public void onMensajeActualizado(Mensaje mensajeActualizado) {
                for (int i = 0; i < listaMensajes.size(); i++) {
                    if (listaMensajes.get(i).getId().equals(mensajeActualizado.getId())) {
                        listaMensajes.set(i, mensajeActualizado);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onUsuarioEscribiendo(String usuarioId, boolean estaEscribiendo) {
                if (estaEscribiendo) {
                    binding.tvSubtitulo.setText("escribiendo...");
                    binding.tvSubtitulo.setTextColor(android.graphics.Color.parseColor("#25D366"));
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        binding.tvSubtitulo.setText("en línea");
                        binding.tvSubtitulo.setTextColor(android.graphics.Color.parseColor("#E0E0E0"));
                    }, 3000);
                }
            }

            @Override
            public void onMensajeEliminado(String mensajeId) {
                for (int i = 0; i < listaMensajes.size(); i++) {
                    if (listaMensajes.get(i).getId().equals(mensajeId)) {
                        listaMensajes.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }
        });
        realtimeManager.conectar();
    }

    private void marcarMensajesComoLeidos() {
        String token = "Bearer " + sessionManager.getAccessToken();
        Map<String, Object> body = new HashMap<>();
        body.put("estado", "leido");
        ApiClient.getChatApi().marcarComoLeidos(token, "eq." + conversacionId, "neq." + sessionManager.getUserId(), body).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    private void consultarUltimaConexion() {
        if (destinatarioId == null) return;
        String token = "Bearer " + sessionManager.getAccessToken();
        ApiClient.getProfileApi().getMiPerfil(token, "eq." + destinatarioId).enqueue(new Callback<List<Perfil>>() {
            @Override
            public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                binding.tvSubtitulo.setText("en línea");
            }
            @Override public void onFailure(Call<List<Perfil>> call, Throwable t) {}
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Map<String, Object> myUpdate = new HashMap<>();
        myUpdate.put("ultima_conexion", sdf.format(new Date()));
        ApiClient.getProfileApi().actualizarUltimaConexion(token, "eq." + sessionManager.getUserId(), myUpdate).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    private void consultarHistorial() {
        String token = "Bearer " + sessionManager.getAccessToken();
        ApiClient.getChatApi().getHistorialMensajes(token, "eq." + conversacionId, "creado_en.asc").enqueue(new Callback<List<Mensaje>>() {
            @Override
            public void onResponse(Call<List<Mensaje>> call, Response<List<Mensaje>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaMensajesCompleta.clear();
                    listaMensajesCompleta.addAll(response.body());
                    listaMensajes.clear();
                    listaMensajes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (!listaMensajes.isEmpty()) binding.rvMessages.scrollToPosition(listaMensajes.size() - 1);
                }
            }
            @Override public void onFailure(Call<List<Mensaje>> call, Throwable t) {}
        });
    }

    private void enviarMensajeTexto() {
        String texto = binding.etMessage.getText().toString().trim();
        if (texto.isEmpty()) return;
        binding.etMessage.setText("");

        String token = "Bearer " + sessionManager.getAccessToken();
        EnviarMensajeRequest req = new EnviarMensajeRequest(conversacionId, sessionManager.getUserId(), texto);
        ApiClient.getChatApi().enviarMensaje(token, "return=minimal", req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realtimeManager != null) realtimeManager.desconectar();
    }
}