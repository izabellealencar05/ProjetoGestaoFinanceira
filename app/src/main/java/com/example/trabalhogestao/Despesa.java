package com.example.trabalhogestao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "despesas",
        foreignKeys = @ForeignKey(entity = Categoria.class,
                parentColumns = "id",
                childColumns = "categoriaId",
                onDelete = ForeignKey.CASCADE))public class Despesa {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String descricao;

    private double valor;

    @NonNull
    private String data;

    @NonNull
    // Campo antigo 'categoria' foi substituído por 'categoriaId'
    @ColumnInfo(index = true) // O index melhora a performance de buscas por este campo
    private int categoriaId;

    public Despesa(@NonNull String descricao, double valor, @NonNull String data, int categoriaId) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoriaId = categoriaId;
    }



    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    @NonNull
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    @NonNull
    public String getData() { return data; }
    public int getCategoriaId() { return categoriaId; }
}
