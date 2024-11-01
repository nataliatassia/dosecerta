package com.naty24.dosecerta;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "medicamento_alertas";

    @Override
    public void onReceive(Context context, Intent intent) {
        String nomeMedicamento = intent.getStringExtra("medicamento");
        long idMedicamento = intent.getLongExtra("id", -1);

        if (nomeMedicamento != null && idMedicamento != -1) {
            exibirNotificacao(context, nomeMedicamento, idMedicamento);
        } else {
            System.err.println("Erro: Nome do medicamento ou ID não passados corretamente.");
        }
    }

    @SuppressLint("NotificationPermission")
    private void exibirNotificacao(Context context, String medicamento, long idMedicamento) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Alertas de Medicamentos",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(context, ListaMedicamentosActivity.class);
        intent.putExtra("medicamento_id", idMedicamento);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) idMedicamento,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Hora de tomar o medicamento!")
                .setContentText("É hora de tomar " + medicamento + "!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify((int) idMedicamento, builder.build());
    }
}
