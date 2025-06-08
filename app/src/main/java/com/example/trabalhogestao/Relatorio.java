package com.example.trabalhogestao;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Relatorio extends AppCompatActivity {

    // --- Variáveis de Controle de Estado ---
    private Calendar calendario; // Controla a data do período atual
    private String tipoRelatorio = "mensal"; // "semanal", "mensal", "anual"

    // --- Componentes da UI ---
    private ImageButton btnPeriodoAnterior, btnProximoPeriodo;
    private TextView tvPeriodoAtual, tvTotalGasto, tvMediaDiaria;
    private LinearLayout llCategorias;
    private MaterialButtonToggleGroup toggleModoRelatorio;

    private AppDatabase db;
    private DespesaDao despesaDao;
    private static final String TAG = "RelatorioActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        // Inicializa o banco de dados
        db = AppDatabase.getInstancia(getApplicationContext());
        despesaDao = db.despesaDao();

        // Inicializa o calendário com a data atual
        calendario = Calendar.getInstance();

        // Vincula os componentes da UI
        vincularViews();

        // Configura os listeners dos botões
        configurarListeners();

        // Inicia com o modo mensal selecionado
        toggleModoRelatorio.check(R.id.btnModoMensal);

        // Gera o primeiro relatório (mês atual)
        atualizarRelatorio();
    }

    private void vincularViews() {
        btnPeriodoAnterior = findViewById(R.id.btnPeriodoAnterior);
        btnProximoPeriodo = findViewById(R.id.btnProximoPeriodo);
        tvPeriodoAtual = findViewById(R.id.tvPeriodoAtual);
        toggleModoRelatorio = findViewById(R.id.toggleModoRelatorio);
        tvTotalGasto = findViewById(R.id.tvTotalGasto);
        tvMediaDiaria = findViewById(R.id.tvMediaDiaria);
        llCategorias = findViewById(R.id.llCategorias);
        Button btnVoltarRelatorio = findViewById(R.id.btnVoltarRelatorio);
        btnVoltarRelatorio.setOnClickListener(v -> finish());
    }

    private void configurarListeners() {
        btnPeriodoAnterior.setOnClickListener(v -> navegarPeriodo(-1));
        btnProximoPeriodo.setOnClickListener(v -> navegarPeriodo(1));

        toggleModoRelatorio.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnModoSemanal) {
                    tipoRelatorio = "semanal";
                } else if (checkedId == R.id.btnModoMensal) {
                    tipoRelatorio = "mensal";
                } else if (checkedId == R.id.btnModoAnual) {
                    tipoRelatorio = "anual";
                }
                // Reseta o calendário para a data atual ao trocar de modo
                calendario = Calendar.getInstance();
                atualizarRelatorio();
            }
        });
    }

    private void navegarPeriodo(int direcao) {
        switch (tipoRelatorio) {
            case "semanal":
                calendario.add(Calendar.WEEK_OF_YEAR, direcao);
                break;
            case "mensal":
                calendario.add(Calendar.MONTH, direcao);
                break;
            case "anual":
                calendario.add(Calendar.YEAR, direcao);
                break;
        }
        atualizarRelatorio();
    }

    private void atualizarRelatorio() {
        Calendar inicio = (Calendar) calendario.clone();
        Calendar fim = (Calendar) calendario.clone();
        int diasNoPeriodo;
        String formatoTitulo;

        switch (tipoRelatorio) {
            case "semanal":
                inicio.set(Calendar.DAY_OF_WEEK, inicio.getFirstDayOfWeek());
                fim.set(Calendar.DAY_OF_WEEK, fim.getFirstDayOfWeek());
                fim.add(Calendar.DAY_OF_WEEK, 6);
                formatoTitulo = "'Semana de' dd/MM/yyyy";
                diasNoPeriodo = 7;
                break;
            case "anual":
                inicio.set(Calendar.DAY_OF_YEAR, 1);
                fim.set(Calendar.DAY_OF_YEAR, fim.getActualMaximum(Calendar.DAY_OF_YEAR));
                formatoTitulo = "yyyy";
                diasNoPeriodo = fim.getActualMaximum(Calendar.DAY_OF_YEAR);
                break;
            case "mensal":
            default:
                inicio.set(Calendar.DAY_OF_MONTH, 1);
                fim.set(Calendar.DAY_OF_MONTH, fim.getActualMaximum(Calendar.DAY_OF_MONTH));
                formatoTitulo = "MMMM 'de' yyyy";
                diasNoPeriodo = fim.getActualMaximum(Calendar.DAY_OF_MONTH);
                break;
        }

        // Atualiza o texto do período na tela
        SimpleDateFormat sdfTitulo = new SimpleDateFormat(formatoTitulo, new Locale("pt", "BR"));
        tvPeriodoAtual.setText(sdfTitulo.format(calendario.getTime()));

        Date dataInicioDate = inicio.getTime();
        Date dataFimDate = fim.getTime();

        String dataInicioFormatada = formatarDataParaDB(dataInicioDate);
        String dataFimFormatada = formatarDataParaDB(dataFimDate);

        new Thread(() -> {
            List<DespesaComCategoria> despesas = despesaDao.listarPorPeriodoComCategoria(dataInicioFormatada, dataFimFormatada);
            double totalCalculado = despesas.stream().mapToDouble(d -> d.despesa.getValor()).sum();
            double mediaDiaria = (diasNoPeriodo > 0) ? totalCalculado / diasNoPeriodo : 0;

            Map<String, Double> gastosPorCategoria = new HashMap<>();
            for(DespesaComCategoria d : despesas) {
                gastosPorCategoria.put(d.nomeCategoria, gastosPorCategoria.getOrDefault(d.nomeCategoria, 0.0) + d.despesa.getValor());
            }

            runOnUiThread(() -> preencherDadosRelatorio(totalCalculado, mediaDiaria, gastosPorCategoria));
        }).start();
    }

    private void preencherDadosRelatorio(double total, double media, Map<String, Double> gastosPorCategoria) {
        DecimalFormat df = new DecimalFormat("0.00");
        tvTotalGasto.setText("R$ " + df.format(total));
        tvMediaDiaria.setText("R$ " + df.format(media));

        llCategorias.removeAllViews();
        if (gastosPorCategoria.isEmpty()) {
            TextView tvSemGastos = new TextView(this);
            tvSemGastos.setText("Nenhum gasto registrado neste período.");
            llCategorias.addView(tvSemGastos);
        } else {
            gastosPorCategoria.forEach((categoria, valor) -> adicionarLinhaCategoria(categoria, valor, df));
        }
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
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(data);
    }
}
