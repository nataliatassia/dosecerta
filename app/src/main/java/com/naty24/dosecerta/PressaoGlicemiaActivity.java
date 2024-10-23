package com.naty24.dosecerta;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PressaoGlicemiaActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ListView listViewHistorico;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listaHistorico;
    private int itemSelecionadoId = -1; // Para armazenar o ID do item selecionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressao_glicemia);

        // Inicializando o DBHelper
        dbHelper = new DBHelper(this);

        // Inicializando a lista de histórico e o adaptador
        listaHistorico = new ArrayList<>();
        listViewHistorico = findViewById(R.id.listViewHistorico);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, listaHistorico);
        listViewHistorico.setAdapter(adapter);
        listViewHistorico.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // Permite seleção única

        // Carregar o histórico ao iniciar a atividade
        carregarHistorico();

        // Ação do botão de salvar os dados de pressão e glicemia
        Button btnSalvarPressao = findViewById(R.id.btn_salvar_pressao);
        btnSalvarPressao.setOnClickListener(v -> {
            String pressao = ((EditText) findViewById(R.id.et_pressao)).getText().toString().trim();
            String glicemia = ((EditText) findViewById(R.id.et_glicemia)).getText().toString().trim();

            // Verifica se os campos não estão vazios
            if (pressao.isEmpty() || glicemia.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Valida se a pressão está no formato correto (ex: 120/80)
            if (!pressao.matches("\\d{1,3}/\\d{1,3}")) {
                Toast.makeText(this, "Por favor, insira a pressão no formato correto (ex: 120/80).", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtendo a data atual no formato dd/MM/yyyy
            String data = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            // Inserindo os dados no banco de dados
            salvarDadosNoBanco(pressao, glicemia, data);
        });

        // Ação do botão de voltar
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish()); // Fecha a activity

        // Ação do botão de excluir
        findViewById(R.id.btn_excluir).setOnClickListener(v -> {
            if (itemSelecionadoId != -1) {
                excluirDados(itemSelecionadoId);
                itemSelecionadoId = -1; // Resetando o ID após a exclusão
            } else {
                Toast.makeText(this, "Selecione um item para excluir.", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para seleção de item
        listViewHistorico.setOnItemClickListener((parent, view, position, id) -> {
            itemSelecionadoId = getIdFromHistorico(position); // Obter o ID do item selecionado
        });
    }

    // Método para salvar os dados no banco de dados
    private void salvarDadosNoBanco(String pressao, String glicemia, String data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("pressao", pressao);
        values.put("glicemia", glicemia);
        values.put("data", data);

        long result = db.insert("pressao_glicemia", null, values);
        db.close();

        // Verifica se os dados foram inseridos com sucesso
        if (result != -1) {
            Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show();
            carregarHistorico(); // Atualiza o histórico após salvar
        } else {
            Toast.makeText(this, "Erro ao salvar os dados.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para carregar o histórico de pressão e glicemia
    private void carregarHistorico() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query("pressao_glicemia", null, null, null, null, null, "data DESC"); // Ordenar por data

            listaHistorico.clear(); // Limpa a lista antes de carregar novos dados
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id")); // Supondo que você tenha uma coluna "id"
                    @SuppressLint("Range") String pressao = cursor.getString(cursor.getColumnIndex("pressao"));
                    @SuppressLint("Range") String glicemia = cursor.getString(cursor.getColumnIndex("glicemia"));
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("data"));
                    listaHistorico.add("ID: " + id + ", Data: " + data + ", Pressão: " + pressao + ", Glicemia: " + glicemia);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao carregar histórico: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        adapter.notifyDataSetChanged(); // Atualiza o adaptador
    }

    // Método para excluir dados do banco de dados
    private void excluirDados(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete("pressao_glicemia", "id = ?", new String[]{String.valueOf(id)});
        db.close();

        if (rowsAffected > 0) {
            Toast.makeText(this, "Dados excluídos com sucesso!", Toast.LENGTH_SHORT).show();
            carregarHistorico(); // Atualiza o histórico após a exclusão
        } else {
            Toast.makeText(this, "Erro ao excluir os dados.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para obter o ID do item do histórico
    private int getIdFromHistorico(int position) {
        String item = listaHistorico.get(position);
        String[] partes = item.split(", "); // Divide a string para obter o ID
        String idString = partes[0].split(": ")[1]; // Obtém o ID da primeira parte
        return Integer.parseInt(idString); // Retorna o ID como inteiro
    }
}
