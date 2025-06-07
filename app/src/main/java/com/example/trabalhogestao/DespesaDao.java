package com.example.trabalhogestao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface DespesaDao {

    @Insert
    void inserir (Despesa despesa);
    @Query("SELECT * FROM despesas WHERE data BETWEEN :dataInicio AND :dataFim ORDER BY data DESC")
    List<Despesa> listarPorPeriodo(String dataInicio, String dataFim);

    @Query("SELECT * FROM despesas ORDER BY data DESC")
    List<Despesa> listarTodas();

    @Query("SELECT * FROM despesas WHERE data = :data")
    List<Despesa> listarPorData(String data);

    @Query("SELECT * FROM despesas WHERE categoria = :categoria")
    List<Despesa> listarPorCategoria(String categoria);

    @Delete
    void deletar(Despesa despesa);

    @Query("DELETE FROM despesas")
    void deletarTodas();

}
