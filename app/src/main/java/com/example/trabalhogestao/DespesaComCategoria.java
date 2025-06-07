package com.example.trabalhogestao;

import androidx.room.Embedded;

// Esta é uma classe "POJO" (Plain Old Java Object).
// Ela não é uma tabela no banco, apenas uma estrutura para receber os resultados da nossa consulta.
public class DespesaComCategoria {

    @Embedded // O Room vai preencher os campos da despesa aqui
    public Despesa despesa;

    // O Room vai preencher o nome da categoria aqui
    public String nomeCategoria;
}