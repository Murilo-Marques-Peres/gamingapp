package com.example.mygamingapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "MeuBancoGameplay.db";
    private static final int VERSAO = 1;
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tabela_hoje (id INTEGER PRIMARY KEY, h_ini TEXT, m_ini TEXT, h_fim TEXT, m_fim TEXT);");
        db.execSQL("CREATE TABLE tabela_ontem (id INTEGER PRIMARY KEY, h_ini TEXT, m_ini TEXT, h_fim TEXT, m_fim TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tabela_hoje");
        db.execSQL("DROP TABLE IF EXISTS tabela_ontem");
        onCreate(db);
    }

    public void verificarEResetarViradaDeDia() {
        SharedPreferences pref = context.getSharedPreferences("ControleData", Context.MODE_PRIVATE);
        String dataSalva = pref.getString("ultima_data", "");
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!dataAtual.equals(dataSalva)) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM tabela_ontem");
                db.execSQL("INSERT INTO tabela_ontem (h_ini, m_ini, h_fim, m_fim) SELECT h_ini, m_ini, h_fim, m_fim FROM tabela_hoje");
                db.execSQL("DELETE FROM tabela_hoje");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
            pref.edit().putString("ultima_data", dataAtual).apply();
        }
    }

    public long inserirNoBancoHoje() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("h_ini", "00"); values.put("m_ini", "00");
        values.put("h_fim", "00"); values.put("m_fim", "00");

        long idGerado = db.insert("tabela_hoje", null, values);
        db.close();
        return idGerado;
    }

    public void atualizarHorarioNoBancoHoje(long id, String hIni, String mIni, String hFim, String mFim) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("h_ini", hIni); values.put("m_ini", mIni);
        values.put("h_fim", hFim); values.put("m_fim", mFim);

        db.update("tabela_hoje", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // NOVO: exclui somente o registro de hoje com o ID informado.
    public boolean excluirItemHoje(long id) {
        SQLiteDatabase db = getWritableDatabase();

        int linhasExcluidas = db.delete(
                "tabela_hoje",
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return linhasExcluidas > 0;
    }

    public List<ItemModel> buscarItensHoje() {
        List<ItemModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, h_ini, m_ini, h_fim, m_fim FROM tabela_hoje", null);

        if (cursor.moveToFirst()) {
            do {
                lista.add(new ItemModel(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    public List<ItemModel> buscarItensOntem() {
        List<ItemModel> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, h_ini, m_ini, h_fim, m_fim FROM tabela_ontem", null);

        if (cursor.moveToFirst()) {
            do {
                lista.add(new ItemModel(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    public String calcularTempoTotalHoje() {
        return obterTempoTotalDeTabela("tabela_hoje");
    }

    public String calcularTempoTotalOntem() {
        return obterTempoTotalDeTabela("tabela_ontem");
    }

    private String obterTempoTotalDeTabela(String nomeTabela) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT h_ini, m_ini, h_fim, m_fim FROM " + nomeTabela, null);
        int totalMinutosGameplay = 0;

        if (cursor.moveToFirst()) {
            do {
                int hIni = Integer.parseInt(cursor.getString(0));
                int mIni = Integer.parseInt(cursor.getString(1));
                int hFim = Integer.parseInt(cursor.getString(2));
                int mFim = Integer.parseInt(cursor.getString(3));

                int minutosInicio = (hIni * 60) + mIni;
                int minutosFim = (hFim * 60) + mFim;

                // Trata caso a gameplay passe da meia-noite
                if (minutosFim >= minutosInicio) {
                    totalMinutosGameplay += (minutosFim - minutosInicio);
                } else {
                    totalMinutosGameplay += ((1440 - minutosInicio) + minutosFim);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        int horasFinais = totalMinutosGameplay / 60;
        int minutosFinais = totalMinutosGameplay % 60;

        return String.format(Locale.getDefault(), "%02dh %02dm", horasFinais, minutosFinais);
    }
}