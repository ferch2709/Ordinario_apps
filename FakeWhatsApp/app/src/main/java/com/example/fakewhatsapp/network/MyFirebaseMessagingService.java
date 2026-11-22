package com.example.fakewhatsapp.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.fakewhatsapp.MainActivity;
import com.example.fakewhatsapp.auth.DispositivoRequest;
import com.example.fakewhatsapp.auth.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "fake_whatsapp_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Si el usuario ya inició sesión, enviamos el nuevo token a Supabase
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            enviarTokenASupabase(token, sessionManager);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String titulo = "FakeWhatsApp";
        String cuerpo = "Nuevo mensaje recibido";

        // Parámetros para abrir la conversación correcta
        String conversacionId = null;
        String destinatarioId = null;
        String destinatarioNombre = null;
        String destinatarioFoto = null;

        // 1. Extraer los datos enviados por la Edge Function
        if (!remoteMessage.getData().isEmpty()) {
            conversacionId = remoteMessage.getData().get("conversacion_id");
            destinatarioId = remoteMessage.getData().get("destinatario_id");
            destinatarioNombre = remoteMessage.getData().get("destinatario_nombre");
            destinatarioFoto = remoteMessage.getData().get("destinatario_foto");

            if (destinatarioNombre != null) titulo = destinatarioNombre;
            if (remoteMessage.getData().containsKey("body")) {
                cuerpo = remoteMessage.getData().get("body");
            }
        }

        // Si viene en payload notification estándar
        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                titulo = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                cuerpo = remoteMessage.getNotification().getBody();
            }
        }

        mostrarNotificacion(titulo, cuerpo, conversacionId, destinatarioId, destinatarioNombre, destinatarioFoto);
    }

    private void mostrarNotificacion(String titulo, String cuerpo,
                                     String convId, String destId, String destNombre, String destFoto) {
        crearCanalNotificacion(this);

        Intent intent;

        // Si la notificación incluye los datos del chat, abrimos ChatActivity directamente
        if (convId != null) {
            intent = new Intent(this, com.example.fakewhatsapp.ChatActivity.class);
            intent.putExtra("conversacion_id", convId);
            intent.putExtra("destinatario_id", destId);
            intent.putExtra("destinatario_nombre", destNombre);
            intent.putExtra("destinatario_foto", destFoto);
        } else {
            // Si es una notificación genérica, abrimos la pantalla principal
            intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public static void crearCanalNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Mensajes de FakeWhatsApp",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal de notificaciones para mensajes entrantes");
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void enviarTokenASupabase(String token, SessionManager sessionManager) {
        String authToken = "Bearer " + sessionManager.getAccessToken();
        String modelo = Build.MANUFACTURER + " " + Build.MODEL;

        DispositivoRequest request = new DispositivoRequest(sessionManager.getUserId(), token, modelo);

        ApiClient.getProfileApi().registrarDispositivo(authToken, "resolution=merge-duplicates", request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {}
                });
    }
}