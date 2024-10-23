package com.naty24.dosecerta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dose_certa.db";
    private static final int DATABASE_VERSION = 3; // Versão do banco de dados

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Criação da tabela de medicamentos
        String CREATE_MEDICAMENTOS_TABLE = "CREATE TABLE IF NOT EXISTS medicamentos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," + // Garantindo que o nome não seja nulo
                "intervalo INTEGER NOT NULL," + // Garantindo que o intervalo não seja nulo
                "ultima_hora TEXT," +
                "data_ultima TEXT," +
                "proximo_alarme TEXT," +
                "usuario_id INTEGER," + // Relaciona ao usuário
                "ativo INTEGER DEFAULT 1," + // Medicamento ativo por padrão
                "dias_tratamento INTEGER DEFAULT 0)"; // Dias de tratamento

        // Criação da tabela de pressão e glicemia
        String CREATE_PRESSAO_GLICEMIA_TABLE = "CREATE TABLE IF NOT EXISTS pressao_glicemia (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "pressao TEXT," +
                "glicemia TEXT," +
                "data TEXT)";

        // Criação da tabela de usuários
        String CREATE_USUARIOS_TABLE = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario TEXT NOT NULL," +
                "senha TEXT NOT NULL)";

        db.execSQL(CREATE_MEDICAMENTOS_TABLE);
        db.execSQL(CREATE_PRESSAO_GLICEMIA_TABLE);
        db.execSQL(CREATE_USUARIOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Atualizando o banco de dados se a versão for inferior a 3
        if (oldVersion < 3) {
            // Verifica se a coluna já não existe antes de tentar adicioná-la
            db.execSQL("ALTER TABLE medicamentos ADD COLUMN dias_tratamento INTEGER DEFAULT 0");
        }
    }

    // Inserir medicamento
    public boolean inserirMedicamento(String nome, int intervalo, String ultimaHora, String dataUltima, String proximoAlarme, int usuarioId, int diasTratamento) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("intervalo", intervalo);
        values.put("ultima_hora", ultimaHora);
        values.put("data_ultima", dataUltima);
        values.put("proximo_alarme", proximoAlarme);
        values.put("usuario_id", usuarioId);
        values.put("dias_tratamento", diasTratamento);

        long result = db.insert("medicamentos", null, values);
        db.close(); // Fecha o banco de dados após a operação
        return result != -1;
    }

    // Inserir dados de pressão e glicemia
    public boolean inserirPressaoGlicemia(String pressao, String glicemia, String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pressao", pressao);
        values.put("glicemia", glicemia);
        values.put("data", data);

        long result = db.insert("pressao_glicemia", null, values);
        db.close(); // Fecha o banco de dados após a operação
        return result != -1;
    }

    // Inserir usuário
    public boolean inserirUsuario(String usuario, String senha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario", usuario);
        values.put("senha", senha);

        long result = db.insert("usuarios", null, values);
        db.close(); // Fecha o banco de dados após a operação
        return result != -1;
    }

    // Verificar login do usuário
    public boolean verificarUsuario(String usuario, String senha) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM usuarios WHERE usuario = ? AND senha = ?", new String[]{usuario, senha});
        boolean usuarioEncontrado = cursor.getCount() > 0;
        cursor.close(); // Fecha o cursor após a operação
        db.close(); // Fecha o banco de dados
        return usuarioEncontrado;
    }

    // Buscar todos os medicamentos de um usuário
    public Cursor buscarMedicamentos(int usuarioId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Certifique-se de que o id do usuário é válido
        if (usuarioId <= 0) {
            db.close(); // Fecha o banco se o ID não for válido
            return null;
        }
        return db.rawQuery("SELECT * FROM medicamentos WHERE usuario_id = ? AND ativo = 1", new String[]{String.valueOf(usuarioId)});
    }

    // Atualizar medicamento
    public boolean atualizarMedicamento(int id, String nome, int intervalo, String ultimaHora, String dataUltima, String proximoAlarme, int diasTratamento) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("intervalo", intervalo);
        values.put("ultima_hora", ultimaHora);
        values.put("data_ultima", dataUltima);
        values.put("proximo_alarme", proximoAlarme);
        values.put("dias_tratamento", diasTratamento);

        int result = db.update("medicamentos", values, "id = ?", new String[]{String.valueOf(id)});
        db.close(); // Fecha o banco de dados após a operação
        return result > 0;
    }

    // Desativar medicamento (marcar como inativo)
    public boolean desativarMedicamento(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ativo", 0); // Define o medicamento como inativo

        int result = db.update("medicamentos", values, "id = ?", new String[]{String.valueOf(id)});
        db.close(); // Fecha o banco de dados após a operação
        return result > 0;
    }

    // Método para buscar os alarmes (opcional)
    public Cursor buscarAlarmesPorUsuario(int usuarioId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM medicamentos WHERE usuario_id = ? AND ativo = 1", new String[]{String.valueOf(usuarioId)});
    }
}
