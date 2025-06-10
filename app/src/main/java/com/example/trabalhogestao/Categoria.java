package com.example.trabalhogestao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Locale;

@Entity(tableName = "categorias",
        indices = {@Index(value = "nome_normalizado", unique = true)})
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String nome;
    @NonNull
    @ColumnInfo(name = "nome_normalizado")
    private String nomeNormalizado;
    public Categoria(@NonNull String nome) {
        this.nome = nome;
        this.nomeNormalizado = nome.toLowerCase(Locale.getDefault());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getNome() {
        return nome;
    }

    public void setNome(@NonNull String nome) {
        this.nome = nome;
        this.nomeNormalizado = nome.toLowerCase(Locale.getDefault());
    }

    @NonNull
    public String getNomeNormalizado() {
        return nomeNormalizado;
    }

    public void setNomeNormalizado(@NonNull String nomeNormalizado) {
        this.nomeNormalizado = nomeNormalizado;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}
