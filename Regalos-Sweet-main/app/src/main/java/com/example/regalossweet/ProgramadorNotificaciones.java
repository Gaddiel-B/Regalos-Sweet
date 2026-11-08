package com.example.regalossweet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProgramadorNotificaciones {
    public static void programarAlarma(Context context, String cliente, String fecha, String hora){
        try{
            String fechaHoraString = fecha + " " + hora;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM--dd HH:mm", Locale.getDefault());
            Date fechaEntrega = sdf.parse(fechaHoraString);

            if(fechaEntrega != null){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(fechaEntrega);

                //Restar 2 horas a la fecha de entrega
                calendar.add(Calendar.HOUR_OF_DAY, -2);
                //Evita alarmas pasadas por si la entrega inmediata
                if(calendar.before(Calendar.getInstance())){
                    return;
                }
                //Intent -> Receiver
                Intent intent = new Intent(context, NotificacionReceiver.class);
                intent.putExtra("cliente", cliente);
                intent.putExtra("hora", hora);

                int requestCode = (int) System.currentTimeMillis();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if(alarmManager != null){
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
    }
}
