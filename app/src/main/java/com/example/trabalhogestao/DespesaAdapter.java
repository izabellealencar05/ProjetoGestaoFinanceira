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

    private List<DespesaComCategoria> listaDespesas;
    private final OnItemClickListener listener;

    private final SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat formatoExibicao = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onEditClick(Despesa despesa);
        void onDeleteClick(Despesa despesa);
    }


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
        DespesaComCategoria despesaComCategoria = listaDespesas.get(position);
        final Despesa despesa = despesaComCategoria.despesa;

        holder.tvDescricao.setText(despesa.getDescricao());
        holder.tvValor.setText("R$ " + despesa.getValor());
        holder.tvCategoria.setText(despesaComCategoria.nomeCategoria);

        String dataDoBanco = despesa.getData();
        try {
            Date data = formatoBanco.parse(dataDoBanco);
            String dataParaExibir = formatoExibicao.format(data);
            holder.tvData.setText(dataParaExibir);
        } catch (ParseException e) {
            Log.e("DespesaAdapter", "Erro ao formatar data: " + dataDoBanco, e);
            holder.tvData.setText(dataDoBanco); // Em caso de erro, mostra a data original
        }

        holder.btnEditar.setOnClickListener(v -> listener.onEditClick(despesa));
        holder.btnExcluir.setOnClickListener(v -> listener.onDeleteClick(despesa));
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