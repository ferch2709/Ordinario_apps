package com.example.fakewhatsapp.network;

import android.os.Handler;
import android.os.Looper;

import com.example.fakewhatsapp.auth.Mensaje;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RealtimeManager {

    public interface RealtimeCallback {
        void onNuevoMensaje(Mensaje mensaje);
        void onMensajeActualizado(Mensaje mensaje);
        void onUsuarioEscribiendo(String usuarioId, boolean estaEscribiendo);
        void onMensajeEliminado(String mensajeId);
    }

    private WebSocket webSocket;
    private ScheduledExecutorService heartbeatScheduler;
    private final String conversacionId;
    private final String miUsuarioId;
    private final RealtimeCallback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public RealtimeManager(String conversacionId, String miUsuarioId, RealtimeCallback callback) {
        this.conversacionId = conversacionId;
        this.miUsuarioId = miUsuarioId;
        this.callback = callback;
    }

    public void conectar() {
        String wsUrl = SupabaseClientConfig.SUPABASE_URL
                .replace("https://", "wss://")
                .replace("http://", "ws://")
                + "realtime/v1/websocket?apikey=" + SupabaseClientConfig.SUPABASE_KEY + "&vsn=1.0.0";

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url(wsUrl).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                suscribirse(ws);
                iniciarHeartbeat(ws);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                procesarMensajeSocket(text);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {}
        });
    }

    private void suscribirse(WebSocket ws) {
        String joinJson = "{"
                + "\"topic\": \"realtime:public:mensajes\","
                + "\"event\": \"phx_join\","
                + "\"payload\": {"
                + "  \"config\": {"
                + "    \"broadcast\": {\"self\": false},"
                + "    \"postgres_changes\": ["
                + "      {\"event\": \"*\", \"schema\": \"public\", \"table\": \"mensajes\"}"
                + "    ]"
                + "  }"
                + "},"
                + "\"ref\": \"1\""
                + "}";
        ws.send(joinJson);
    }

    private void iniciarHeartbeat(WebSocket ws) {
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            String heartbeat = "{\"topic\":\"phoenix\",\"event\":\"heartbeat\",\"payload\":{},\"ref\":\"hb\"}";
            ws.send(heartbeat);
        }, 25, 25, TimeUnit.SECONDS);
    }

    public void emitirEscribiendo(boolean escribiendo) {
        if (webSocket != null) {
            String broadcastJson = "{"
                    + "\"topic\": \"realtime:public:mensajes\","
                    + "\"event\": \"broadcast\","
                    + "\"payload\": {"
                    + "  \"event\": \"typing\","
                    + "  \"payload\": {"
                    + "    \"conversacion_id\": \"" + conversacionId + "\","
                    + "    \"usuario_id\": \"" + miUsuarioId + "\","
                    + "    \"escribiendo\": " + escribiendo
                    + "  }"
                    + "},"
                    + "\"ref\": \"type\""
                    + "}";
            webSocket.send(broadcastJson);
        }
    }

    private void procesarMensajeSocket(String jsonString) {
        try {
            JsonObject json = gson.fromJson(jsonString, JsonObject.class);
            if (json == null || !json.has("event")) return;

            String event = json.get("event").getAsString();

            if ("postgres_changes".equals(event)) {
                JsonObject payload = json.getAsJsonObject("payload");
                JsonObject data = payload.getAsJsonObject("data");
                String eventType = data.get("type").getAsString();

                if ("DELETE".equals(eventType)) {
                    JsonObject oldRecord = data.getAsJsonObject("old_record");
                    if (oldRecord != null && oldRecord.has("id")) {
                        String idEliminado = oldRecord.get("id").getAsString();
                        mainHandler.post(() -> callback.onMensajeEliminado(idEliminado));
                    }
                } else {
                    JsonObject record = data.getAsJsonObject("record");
                    Mensaje mensaje = gson.fromJson(record, Mensaje.class);

                    if (mensaje != null && conversacionId.equals(mensaje.getConversacionId())) {
                        if ("INSERT".equals(eventType)) {
                            mainHandler.post(() -> callback.onNuevoMensaje(mensaje));
                        } else if ("UPDATE".equals(eventType)) {
                            mainHandler.post(() -> callback.onMensajeActualizado(mensaje));
                        }
                    }
                }
            } else if ("broadcast".equals(event)) {
                JsonObject payload = json.getAsJsonObject("payload");
                if (payload.has("event") && "typing".equals(payload.get("event").getAsString())) {
                    JsonObject data = payload.getAsJsonObject("payload");
                    String convId = data.get("conversacion_id").getAsString();
                    String emisor = data.get("usuario_id").getAsString();
                    boolean estaEscribiendo = data.get("escribiendo").getAsBoolean();

                    if (conversacionId.equals(convId) && !miUsuarioId.equals(emisor)) {
                        mainHandler.post(() -> callback.onUsuarioEscribiendo(emisor, estaEscribiendo));
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public void desconectar() {
        if (heartbeatScheduler != null) heartbeatScheduler.shutdownNow();
        if (webSocket != null) webSocket.close(1000, "Pantalla cerrada");
    }
}