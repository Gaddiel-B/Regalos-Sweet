package com.example.regalossweet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificacionReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "entregas_sweet";

    @Override
    public void onReceive(Context context, Intent intent){
        String cliente = intent.getStringExtra("cliente");
        String horaEntrega = intent.getStringExtra("hora");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Canal de Notificación.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alertas de Entrega", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        //Sonido por defecto.
        Uri sonidoUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //Notificación.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) //cambiar por logo.
                .setContentTitle("¡Entrega próxima en 2 Horas!")
                .setContentText("Pedido de: " + cliente + " programado para las " + horaEntrega)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(sonidoUri)
                .setAutoCancel(true);
        //ID aleatorio (Para no sobreescribir notificaciones)
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}
