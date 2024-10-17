package com.naty24.dosecerta;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdicionarMedicamentoActivity extends AppCompatActivity {
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_medicamento);
        dbHelper = new DBHelper(this);

        Spinner spinner = findViewById(R.id.spinner_intervalo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.intervalos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            String nome = ((EditText) findViewById(R.id.et_nome)).getText().toString();
            int intervalo = Integer.parseInt(spinner.getSelectedItem().toString().split("/")[0]);
            String ultimaHora = ((EditText) findViewById(R.id.et_ultima_hora)).getText().toString();
            String dataUltima = ((EditText) findViewById(R.id.et_data_ultima)).getText().toString();

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nome", nome);
            values.put("intervalo", intervalo);
            values.put("ultima_hora", ultimaHora);
            values.put("data_ultima", dataUltima);
            String proximoAlarme = calcularProximoAlarme(ultimaHora, intervalo);
            values.put("proximo_alarme", proximoAlarme);

            long id = db.insert("medicamentos", null, values);
            db.close();

            configurarAlarme(proximoAlarme, nome, id); // Configura o alarme
            Toast.makeText(this, "Medicamento adicionado!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private String calcularProximoAlarme(String ultimaHora, int intervalo) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date ultimaData = sdf.parse(ultimaHora);
            if (ultimaData != null) {
                long proximoAlarmeMillis = ultimaData.getTime() + (intervalo * 60 * 60 * 1000); // Adiciona o intervalo em milissegundos
                Date proximoData = new Date(proximoAlarmeMillis);
                return sdf.format(proximoData); // Retorna o próximo horário formatado
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null; // Retorna nulo se houve erro
    }

    @SuppressLint("ScheduleExactAlarm")
    private void configurarAlarme(String hora, String medicamento, long id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicamento", medicamento);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(hora);
            if (date != null) {
                long alarmTime = date.getTime();
                // Verifica se o horário do alarme é no passado
                if (alarmTime <= System.currentTimeMillis()) {
                    alarmTime += AlarmManager.INTERVAL_DAY; // Ajusta para o próximo dia
                }
                // Usa setExact para configurar o alarme
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
