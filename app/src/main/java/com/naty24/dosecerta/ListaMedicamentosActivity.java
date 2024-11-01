package com.naty24.dosecerta;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log; // Adicionado para log de depuração
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ListaMedicamentosActivity extends AppCompatActivity {
    private DBHelper dbHelper; // Instância do DBHelper para acesso ao banco de dados
    private ListView listView; // Lista que exibirá os medicamentos
    private ArrayAdapter<String> adapter; // Adaptador para a ListView
    private List<Medicamento> listaMedicamentos; // Lista de medicamentos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_medicamentos); // Define o layout da atividade

        dbHelper = new DBHelper(this); // Inicializa o DBHelper
        listView = findViewById(R.id.listViewMedicamentos); // Obtém a referência da ListView
        listaMedicamentos = new ArrayList<>(); // Inicializa a lista de medicamentos

        // Inicializa o adaptador da ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, new ArrayList<>());
        listView.setAdapter(adapter); // Define o adaptador para a ListView
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); // Permite seleção múltipla

        // Obtém o ID do usuário logado a partir das preferências compartilhadas
        SharedPreferences sharedPreferences = getSharedPreferences("DoseCertaPrefs", MODE_PRIVATE);
        long usuarioId = sharedPreferences.getLong("usuario_id", -1);

        // Verifica se o usuário está logado
        if (usuarioId == -1) {
            Toast.makeText(this, "Erro ao carregar medicamentos: usuário não encontrado.", Toast.LENGTH_SHORT).show();
            finish(); // Encerra a atividade se o usuário não for encontrado
            return;
        }

        // Adiciona log para depuração
        Log.d("Debug", "usuarioId: " + usuarioId);

        // Carrega os medicamentos do banco de dados
        carregarMedicamentos(usuarioId);

        // Botão para excluir medicamentos selecionados
        Button btnExcluir = findViewById(R.id.btn_excluir);
        btnExcluir.setOnClickListener(v -> excluirMedicamentosSelecionados());

        // Botão para voltar à tela anterior
        Button btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(ListaMedicamentosActivity.this, MainActivity.class);
            startActivity(intent); // Inicia a MainActivity
            finish(); // Encerra a atividade atual
        });
    }

    // Método para carregar medicamentos do banco de dados
    private void carregarMedicamentos(long usuarioId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Obtém uma instância do banco de dados
        Cursor cursor = null;

        try {
            // Seleciona todos os medicamentos associados ao usuário logado
            String selection = "usuario_id = ? AND ativo = 1"; // Garante que apenas medicamentos ativos sejam carregados
            String[] selectionArgs = new String[]{String.valueOf(usuarioId)};
            cursor = db.query("medicamentos", null, selection, selectionArgs, null, null, null);

            // Verifica se o cursor contém resultados
            if (cursor != null && cursor.moveToFirst()) {
                listaMedicamentos.clear(); // Limpa a lista de medicamentos
                adapter.clear(); // Limpa o adaptador

                do {
                    // Obtém os dados do medicamento do cursor
                    @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String nome = cursor.getString(cursor.getColumnIndex("nome"));
                    @SuppressLint("Range") String ultimaHora = cursor.getString(cursor.getColumnIndex("ultima_hora")); // Última hora
                    @SuppressLint("Range") String dataUltima = cursor.getString(cursor.getColumnIndex("data_ultima")); // Data da última dose
                    @SuppressLint("Range") String proximoAlarme = cursor.getString(cursor.getColumnIndex("proximo_alarme")); // Próximo alarme
                    @SuppressLint("Range") int diasTratamento = cursor.getInt(cursor.getColumnIndex("dias_tratamento"));
                    @SuppressLint("Range") int intervalo = cursor.getInt(cursor.getColumnIndex("intervalo"));
                    @SuppressLint("Range") int ativo = cursor.getInt(cursor.getColumnIndex("ativo"));

                    // Adiciona medicamento à lista
                    Medicamento medicamento = new Medicamento(id, nome, ultimaHora, dataUltima, proximoAlarme, diasTratamento, usuarioId, intervalo, ativo == 1);
                    listaMedicamentos.add(medicamento);

                    // Adiciona ao adaptador
                    String proximoAlarmeFormatado = formatarProximoAlarme(medicamento.getProximoAlarme());
                    adapter.add("Medicamento: " + medicamento.getNome() + " - Próximo Alarme: " + proximoAlarmeFormatado +
                            (medicamento.isAlarmeAtivo() ? " (Ativo)" : " (Inativo)"));
                } while (cursor.moveToNext());

                adapter.notifyDataSetChanged(); // Notifica que os dados foram alterados
            } else {
                Toast.makeText(this, "Nenhum medicamento encontrado.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Registra a exceção
            Toast.makeText(this, "Erro ao carregar medicamentos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close(); // Fecha o cursor se não for nulo
            }
            db.close(); // Fecha o banco de dados
        }
    }

    // Método para formatar o próximo alarme
    private String formatarProximoAlarme(String proximoAlarme) {
        // Lógica para formatar a string de data e hora do próximo alarme
        return proximoAlarme; // Substitua esta linha pela lógica de formatação que você precisa
    }

    // Exclui os medicamentos selecionados da lista e do banco de dados
    private void excluirMedicamentosSelecionados() {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Obtém uma instância gravável do banco de dados
        List<Medicamento> medicamentosParaExcluir = new ArrayList<>(); // Lista de medicamentos a serem excluídos

        // Itera pela lista de medicamentos e verifica os selecionados
        for (int i = 0; i < listView.getCount(); i++) {
            if (listView.isItemChecked(i)) {
                medicamentosParaExcluir.add(listaMedicamentos.get(i)); // Adiciona o medicamento à lista de exclusão
            }
        }

        // Exclui cada medicamento selecionado do banco de dados
        for (Medicamento medicamento : medicamentosParaExcluir) {
            db.delete("medicamentos", "id = ?", new String[]{String.valueOf(medicamento.getId())}); // Exclui do banco
            listaMedicamentos.remove(medicamento); // Remove da lista local
        }

        adapter.notifyDataSetChanged(); // Notifica que os dados foram alterados
        Toast.makeText(this, "Medicamentos excluídos com sucesso.", Toast.LENGTH_SHORT).show(); // Mensagem de sucesso
    }
}
