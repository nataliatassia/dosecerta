package com.naty24.dosecerta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o banco de dados
        dbHelper = new DBHelper(this);

        // Inicializa SharedPreferences para armazenar o estado de login
        sharedPreferences = getSharedPreferences("DoseCertaPrefs", MODE_PRIVATE);

        // Verifica se o usuário está logado
        verificarEstadoDeLogin();

        // Configurar os botões para navegação
        configurarBotoes();
    }

    private void verificarEstadoDeLogin() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        // Mostrar ou esconder botões baseados no estado de login
        if (isLoggedIn) {
            // Se estiver logado, exibe os botões de funcionalidades
            findViewById(R.id.btn_adicionar_medicamento).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_lista_medicamentos).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_pressao_glicemia).setVisibility(View.VISIBLE);

            // Esconder os botões de login e criar conta
            findViewById(R.id.btn_login).setVisibility(View.GONE);
            findViewById(R.id.btn_criar_conta).setVisibility(View.GONE);
        } else {
            // Se não estiver logado, exibe os botões de login e criar conta
            findViewById(R.id.btn_login).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_criar_conta).setVisibility(View.VISIBLE);

            // Esconder os botões de funcionalidades
            findViewById(R.id.btn_adicionar_medicamento).setVisibility(View.GONE);
            findViewById(R.id.btn_lista_medicamentos).setVisibility(View.GONE);
            findViewById(R.id.btn_pressao_glicemia).setVisibility(View.GONE);
        }
    }

    private void configurarBotoes() {
        // Botão para Login
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Botão para Criar Conta
        findViewById(R.id.btn_criar_conta).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrarUsuarioActivity.class);
            startActivity(intent);
        });

        // Botão para Adicionar Medicamento
        findViewById(R.id.btn_adicionar_medicamento).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdicionarMedicamentoActivity.class);
            startActivity(intent);
        });

        // Botão para Lista de Medicamentos
        findViewById(R.id.btn_lista_medicamentos).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListaMedicamentosActivity.class);
            startActivity(intent);
        });

        // Botão para Pressão e Glicemia
        findViewById(R.id.btn_pressao_glicemia).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PressaoGlicemiaActivity.class);
            startActivity(intent);
        });
    }
}
