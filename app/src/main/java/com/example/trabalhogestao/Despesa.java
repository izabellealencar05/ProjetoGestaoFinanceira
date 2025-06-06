package com.example.trabalhogestao;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "despesas")
public class Despesa {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String descricao;

    private double valor;

    @NonNull
    private String data;

    @NonNull
    private String categoria;

    // Construtor
    public Despesa(@NonNull String descricao, double valor, @NonNull String data, @NonNull String categoria) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getValor() {
        return valor;
    }

    public String getData() {
        return data;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setId(int id) {
        this.id = id;
    }
}
