package com.naty24.dosecerta;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dose_certa.db";
    private static final int DATABASE_VERSION = 2; // Atualize a versão do banco de dados

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEDICAMENTOS_TABLE = "CREATE TABLE medicamentos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT," +
                "intervalo INTEGER," +
                "ultima_hora TEXT," +
                "data_ultima TEXT," +
                "proximo_alarme TEXT," +
                "usuario_id INTEGER," + // Adiciona a coluna usuario_id
                "ativo INTEGER DEFAULT 1)"; // 1 = ativo, 0 = inativo

        String CREATE_PRESSAO_GLICEMIA_TABLE = "CREATE TABLE pressao_glicemia (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "pressao TEXT," +
                "glicemia TEXT," +
                "data TEXT)";

        String CREATE_USUARIOS_TABLE = "CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario TEXT," +
                "senha TEXT)";

        db.execSQL(CREATE_MEDICAMENTOS_TABLE);
        db.execSQL(CREATE_PRESSAO_GLICEMIA_TABLE);
        db.execSQL(CREATE_USUARIOS_TABLE); // Cria a tabela de usuários
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS medicamentos");
        db.execSQL("DROP TABLE IF EXISTS pressao_glicemia");
        db.execSQL("DROP TABLE IF EXISTS usuarios"); // Adiciona a tabela de usuários na atualização
        onCreate(db);
    }
}
