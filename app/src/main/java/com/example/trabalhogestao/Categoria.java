package com.example.trabalhogestao;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias",
        indices = {@Index(value = "nome", unique = true)}) // Garante que não haja categorias com nomes repetidos
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String nome;

    public Categoria(@NonNull String nome) {
        this.nome = nome;
    }

    // Getters e Setters
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

    // Isso é importante para o Spinner exibir o nome da categoria corretamente
    @Override
    public String toString() {
        return this.nome;
    }
}