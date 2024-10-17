package com.naty24.dosecerta;

import android.annotation.SuppressLint;
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

public class MainActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listaMedicamentos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        listView = findViewById(R.id.listViewMedicamentos);
        listaMedicamentos = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMedicamentos);
        listView.setAdapter(adapter);

        // Carregar medicamentos ao iniciar a atividade
        carregarMedicamentos();

        // Configurar os botões para navegação
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_adicionar_medicamento).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdicionarMedicamentoActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_pressao_glicemia).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PressaoGlicemiaActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_lista_medicamentos).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListaMedicamentosActivity.class);
            startActivity(intent);
        });
    }

    private void carregarMedicamentos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("medicamentos", null, null, null, null, null, null);
        listaMedicamentos.clear();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String nome = cursor.getString(cursor.getColumnIndex("nome"));
            listaMedicamentos.add(nome); // Adiciona o nome do medicamento à lista
        }
        cursor.close();
        adapter.notifyDataSetChanged(); // Atualiza a lista
    }
}
