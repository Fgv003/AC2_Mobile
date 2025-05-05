package com.example.ac2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BancoHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medicamentos.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABELA_MEDICAMENTOS = "medicamentos";
    public static final String COLUNA_ID = "id";
    public static final String COLUNA_NOME = "nome";
    public static final String COLUNA_TOMADO = "tomado";
    public static final String COLUNA_HORARIO = "horario";

    public BancoHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEDICAMENTOS_TABLE = "CREATE TABLE " + TABELA_MEDICAMENTOS + " (" +
                COLUNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUNA_NOME + " TEXT, " +
                COLUNA_TOMADO + " INTEGER, " +
                COLUNA_HORARIO + " TEXT)";
        db.execSQL(CREATE_MEDICAMENTOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_MEDICAMENTOS);
        onCreate(db);
    }

    public long inserirMedicamento(String nome, String horario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUNA_NOME, nome);
        values.put(COLUNA_TOMADO, 0);
        values.put(COLUNA_HORARIO, horario);

        long result = db.insert(TABELA_MEDICAMENTOS, null, values);
        db.close();
        return result;
    }

    public Cursor listarMedicamentos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABELA_MEDICAMENTOS, null);
    }

    public boolean marcarComoTomado(String medicamentoNome) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUNA_TOMADO, 1);

        int rowsUpdated = db.update(TABELA_MEDICAMENTOS, values, COLUNA_NOME + " = ?", new String[]{medicamentoNome});
        return rowsUpdated > 0;
    }

    public int excluirMedicamento(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int resultado = db.delete(TABELA_MEDICAMENTOS, COLUNA_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return resultado;
    }
    public Cursor listarHorariosMedicamentos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABELA_MEDICAMENTOS, null);
    }
}
