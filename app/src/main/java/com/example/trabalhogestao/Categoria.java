package com.example.trabalhogestao;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias",
        indices = {@Index(value = "nome", unique = true)})
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String nome;

    public Categoria(@NonNull String nome) {
        this.nome = nome;
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
    }

    @Override
    public String toString() {
        return this.nome;
    }
}