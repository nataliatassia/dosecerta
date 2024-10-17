package com.naty24.dosecerta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Obtém o nome do medicamento passado pelo Intent
        String nomeMedicamento = intent.getStringExtra("medicamento");
        exibirNotificacao(context, nomeMedicamento);
    }

    private void exibirNotificacao(Context context, String medicamento) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "medicamento_alertas";

        // Criação do canal de notificação para Android O e superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Alertas de Medicamentos", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Configuração da notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Substitua pelo seu ícone
                .setContentTitle("Hora de tomar o medicamento!")
                .setContentText("É hora de tomar " + medicamento)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Envio da notificação
        notificationManager.notify((int) System.currentTimeMillis(), builder.build()); // Usando um ID de notificação único
    }
}
