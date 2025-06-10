package com.example.trabalhogestao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CategoriaDao {

    @Insert
    long inserir(Categoria categoria);

    @Query("SELECT * FROM categorias ORDER BY nome ASC")
    List<Categoria> listarTodas();
}