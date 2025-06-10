package com.example.trabalhogestao;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Relatorio extends AppCompatActivity {

    // --- Variáveis de Controle de Estado ---
    private Calendar calendarioBase;
    private String modoRelatorio = "mensal";
    private Calendar dataInicioPersonalizada, dataFimPersonalizada;

    // --- Componentes da UI ---
    private TextView tvPeriodoAtual, tvTotalGasto, tvMediaDiaria;
    private LinearLayout llCategorias, layoutNavegacao, layoutDatasPersonalizadas;
    private ChipGroup chipGroupSemanas;
    private Button btnDataInicio, btnDataFim;
    private MaterialButtonToggleGroup toggleModoRelatorio;
    private ImageButton btnPeriodoAnterior, btnProximoPeriodo;

    private AppDatabase db;
    private final SimpleDateFormat formatoDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat formatoUI = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        db = AppDatabase.getInstancia(getApplicationContext());
        calendarioBase = Calendar.getInstance();

        vincularViews();
        configurarListeners();

        toggleModoRelatorio.check(R.id.btnModoMensal);
        atualizarVisibilidadeControles();
        atualizarRelatorio();
    }

    private void vincularViews() {
        tvPeriodoAtual = findViewById(R.id.tvPeriodoAtual);
        tvTotalGasto = findViewById(R.id.tvTotalGasto);
        tvMediaDiaria = findViewById(R.id.tvMediaDiaria);
        llCategorias = findViewById(R.id.llCategorias);
        layoutNavegacao = findViewById(R.id.layoutNavegacao);
        layoutDatasPersonalizadas = findViewById(R.id.layoutDatasPersonalizadas);
        chipGroupSemanas = findViewById(R.id.chipGroupSemanas);
        btnDataInicio = findViewById(R.id.btnDataInicio);
        btnDataFim = findViewById(R.id.btnDataFim);
        toggleModoRelatorio = findViewById(R.id.toggleModoRelatorio);
        btnPeriodoAnterior = findViewById(R.id.btnPeriodoAnterior);
        btnProximoPeriodo = findViewById(R.id.btnProximoPeriodo);
        findViewById(R.id.btnVoltarRelatorio).setOnClickListener(v -> finish());
    }

    private void configurarListeners() {
        btnPeriodoAnterior.setOnClickListener(v -> navegarPeriodo(-1));
        btnProximoPeriodo.setOnClickListener(v -> navegarPeriodo(1));

        toggleModoRelatorio.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnModoMensal) modoRelatorio = "mensal";
            else if (checkedId == R.id.btnModoAnual) modoRelatorio = "anual";
            else if (checkedId == R.id.btnModoPersonalizado) modoRelatorio = "personalizado";

            calendarioBase = Calendar.getInstance();
            dataInicioPersonalizada = null;
            dataFimPersonalizada = null;
            btnDataInicio.setText("Início");
            btnDataFim.setText("Fim");

            atualizarVisibilidadeControles();
            atualizarRelatorio();
        });

        btnDataInicio.setOnClickListener(v -> abrirDatePicker(true));
        btnDataFim.setOnClickListener(v -> abrirDatePicker(false));
    }

    private void atualizarVisibilidadeControles() {
        layoutNavegacao.setVisibility("personalizado".equals(modoRelatorio) ? View.GONE : View.VISIBLE);
        layoutDatasPersonalizadas.setVisibility("personalizado".equals(modoRelatorio) ? View.VISIBLE : View.GONE);
        chipGroupSemanas.setVisibility("mensal".equals(modoRelatorio) ? View.VISIBLE : View.GONE);
    }

    private void navegarPeriodo(int direcao) {
        if ("mensal".equals(modoRelatorio)) calendarioBase.add(Calendar.MONTH, direcao);
        else if ("anual".equals(modoRelatorio)) calendarioBase.add(Calendar.YEAR, direcao);
        atualizarRelatorio();
    }

    private void abrirDatePicker(boolean isDataInicio) {
        Calendar calendarioParaPicker = Calendar.getInstance();
        if (isDataInicio && dataInicioPersonalizada != null) {
            calendarioParaPicker = dataInicioPersonalizada;
        } else if (!isDataInicio && dataFimPersonalizada != null) {
            calendarioParaPicker = dataFimPersonalizada;
        }

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar dataSelecionada = Calendar.getInstance();
            dataSelecionada.set(year, month, dayOfMonth);
            if (isDataInicio) {
                dataInicioPersonalizada = dataSelecionada;
                btnDataInicio.setText(formatoUI.format(dataSelecionada.getTime()));
            } else {
                dataFimPersonalizada = dataSelecionada;
                btnDataFim.setText(formatoUI.format(dataSelecionada.getTime()));
            }
            if (dataInicioPersonalizada != null && dataFimPersonalizada != null) {
                atualizarRelatorio();
            }
        }, calendarioParaPicker.get(Calendar.YEAR), calendarioParaPicker.get(Calendar.MONTH), calendarioParaPicker.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void atualizarRelatorio() {
        if ("personalizado".equals(modoRelatorio)) {
            if (dataInicioPersonalizada == null || dataFimPersonalizada == null) {
                preencherDadosRelatorio(0, 0, new HashMap<>());
                tvPeriodoAtual.setText("Selecione um período");
                return;
            }
            tvPeriodoAtual.setText(formatoUI.format(dataInicioPersonalizada.getTime()) + " - " + formatoUI.format(dataFimPersonalizada.getTime()));
            gerarRelatorioParaPeriodo(dataInicioPersonalizada.getTime(), dataFimPersonalizada.getTime());
        } else {
            Calendar inicio = (Calendar) calendarioBase.clone();
            Calendar fim = (Calendar) calendarioBase.clone();
            if ("mensal".equals(modoRelatorio)) {
                inicio.set(Calendar.DAY_OF_MONTH, 1);
                fim.set(Calendar.DAY_OF_MONTH, fim.getActualMaximum(Calendar.DAY_OF_MONTH));
                tvPeriodoAtual.setText(new SimpleDateFormat("MMMM 'de' yyyy", new Locale("pt","BR")).format(calendarioBase.getTime()));
                gerarChipsDeSemana(calendarioBase);
                gerarRelatorioParaPeriodo(inicio.getTime(), fim.getTime());
            } else { // Anual
                inicio.set(Calendar.DAY_OF_YEAR, 1);
                fim.set(Calendar.DAY_OF_YEAR, fim.getActualMaximum(Calendar.DAY_OF_YEAR));
                tvPeriodoAtual.setText(new SimpleDateFormat("yyyy", Locale.getDefault()).format(calendarioBase.getTime()));
                gerarRelatorioParaPeriodo(inicio.getTime(), fim.getTime());
            }
        }
    }

    private void gerarChipsDeSemana(Calendar mes) {
        chipGroupSemanas.clearCheck();
        chipGroupSemanas.removeAllViews();
        int semanasNoMes = mes.getActualMaximum(Calendar.WEEK_OF_MONTH);

        for (int i = 1; i <= semanasNoMes; i++) {
            Chip chip = new Chip(this);
            chip.setText("Semana " + i);
            chip.setCheckable(true);
            final int semanaAtual = i;
            chip.setOnClickListener(v -> {
                Calendar inicioSemana = (Calendar) mes.clone();
                inicioSemana.set(Calendar.WEEK_OF_MONTH, semanaAtual);
                inicioSemana.set(Calendar.DAY_OF_WEEK, inicioSemana.getFirstDayOfWeek());

                Calendar fimSemana = (Calendar) inicioSemana.clone();
                fimSemana.add(Calendar.DAY_OF_WEEK, 6);

                gerarRelatorioParaPeriodo(inicioSemana.getTime(), fimSemana.getTime());
            });
            chipGroupSemanas.addView(chip);
        }
    }

    private void gerarRelatorioParaPeriodo(Date dataInicio, Date dataFim) {
        long diffEmMillis = Math.abs(dataFim.getTime() - dataInicio.getTime());
        long diffEmDias = TimeUnit.DAYS.convert(diffEmMillis, TimeUnit.MILLISECONDS) + 1;

        String dataInicioDB = formatoDB.format(dataInicio);
        String dataFimDB = formatoDB.format(dataFim);

        new Thread(() -> {
            List<DespesaComCategoria> despesas = db.despesaDao().listarPorPeriodoComCategoria(dataInicioDB, dataFimDB);
            double total = despesas.stream().mapToDouble(d -> d.despesa.getValor()).sum();
            double media = (diffEmDias > 0) ? total / diffEmDias : 0;

            Map<String, Double> gastosPorCategoria = new HashMap<>();
            for (DespesaComCategoria d : despesas) {
                gastosPorCategoria.put(d.nomeCategoria, gastosPorCategoria.getOrDefault(d.nomeCategoria, 0.0) + d.despesa.getValor());
            }

            runOnUiThread(() -> preencherDadosRelatorio(total, media, gastosPorCategoria));
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
            gastosPorCategoria.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(entry -> adicionarLinhaCategoria(entry.getKey(), entry.getValue(), df));
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
}
