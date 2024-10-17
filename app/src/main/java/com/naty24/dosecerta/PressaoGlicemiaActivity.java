package com.naty24.dosecerta;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PressaoGlicemiaActivity extends AppCompatActivity {
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressao_glicemia);
        dbHelper = new DBHelper(this);

        findViewById(R.id.btn_salvar_pressao).setOnClickListener(v -> {
            String pressao = ((EditText) findViewById(R.id.et_pressao)).getText().toString();
            String glicemia = ((EditText) findViewById(R.id.et_glicemia)).getText().toString();
            String data = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("pressao", pressao);
            values.put("glicemia", glicemia);
            values.put("data", data);

            db.insert("pressao_glicemia", null, values);
            db.close();

            Toast.makeText(this, "Dados salvos!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
