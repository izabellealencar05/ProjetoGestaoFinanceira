package com.example.trabalhogestao;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder> {

    // --- VARIÁVEIS DA CLASSE ---
    private List<DespesaComCategoria> listaDespesas;
    private final OnItemClickListener listener;

    // Formatadores de data para conversão
    private final SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat formatoExibicao = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // --- INTERFACE PARA OS CLIQUES ---
    // Comunica os eventos de clique para a Home
    public interface OnItemClickListener {
        void onEditClick(Despesa despesa);
        void onDeleteClick(Despesa despesa);
    }

    // --- CONSTRUTOR ---
    // Recebe tanto a lista de despesas quanto o listener (a Home)
    public DespesaAdapter(List<DespesaComCategoria> listaDespesas, OnItemClickListener listener) {
        this.listaDespesas = listaDespesas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DespesaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_despesa, parent, false);
        return new DespesaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DespesaViewHolder holder, int position) {
        // Pega o objeto da posição atual
        DespesaComCategoria despesaComCategoria = listaDespesas.get(position);
        final Despesa despesa = despesaComCategoria.despesa;

        // 1. Exibe os dados de texto
        holder.tvDescricao.setText(despesa.getDescricao());
        holder.tvValor.setText("R$ " + despesa.getValor());
        holder.tvCategoria.setText(despesaComCategoria.nomeCategoria);

        // 2. Formata e exibe a data
        String dataDoBanco = despesa.getData();
        try {
            Date data = formatoBanco.parse(dataDoBanco);
            String dataParaExibir = formatoExibicao.format(data);
            holder.tvData.setText(dataParaExibir);
        } catch (ParseException e) {
            Log.e("DespesaAdapter", "Erro ao formatar data: " + dataDoBanco, e);
            holder.tvData.setText(dataDoBanco); // Em caso de erro, mostra a data original
        }

        // 3. Configura os cliques dos botões
        holder.btnEditar.setOnClickListener(v -> listener.onEditClick(despesa));
        holder.btnExcluir.setOnClickListener(v -> listener.onDeleteClick(despesa));
    }

    @Override
    public int getItemCount() {
        return listaDespesas.size();
    }

    // Método para atualizar a lista
    public void setDespesas(List<DespesaComCategoria> novasDespesas) {
        this.listaDespesas = novasDespesas;
        notifyDataSetChanged();
    }


    // --- VIEWHOLDER ---
    // Contém todos os componentes visuais do item da lista
    public static class DespesaViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescricao, tvValor, tvData, tvCategoria;
        ImageButton btnEditar, btnExcluir;

        public DespesaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvValor = itemView.findViewById(R.id.tvValor);
            tvData = itemView.findViewById(R.id.tvData);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}