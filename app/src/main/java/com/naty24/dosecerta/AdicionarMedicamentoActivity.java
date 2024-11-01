package com.naty24.dosecerta;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdicionarMedicamentoActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private long usuarioId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_medicamento);

        dbHelper = new DBHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("DoseCertaPrefs", MODE_PRIVATE);
        usuarioId = sharedPreferences.getLong("usuario_id", -1);

        if (usuarioId == -1) {
            Toast.makeText(this, "Usuário não autenticado. Faça login novamente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Spinner spinner = findViewById(R.id.spinner_intervalo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.intervalos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            String nome = ((EditText) findViewById(R.id.et_nome)).getText().toString().trim();
            String ultimaHora = ((EditText) findViewById(R.id.et_ultima_hora)).getText().toString().trim();
            String dataUltima = ((EditText) findViewById(R.id.et_data_ultima)).getText().toString().trim();

            if (nome.isEmpty() || ultimaHora.isEmpty() || dataUltima.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            int intervalo;
            try {
                intervalo = Integer.parseInt(spinner.getSelectedItem().toString().split("/")[0]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                Toast.makeText(this, "Erro ao obter o intervalo.", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nome", nome);
            values.put("intervalo", intervalo);
            values.put("ultima_hora", ultimaHora);
            values.put("data_ultima", dataUltima);
            values.put("usuario_id", usuarioId);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();

            try {
                // Configura o calendário para a data de término do tratamento
                SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar dataFinalCalendar = Calendar.getInstance();
                dataFinalCalendar.setTime(dateOnlySdf.parse(dataUltima));
                // Ajusta a data final para o final do dia
                dataFinalCalendar.set(Calendar.HOUR_OF_DAY, 23);
                dataFinalCalendar.set(Calendar.MINUTE, 59);
                dataFinalCalendar.set(Calendar.SECOND, 59);

                // Configura o calendário para a data e hora da última dose informada
                calendar.setTime(sdf.parse(dataUltima + " " + ultimaHora));

                // Mantém a data atual do dispositivo como referência, mas define a hora como ultimaHora
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ultimaHora.split(":")[0]));
                startCalendar.set(Calendar.MINUTE, Integer.parseInt(ultimaHora.split(":")[1]));
                startCalendar.set(Calendar.SECOND, 0);
                startCalendar.set(Calendar.MILLISECOND, 0);

                // Adiciona os alarmes começando da última dose até a data final
                while (startCalendar.before(dataFinalCalendar) || startCalendar.equals(dataFinalCalendar)) {
                    // Adiciona o horário atual do alarme na tabela de medicamentos
                    values.put("proximo_alarme", sdf.format(startCalendar.getTime()));
                    long id = db.insert("medicamentos", null, values);

                    // Configura o alarme para o horário calculado
                    if (id != -1) {
                        configurarAlarme(startCalendar, nome, id);
                        Log.d("AdicionarMedicamento", "Alarme configurado para: " + sdf.format(startCalendar.getTime()));
                    }

                    // Incrementa o horário do alarme para o próximo baseado no intervalo
                    startCalendar.add(Calendar.HOUR_OF_DAY, intervalo);

                    // Verifica se o próximo horário ultrapassa a data final
                    if (startCalendar.after(dataFinalCalendar)) {
                        break; // Encerra o loop se ultrapassar a data final
                    }
                }

                Toast.makeText(this, "Medicamento adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao analisar a data ou hora.", Toast.LENGTH_SHORT).show();
            } finally {
                db.close();
            }
        });

        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
    }

    @SuppressLint({"ScheduleExactAlarm", "MissingPermission"})
    public void configurarAlarme(Calendar calendar, String medicamento, long id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicamento", medicamento);
        intent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}
