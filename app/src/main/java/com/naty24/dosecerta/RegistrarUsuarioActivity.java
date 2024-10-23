package com.naty24.dosecerta;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrarUsuarioActivity extends AppCompatActivity {
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);
        dbHelper = new DBHelper(this);

        EditText etUsuario = findViewById(R.id.et_usuario);
        EditText etSenha = findViewById(R.id.et_senha);
        Button btnRegistrar = findViewById(R.id.btn_registrar);

        btnRegistrar.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString();
            String senha = etSenha.getText().toString();

            if (!usuario.isEmpty() && !senha.isEmpty()) {
                if (usuarioJaCadastrado(usuario)) {
                    Toast.makeText(this, "Usuário já cadastrado!", Toast.LENGTH_SHORT).show();
                } else {
                    registrarUsuario(usuario, senha);
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean usuarioJaCadastrado(String usuario) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {"usuario"};
        String selection = "usuario = ?";
        String[] selectionArgs = {usuario};

        Cursor cursor = null;
        boolean existe = false;

        try {
            cursor = db.query("usuarios", columns, selection, selectionArgs, null, null, null);
            existe = cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close(); // Fecha o cursor para evitar vazamento de memória
            }
            db.close(); // Fecha o banco de dados
        }

        return existe;
    }

    private void registrarUsuario(String usuario, String senha) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario", usuario);
        values.put("senha", senha); // Considere usar uma função de hash para a senha

        long id = db.insert("usuarios", null, values);

        if (id != -1) {
            Toast.makeText(this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show();
            // Aqui você pode salvar o ID do usuário, se necessário, ou fazer outras operações.
            finish();
        } else {
            Toast.makeText(this, "Erro ao registrar usuário!", Toast.LENGTH_SHORT).show();
        }
        db.close(); // Fecha o banco de dados após a operação
    }
}
