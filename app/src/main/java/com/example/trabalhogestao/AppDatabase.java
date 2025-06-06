package com.example.trabalhogestao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Despesa.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instancia;

    public abstract DespesaDao despesaDao();

    public static synchronized AppDatabase getInstancia(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "banco_despesas"
            ).fallbackToDestructiveMigration().build();
        }
        return instancia;
    }
}
