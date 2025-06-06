package com.example.trabalhogestao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder> {

    private List<Despesa> listaDespesas;

    public DespesaAdapter(List<Despesa> listaDespesas) {
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
        Despesa despesa = listaDespesas.get(position);
        holder.tvDescricao.setText(despesa.getDescricao());
        holder.tvValor.setText("R$ " + despesa.getValor());
        holder.tvData.setText(despesa.getData());
        holder.tvCategoria.setText(despesa.getCategoria());
    }

    @Override
    public int getItemCount() {
        return listaDespesas.size();
    }
    public void setDespesas(List<Despesa> novasDespesas) {
        this.listaDespesas = novasDespesas;
        notifyDataSetChanged();  // Atualiza o RecyclerView
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
