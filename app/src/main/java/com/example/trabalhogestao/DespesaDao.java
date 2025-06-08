package com.example.trabalhogestao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DespesaDao {

    @Insert
    void inserir(Despesa despesa);

    @Delete
    void deletar(Despesa despesa);

    @Query("DELETE FROM despesas")
    void deletarTodas();

    @Update
    void atualizar(Despesa despesa);
    @Query("SELECT * FROM despesas WHERE id = :despesaId LIMIT 1")
    Despesa getDespesaById(int despesaId);
    @Transaction
    @Query("SELECT d.*, c.nome AS nomeCategoria FROM despesas d JOIN categorias c ON d.categoriaId = c.id ORDER BY d.data DESC")
    List<DespesaComCategoria> listarTodasComCategoria();

    @Transaction
    @Query("SELECT d.*, c.nome AS nomeCategoria FROM despesas d JOIN categorias c ON d.categoriaId = c.id WHERE d.data BETWEEN :dataInicio AND :dataFim ORDER BY d.data DESC")
    List<DespesaComCategoria> listarPorPeriodoComCategoria(String dataInicio, String dataFim);
}