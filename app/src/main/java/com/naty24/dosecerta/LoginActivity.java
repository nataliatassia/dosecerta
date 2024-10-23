package com.naty24.dosecerta;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa o banco de dados
        dbHelper = new DBHelper(this);

        // Inicializa o SharedPreferences
        sharedPreferences = getSharedPreferences("DoseCertaPrefs", MODE_PRIVATE);

        // Verifica se o usuário já está logado
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            long usuarioId = sharedPreferences.getLong("usuario_id", -1);
            Intent intent = new Intent(LoginActivity.this, ListaMedicamentosActivity.class);
            intent.putExtra("usuario_id", usuarioId);
            startActivity(intent);
            finish(); // Finaliza a LoginActivity
        }

        EditText etUsuario = findViewById(R.id.et_usuario);
        EditText etSenha = findViewById(R.id.et_senha);
        Button btnLogin = findViewById(R.id.btn_login);

        // Configurar o clique do botão de login
        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString();
            String senha = etSenha.getText().toString();

            // Validação básica dos campos de entrada
            if (!usuario.isEmpty() && !senha.isEmpty()) {
                long usuarioId = verificarLogin(usuario, senha);
                if (usuarioId != -1) {
                    // Login bem-sucedido, salva estado de login e navega para a ListaMedicamentosActivity
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();

                    // Salva o estado de login no SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putLong("usuario_id", usuarioId); // Salva o ID do usuário
                    editor.apply();

                    // Navega para a ListaMedicamentosActivity
                    Intent intent = new Intent(LoginActivity.this, ListaMedicamentosActivity.class);
                    intent.putExtra("usuario_id", usuarioId); // Passa o ID do usuário
                    startActivity(intent);
                    finish(); // Finaliza a LoginActivity
                } else {
                    Toast.makeText(this, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Função para verificar login no banco de dados
    @SuppressLint("Range")
    private long verificarLogin(String usuario, String senha) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {"id"}; // Supondo que a tabela "usuarios" tenha uma coluna "id"
        String selection = "usuario = ? AND senha = ?";
        String[] selectionArgs = {usuario, senha};

        Cursor cursor = null;
        long userId = -1;

        try {
            cursor = db.query("usuarios", columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getLong(cursor.getColumnIndex("id")); // Obtém o ID do usuário
            }
        } finally {
            if (cursor != null) {
                cursor.close(); // Fecha o cursor para evitar vazamento de memória
            }
            db.close(); // Fecha o banco de dados
        }

        return userId; // Retorna o ID do usuário ou -1 se não encontrado
    }
}
