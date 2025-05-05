package com.example.ac2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class BackgroundService extends Service {

    private BancoHelper bancoHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        bancoHelper = new BancoHelper(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification(String medicamentoNome) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, "default")
                .setContentTitle("Hora do Remédio")
                .setContentText("Está na hora de tomar: " + medicamentoNome)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(medicamentoNome.hashCode(), notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            verificarEMostrarNotificacoes();
        }).start();
        return START_STICKY;
    }

    private void verificarEMostrarNotificacoes() {
        Cursor cursor = bancoHelper.listarHorariosMedicamentos();
        if (cursor.moveToFirst()) {
            do {
                String nome = cursor.getString(cursor.getColumnIndex(BancoHelper.COLUNA_NOME));
                String horario = cursor.getString(cursor.getColumnIndex(BancoHelper.COLUNA_HORARIO));
                int tomado = cursor.getInt(cursor.getColumnIndex(BancoHelper.COLUNA_TOMADO));

                if (tomado == 0 && estaNoHorario(horario)) {
                    showNotification(nome);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private boolean estaNoHorario(String horario) {
        try {
            String[] partes = horario.split(":");
            int hora = Integer.parseInt(partes[0]);
            int minuto = Integer.parseInt(partes[1]);

            Calendar agora = Calendar.getInstance();
            int horaAtual = agora.get(Calendar.HOUR_OF_DAY);
            int minutoAtual = agora.get(Calendar.MINUTE);

            // Verifica se estamos dentro da faixa de horário (+/-5 minutos)
            int minutosAgendados = hora * 60 + minuto;
            int minutosAtuais = horaAtual * 60 + minutoAtual;

            return Math.abs(minutosAtuais - minutosAgendados) <= 5;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
