package com.example.trabalhogestao;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// INCREMENTE A VERSÃO e adicione a Categoria
@Database(entities = {Despesa.class, Categoria.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instancia;

    public abstract DespesaDao despesaDao();
    public abstract CategoriaDao categoriaDao(); // Adicione o novo DAO

    public static synchronized AppDatabase getInstancia(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "banco_despesas"
                    )
                    // A migração destrutiva apaga os dados antigos.
                    // Ideal para desenvolvimento. Para produção, seria necessária uma migração real.
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instancia;
    }
}