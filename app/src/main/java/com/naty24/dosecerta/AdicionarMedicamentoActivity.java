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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdicionarMedicamentoActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private long usuarioId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_medicamento);

        // Inicializando o banco de dados
        dbHelper = new DBHelper(this);

        // Recuperando o ID do usuário a partir das preferências compartilhadas
        SharedPreferences sharedPreferences = getSharedPreferences("DoseCertaPrefs", MODE_PRIVATE);
        usuarioId = sharedPreferences.getLong("usuario_id", -1);

        // Verificação de autenticação do usuário
        if (usuarioId == -1) {
            Toast.makeText(this, "Usuário não autenticado. Faça login novamente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurando o Spinner de intervalos
        Spinner spinner = findViewById(R.id.spinner_intervalo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.intervalos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Configuração do botão "Salvar"
        findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            String nome = ((EditText) findViewById(R.id.et_nome)).getText().toString().trim();
            String ultimaHora = ((EditText) findViewById(R.id.et_ultima_hora)).getText().toString().trim();
            String dataUltima = ((EditText) findViewById(R.id.et_data_ultima)).getText().toString().trim();

            // Verificar se os campos obrigatórios estão preenchidos
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

            // Inicializando o banco de dados
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nome", nome);
            values.put("intervalo", intervalo);
            values.put("ultima_hora", ultimaHora);
            values.put("data_ultima", dataUltima);
            values.put("usuario_id", usuarioId);

            // Calculando o próximo alarme
            Calendar calendar = calcularProximosAlarmes(ultimaHora, dataUltima, intervalo);
            if (calendar != null) {
                // Loop para definir alarmes até o final do dia final do tratamento (23:59)
                while (calendar.getTimeInMillis() <= System.currentTimeMillis() + AlarmManager.INTERVAL_DAY) {
                    values.put("proximo_alarme", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime()));
                    long id = db.insert("medicamentos", null, values);
                    if (id != -1) {
                        // Configurando o alarme para cada entrada
                        configurarAlarme(calendar, nome, id);
                    }
                    calendar.add(Calendar.HOUR_OF_DAY, intervalo);
                }
                Toast.makeText(this, "Medicamento adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao calcular o próximo alarme.", Toast.LENGTH_SHORT).show();
            }
            db.close();
        });

        // Configuração do botão "Voltar"
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
    }

    /**
     * Método para calcular os próximos alarmes com base na última hora, data final e intervalo.
     * @param ultimaHora Hora da última dose (formato HH:mm).
     * @param dataUltima Data final do tratamento (formato dd/MM/yyyy).
     * @param intervalo Intervalo em horas entre cada dose.
     * @return Calendar com o próximo alarme calculado.
     */
    private Calendar calcularProximosAlarmes(String ultimaHora, String dataUltima, int intervalo) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            // Parseia a data final e a última hora informadas pelo usuário
            Date ultimaData = sdf.parse(dataUltima + " " + ultimaHora);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ultimaData);
            calendar.add(Calendar.HOUR_OF_DAY, intervalo); // Adiciona o intervalo para a próxima dose
            return calendar; // Retorna o próximo alarme
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Método para configurar alarmes com base no Calendar.
     * Solicita permissões para alarmes exatos se necessário (para Android 12 ou superior).
     * @param calendar Data e hora do próximo alarme.
     * @param medicamento Nome do medicamento.
     * @param id ID do medicamento no banco de dados.
     */
    @SuppressLint({"ScheduleExactAlarm", "MissingPermission"})
    public void configurarAlarme(Calendar calendar, String medicamento, long id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Verifica se o dispositivo está rodando Android 12 (API 31) ou superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Solicita a permissão para agendar alarmes exatos, se necessário
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;  // Saia do método até que a permissão seja concedida
            }
        }

        // Configuração do intent para o alarme
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicamento", medicamento);
        intent.putExtra("medicamento_id", id);

        // Configurando o PendingIntent para o alarme
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // Define o alarme com precisão
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
