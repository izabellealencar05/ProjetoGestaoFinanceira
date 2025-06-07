package com.example.trabalhogestao;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Relatorio extends AppCompatActivity {

    private AppDatabase db;
    private DespesaDao despesaDao;

    // Componentes da UI
    private Button btnRelatorioSemanal, btnRelatorioMensal, btnRelatorioAnual, btnVoltarRelatorio;
    private TextView tvInstrucaoRelatorio, tvTituloRelatorio, tvPeriodoRelatorio, tvTotalGasto, tvMediaDiaria;
    private LinearLayout llCategorias, layoutConteudoRelatorio;

    private static final String TAG = "RelatorioActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        db = AppDatabase.getInstancia(getApplicationContext());
        despesaDao = db.despesaDao();

        // Vinculando todos os componentes da UI
        btnRelatorioSemanal = findViewById(R.id.btnRelatorioSemanal);
        btnRelatorioMensal = findViewById(R.id.btnRelatorioMensal);
        btnRelatorioAnual = findViewById(R.id.btnRelatorioAnual);
        btnVoltarRelatorio = findViewById(R.id.btnVoltarRelatorio);

        tvInstrucaoRelatorio = findViewById(R.id.tvInstrucaoRelatorio);
        layoutConteudoRelatorio = findViewById(R.id.layoutConteudoRelatorio);
        tvTituloRelatorio = findViewById(R.id.tvTituloRelatorio);
        tvPeriodoRelatorio = findViewById(R.id.tvPeriodoRelatorio);
        tvTotalGasto = findViewById(R.id.tvTotalGasto);
        tvMediaDiaria = findViewById(R.id.tvMediaDiaria);
        llCategorias = findViewById(R.id.llCategorias);

        // Configurando os cliques dos botões de período
        btnRelatorioSemanal.setOnClickListener(v -> gerarRelatorio("semanal"));
        btnRelatorioMensal.setOnClickListener(v -> gerarRelatorio("mensal"));
        btnRelatorioAnual.setOnClickListener(v -> gerarRelatorio("anual"));

        btnVoltarRelatorio.setOnClickListener(v -> finish());
    }

    private void gerarRelatorio(String tipoPeriodo) {
        Calendar inicio = Calendar.getInstance();
        Calendar fim = Calendar.getInstance();

        Date dataInicioDate;
        Date dataFimDate;
        int diasNoPeriodo;
        String titulo;

        switch (tipoPeriodo) {
            case "mensal":
                titulo = "Relatório Mensal";
                inicio.set(Calendar.DAY_OF_MONTH, 1);
                fim.set(Calendar.DAY_OF_MONTH, fim.getActualMaximum(Calendar.DAY_OF_MONTH));
                diasNoPeriodo = fim.getActualMaximum(Calendar.DAY_OF_MONTH);
                break;

            case "anual":
                titulo = "Relatório Anual";
                inicio.set(Calendar.DAY_OF_YEAR, 1);
                fim.set(Calendar.DAY_OF_YEAR, fim.getActualMaximum(Calendar.DAY_OF_YEAR));
                diasNoPeriodo = fim.getActualMaximum(Calendar.DAY_OF_YEAR);
                break;

            case "semanal":
            default:
                titulo = "Relatório Semanal";
                inicio.set(Calendar.DAY_OF_WEEK, inicio.getFirstDayOfWeek());
                diasNoPeriodo = 7;
                break;
        }

        dataInicioDate = inicio.getTime();
        dataFimDate = fim.getTime();

        String dataInicioFormatada = formatarDataParaDB(dataInicioDate);
        String dataFimFormatada = formatarDataParaDB(dataFimDate);

        // --- LINHA CORRIGIDA ABAIXO ---
        // Usamos 'dataFimDate' (o objeto Date) em vez de 'dataFimFormatada' (a String)
        String periodoExibicao = formatarDataParaUI(dataInicioDate) + " - " + formatarDataParaUI(dataFimDate);

        new Thread(() -> {
            List<DespesaComCategoria> despesasDoPeriodo = despesaDao.listarPorPeriodoComCategoria(dataInicioFormatada, dataFimFormatada);
            Log.d(TAG, "Buscando período " + tipoPeriodo + ". Encontradas " + despesasDoPeriodo.size() + " despesas.");

            double totalCalculado = 0;
            Map<String, Double> gastosPorCategoria = new HashMap<>();
            for (DespesaComCategoria d : despesasDoPeriodo) {
                totalCalculado += d.despesa.getValor();
                String categoria = d.nomeCategoria;
                gastosPorCategoria.put(categoria, gastosPorCategoria.getOrDefault(categoria, 0.0) + d.despesa.getValor());
            }

            double mediaDiariaCalculada = (diasNoPeriodo > 0) ? totalCalculado / diasNoPeriodo : 0;

            final double finalTotalGasto = totalCalculado;
            final double finalMediaDiaria = mediaDiariaCalculada;
            final Map<String, Double> finalGastosPorCategoria = gastosPorCategoria;

            runOnUiThread(() -> {
                tvInstrucaoRelatorio.setVisibility(View.GONE);
                layoutConteudoRelatorio.setVisibility(View.VISIBLE);

                DecimalFormat df = new DecimalFormat("0.00");

                tvTituloRelatorio.setText(titulo);
                tvPeriodoRelatorio.setText("Período: " + periodoExibicao);
                tvTotalGasto.setText("R$ " + df.format(finalTotalGasto));
                tvMediaDiaria.setText("R$ " + df.format(finalMediaDiaria));

                llCategorias.removeAllViews();
                if (finalGastosPorCategoria.isEmpty()) {
                    TextView tvSemGastos = new TextView(this);
                    tvSemGastos.setText("Nenhum gasto registrado neste período.");
                    llCategorias.addView(tvSemGastos);
                } else {
                    for (Map.Entry<String, Double> entry : finalGastosPorCategoria.entrySet()) {
                        adicionarLinhaCategoria(entry.getKey(), entry.getValue(), df);
                    }
                }
            });
        }).start();
    }

    private void adicionarLinhaCategoria(String categoria, double valor, DecimalFormat df) {
        LinearLayout linhaLayout = new LinearLayout(this);
        linhaLayout.setOrientation(LinearLayout.HORIZONTAL);
        linhaLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvNomeCategoria = new TextView(this);
        tvNomeCategoria.setText(categoria);
        tvNomeCategoria.setTextSize(16);
        tvNomeCategoria.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvValorCategoria = new TextView(this);
        tvValorCategoria.setText("R$ " + df.format(valor));
        tvValorCategoria.setTextSize(16);
        tvValorCategoria.setGravity(Gravity.END);

        linhaLayout.addView(tvNomeCategoria);
        linhaLayout.addView(tvValorCategoria);

        llCategorias.addView(linhaLayout);
    }

    private String formatarDataParaDB(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(data);
    }

    private String formatarDataParaUI(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(data);
    }
}