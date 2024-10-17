package com.naty24.dosecerta;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dbHelper = new DBHelper(this);

        EditText etUsuario = findViewById(R.id.et_usuario);
        EditText etSenha = findViewById(R.id.et_senha);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString();
            String senha = etSenha.getText().toString();

            if (!usuario.isEmpty() && !senha.isEmpty()) {
                if (verificarLogin(usuario, senha)) {
                    // Login bem-sucedido, navegue para a próxima atividade
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                    // Inicie a próxima atividade, por exemplo, a lista de medicamentos
                } else {
                    Toast.makeText(this, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean verificarLogin(String usuario, String senha) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {"id"};
        String selection = "usuario = ? AND senha = ?";
        String[] selectionArgs = {usuario, senha};
        Cursor cursor = db.query("usuarios", columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
}
