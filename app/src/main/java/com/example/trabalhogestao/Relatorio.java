package com.example.trabalhogestao;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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

    private TextView tvPeriodoRelatorio;
    private TextView tvTotalGasto;
    private TextView tvMediaDiaria;
    private LinearLayout llCategorias;
    private Button btnVoltarRelatorio;

    private static final String TAG = "RelatorioActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        tvPeriodoRelatorio = findViewById(R.id.tvPeriodoRelatorio);
        tvTotalGasto = findViewById(R.id.tvTotalGasto);
        tvMediaDiaria = findViewById(R.id.tvMediaDiaria);
        llCategorias = findViewById(R.id.llCategorias);
        btnVoltarRelatorio = findViewById(R.id.btnVoltarRelatorio);

        db = AppDatabase.getInstancia(getApplicationContext());
        despesaDao = db.despesaDao();

        gerarRelatorioSemanal();

        btnVoltarRelatorio.setOnClickListener(v -> finish());
    }

    private void gerarRelatorioSemanal() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        Date dataInicioDate = calendar.getTime();
        Date dataFimDate = new Date();

        String dataInicioFormatada = formatarDataParaDB(dataInicioDate);
        String dataFimFormatada = formatarDataParaDB(dataFimDate);

        String periodoExibicao = formatarDataParaUI(dataInicioDate) + " - " + formatarDataParaUI(dataFimDate);
        tvPeriodoRelatorio.setText("Período: " + periodoExibicao);

        new Thread(() -> {
            List<DespesaComCategoria> despesasDaSemana = despesaDao.listarPorPeriodoComCategoria(dataInicioFormatada, dataFimFormatada);

            Log.d(TAG, "Período de busca: " + dataInicioFormatada + " a " + dataFimFormatada);
            Log.d(TAG, "Despesas encontradas na semana: " + despesasDaSemana.size());

            // 1. Usamos variáveis locais para fazer o cálculo
            double totalCalculado = 0;
            Map<String, Double> gastosPorCategoriaCalculado = new HashMap<>();

            for (DespesaComCategoria d : despesasDaSemana) {
                totalCalculado += d.despesa.getValor();
                String categoria = d.nomeCategoria; // Pega o nome da categoria do novo objeto
                double valorAtual = gastosPorCategoriaCalculado.getOrDefault(categoria, 0.0);
                gastosPorCategoriaCalculado.put(categoria, valorAtual + d.despesa.getValor());
            }

            double mediaDiariaCalculada = despesasDaSemana.isEmpty() ? 0 : totalCalculado / 7.0;

            // 2. <-- PONTO CRÍTICO DA CORREÇÃO
            // Criamos variáveis FINAIS com os resultados para passar para a UI thread.
            // É a única forma segura de passar os dados da thread de background para a UI.
            final double finalTotalGasto = totalCalculado;
            final double finalMediaDiaria = mediaDiariaCalculada;
            final Map<String, Double> finalGastosPorCategoria = gastosPorCategoriaCalculado;

            runOnUiThread(() -> {
                DecimalFormat df = new DecimalFormat("0.00");

                // 3. Usamos as variáveis FINAIS aqui. O erro acontece se você usar "totalCalculado".
                tvTotalGasto.setText("R$ " + df.format(finalTotalGasto));
                tvMediaDiaria.setText("R$ " + df.format(finalMediaDiaria));

                llCategorias.removeAllViews();

                if (finalGastosPorCategoria.isEmpty()) {
                    TextView tvSemGastos = new TextView(this);
                    tvSemGastos.setText("Nenhum gasto registrado nesta semana.");
                    tvSemGastos.setTextSize(16);
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        linhaLayout.setLayoutParams(params);

        TextView tvNomeCategoria = new TextView(this);
        tvNomeCategoria.setText(categoria);
        tvNomeCategoria.setTextSize(16);
        LinearLayout.LayoutParams paramsCategoria = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        tvNomeCategoria.setLayoutParams(paramsCategoria);

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