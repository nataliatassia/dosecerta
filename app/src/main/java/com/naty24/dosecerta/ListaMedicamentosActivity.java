package com.naty24.dosecerta;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ListaMedicamentosActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<Medicamento> listaMedicamentos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_medicamentos);

        dbHelper = new DBHelper(this);
        listView = findViewById(R.id.listViewMedicamentos);
        listaMedicamentos = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        carregarMedicamentos();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Medicamento medicamento = listaMedicamentos.get(position);
            if (medicamento.isAlarmeAtivo()) {
                desativarAlarme(medicamento.getId());
                medicamento.setAlarmeAtivo(false);
                Toast.makeText(this, "Alarme desativado para " + medicamento.getNome(), Toast.LENGTH_SHORT).show();
            } else {
                configurarAlarme(medicamento);
                medicamento.setAlarmeAtivo(true);
                Toast.makeText(this, "Alarme ativado para " + medicamento.getNome(), Toast.LENGTH_SHORT).show();
            }
            atualizarLista();
        });
    }

    private void carregarMedicamentos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Obtendo o usuario_id do usuário logado (substitua pelo método adequado de obtenção do id do usuário)
        long usuarioId = getIntent().getLongExtra("usuario_id", -1);

        // Filtra os medicamentos pelo usuario_id
        String selection = "usuario_id = ?";
        String[] selectionArgs = new String[]{String.valueOf(usuarioId)};

        Cursor cursor = db.query("medicamentos", null, selection, selectionArgs, null, null, null);
        listaMedicamentos.clear();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex("id"));
            @SuppressLint("Range") String nome = cursor.getString(cursor.getColumnIndex("nome"));
            @SuppressLint("Range") String proximoAlarme = cursor.getString(cursor.getColumnIndex("proximo_alarme"));
            boolean ativo = true; // Implementar lógica para verificar se o alarme está ativo

            listaMedicamentos.add(new Medicamento(id, nome, proximoAlarme, ativo));
        }
        cursor.close();
        atualizarLista();
    }

    private void atualizarLista() {
        adapter.clear();
        for (Medicamento medicamento : listaMedicamentos) {
            adapter.add(medicamento.getNome() + " - Próximo Alarme: " + medicamento.getProximoAlarme() +
                    (medicamento.isAlarmeAtivo() ? " (Ativo)" : " (Inativo)"));
        }
        adapter.notifyDataSetChanged();
    }

    private void desativarAlarme(long id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private void configurarAlarme(Medicamento medicamento) {
        // Lógica para configurar o alarme
        // Utilize o método que já criamos para configurar o alarme em AdicionarMedicamentoActivity
        String proximoAlarme = medicamento.getProximoAlarme();
        // Exemplo de lógica para configurar o alarme (caso já tenha uma função, chame-a)
        // configureAlarm(proximoAlarme, medicamento.getNome(), medicamento.getId());
    }

    private static class Medicamento {
        private long id;
        private String nome;
        private String proximoAlarme;
        private boolean alarmeAtivo;

        public Medicamento(long id, String nome, String proximoAlarme, boolean alarmeAtivo) {
            this.id = id;
            this.nome = nome;
            this.proximoAlarme = proximoAlarme;
            this.alarmeAtivo = alarmeAtivo;
        }

        public long getId() { return id; }
        public String getNome() { return nome; }
        public String getProximoAlarme() { return proximoAlarme; }
        public boolean isAlarmeAtivo() { return alarmeAtivo; }
        public void setAlarmeAtivo(boolean alarmeAtivo) { this.alarmeAtivo = alarmeAtivo; }
    }

}
