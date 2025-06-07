package com.example.trabalhogestao;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private final SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat formatoExibicao = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


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

        holder.tvDescricao.setText(despesaComCategoria.despesa.getDescricao());
        holder.tvValor.setText("R$ " + despesaComCategoria.despesa.getValor());
        holder.tvCategoria.setText(despesaComCategoria.nomeCategoria);

        String dataDoBanco = despesaComCategoria.despesa.getData();
        try {
            Date data = formatoBanco.parse(dataDoBanco);
            String dataParaExibir = formatoExibicao.format(data);
            holder.tvData.setText(dataParaExibir);
        } catch (ParseException e) {
            Log.e("DespesaAdapter", "Erro ao formatar data: " + dataDoBanco, e);
            holder.tvData.setText(dataDoBanco);
        }
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