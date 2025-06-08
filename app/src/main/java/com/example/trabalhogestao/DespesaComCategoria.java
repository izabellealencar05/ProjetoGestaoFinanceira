package com.example.trabalhogestao;

import androidx.room.Embedded;
public class DespesaComCategoria {

    @Embedded
    public Despesa despesa;

    public String nomeCategoria;
}