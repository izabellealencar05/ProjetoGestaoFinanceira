package com.example.trabalhogestao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder> {

    // A lista agora é do tipo DespesaComCategoria
    private List<DespesaComCategoria> listaDespesas;

    // O construtor também muda
    public DespesaAdapter(List<DespesaComCategoria> listaDespesas) {
        this.listaDespesas = listaDespesas;
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
        DespesaComCategoria despesaComCategoria = listaDespesas.get(position);

        // Para pegar os dados da despesa, acessamos o objeto 'despesa' dentro de 'despesaComCategoria'
        holder.tvDescricao.setText(despesaComCategoria.despesa.getDescricao());
        holder.tvValor.setText("R$ " + despesaComCategoria.despesa.getValor());
        holder.tvData.setText(despesaComCategoria.despesa.getData());

        // Para pegar o nome da categoria, acessamos o campo 'nomeCategoria' diretamente
        holder.tvCategoria.setText(despesaComCategoria.nomeCategoria);
    }

    @Override
    public int getItemCount() {
        return listaDespesas.size();
    }

    public void setDespesas(List<DespesaComCategoria> novasDespesas) {
        this.listaDespesas = novasDespesas;
        notifyDataSetChanged();
    }

    public static class DespesaViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescricao, tvValor, tvData, tvCategoria;

        public DespesaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvValor = itemView.findViewById(R.id.tvValor);
            tvData = itemView.findViewById(R.id.tvData);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
        }
    }
}