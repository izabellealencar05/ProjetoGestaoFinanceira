package com.example.trabalhogestao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface DespesaDao {

    @Insert
    void inserir(Despesa despesa);

    @Delete
    void deletar(Despesa despesa);

    @Query("DELETE FROM despesas")
    void deletarTodas();

    // --- MÉTODOS NOVOS E ATUALIZADOS ABAIXO ---

    /**
     * Este método busca todas as despesas e, para cada uma, busca o nome da categoria
     * correspondente na tabela de categorias.
     * @return Uma lista de objetos DespesaComCategoria.
     */
    @Transaction
    @Query("SELECT d.*, c.nome AS nomeCategoria FROM despesas d JOIN categorias c ON d.categoriaId = c.id ORDER BY d.data DESC")
    List<DespesaComCategoria> listarTodasComCategoria();

    /**
     * Versão do método de busca por período que também inclui o nome da categoria.
     */
    @Transaction
    @Query("SELECT d.*, c.nome AS nomeCategoria FROM despesas d JOIN categorias c ON d.categoriaId = c.id WHERE d.data BETWEEN :dataInicio AND :dataFim ORDER BY d.data DESC")
    List<DespesaComCategoria> listarPorPeriodoComCategoria(String dataInicio, String dataFim);
}