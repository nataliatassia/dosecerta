package com.naty24.dosecerta;

import android.content.ContentValues;
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
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("usuario", usuario);
                values.put("senha", senha);
                db.insert("usuarios", null, values);
                db.close();

                Toast.makeText(this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
