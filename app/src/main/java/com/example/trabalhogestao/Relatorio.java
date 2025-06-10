package com.example.trabalhogestao;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import android.graphics.Color;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Relatorio extends AppCompatActivity {

    private PieChart pieChart;
    private TextView tvPeriodoAtual, tvTotalGasto, tvMediaDiaria, tvLabelMediaDiaria;
    private LinearLayout llCategorias, layoutNavegacao, layoutDatasPersonalizadas;
    private Button btnDataInicio, btnDataFim;
    private MaterialToolbar toolbarRelatorio; // Adicionada variável para a Toolbar
    private MaterialButtonToggleGroup toggleModoRelatorio;
    private ImageButton btnPeriodoAnterior, btnProximoPeriodo;

    private Calendar calendarioBase;
    private String modoRelatorio = "mensal";
    private Calendar dataInicioPersonalizada, dataFimPersonalizada;
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
        atualizarRelatorio();
    }

    private void vincularViews() {
        toolbarRelatorio = findViewById(R.id.toolbarRelatorio); // Vincula a nova toolbar
        pieChart = findViewById(R.id.pieChart);
        tvPeriodoAtual = findViewById(R.id.tvPeriodoAtual);
        tvTotalGasto = findViewById(R.id.tvTotalGasto);
        tvMediaDiaria = findViewById(R.id.tvMediaDiaria);
        llCategorias = findViewById(R.id.llCategorias);
        layoutNavegacao = findViewById(R.id.layoutNavegacao);
        layoutDatasPersonalizadas = findViewById(R.id.layoutDatasPersonalizadas);
        btnDataInicio = findViewById(R.id.btnDataInicio);
        btnDataFim = findViewById(R.id.btnDataFim);
        toggleModoRelatorio = findViewById(R.id.toggleModoRelatorio);
        btnPeriodoAnterior = findViewById(R.id.btnPeriodoAnterior);
        btnProximoPeriodo = findViewById(R.id.btnProximoPeriodo);
        tvLabelMediaDiaria = findViewById(R.id.tvLabelMediaDiaria);
        // A linha do botão de voltar foi removida daqui
    }

    private void configurarListeners() {
        // Configura o clique no ícone de navegação da toolbar para fechar a tela
        toolbarRelatorio.setNavigationOnClickListener(v -> finish());

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

        tvPeriodoAtual.setOnClickListener(v -> abrirSeletorDePeriodo());
        btnDataInicio.setOnClickListener(v -> abrirDatePicker(true));
        btnDataFim.setOnClickListener(v -> abrirDatePicker(false));
    }

    private void abrirSeletorDePeriodo() {
        if ("mensal".equals(modoRelatorio)) {
            abrirSeletorDeMesAno();
        } else if ("anual".equals(modoRelatorio)) {
            abrirSeletorDeAno();
        }
    }

    private void abrirSeletorDeMesAno() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);

        NumberPicker monthPicker = new NumberPicker(this);
        final String[] meses = new DateFormatSymbols(new Locale("pt", "BR")).getMonths();
        String[] displayMeses = Arrays.copyOf(meses, 12);
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(displayMeses);
        monthPicker.setValue(calendarioBase.get(Calendar.MONTH));

        NumberPicker yearPicker = new NumberPicker(this);
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(anoAtual - 50);
        yearPicker.setMaxValue(anoAtual + 50);
        yearPicker.setValue(calendarioBase.get(Calendar.YEAR));

        container.addView(monthPicker);
        container.addView(yearPicker);

        new AlertDialog.Builder(this)
                .setTitle("Selecione o Mês e o Ano")
                .setView(container)
                .setPositiveButton("OK", (dialog, which) -> {
                    calendarioBase.set(Calendar.MONTH, monthPicker.getValue());
                    calendarioBase.set(Calendar.YEAR, yearPicker.getValue());
                    atualizarRelatorio();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirSeletorDeAno() {
        NumberPicker yearPicker = new NumberPicker(this);
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(anoAtual - 50);
        yearPicker.setMaxValue(anoAtual + 50);
        yearPicker.setValue(calendarioBase.get(Calendar.YEAR));

        new AlertDialog.Builder(this)
                .setTitle("Selecione o Ano")
                .setView(yearPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    calendarioBase.set(Calendar.YEAR, yearPicker.getValue());
                    atualizarRelatorio();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarVisibilidadeControles() {
        layoutNavegacao.setVisibility("personalizado".equals(modoRelatorio) ? View.GONE : View.VISIBLE);
        layoutDatasPersonalizadas.setVisibility("personalizado".equals(modoRelatorio) ? View.VISIBLE : View.GONE);

        View anchorView = "personalizado".equals(modoRelatorio) ? layoutDatasPersonalizadas : layoutNavegacao;
        ((androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) findViewById(R.id.scrollViewResultados).getLayoutParams())
                .topToBottom = anchorView.getId();
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
                preencherDadosRelatorio(0, 0, new HashMap<>(), 0);
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
                gerarRelatorioParaPeriodo(inicio.getTime(), fim.getTime());
            } else {
                inicio.set(Calendar.DAY_OF_YEAR, 1);
                fim.set(Calendar.DAY_OF_YEAR, fim.getActualMaximum(Calendar.DAY_OF_YEAR));
                tvPeriodoAtual.setText(new SimpleDateFormat("yyyy", Locale.getDefault()).format(calendarioBase.getTime()));
                gerarRelatorioParaPeriodo(inicio.getTime(), fim.getTime());
            }
        }
    }

    private void gerarRelatorioParaPeriodo(Date dataInicio, Date dataFim) {
        if (dataInicio.after(dataFim)) {
            preencherDadosRelatorio(0,0,new HashMap<>(), 0);
            Toast.makeText(this, "A data de início não pode ser posterior à data de fim.", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataInicioDB = formatoDB.format(dataInicio);
        String dataFimDB = formatoDB.format(dataFim);

        new Thread(() -> {
            List<DespesaComCategoria> despesas = db.despesaDao().listarPorPeriodoComCategoria(dataInicioDB, dataFimDB);
            double total = despesas.stream().mapToDouble(d -> d.despesa.getValor()).sum();

            Set<String> diasComGasto = new HashSet<>();
            for (DespesaComCategoria d : despesas) {
                diasComGasto.add(d.despesa.getData());
            }
            int numeroDeDiasComGasto = diasComGasto.size();
            double media = (numeroDeDiasComGasto > 0) ? total / numeroDeDiasComGasto : 0;

            Map<String, Double> gastosPorCategoria = new HashMap<>();
            for (DespesaComCategoria d : despesas) {
                gastosPorCategoria.put(d.nomeCategoria, gastosPorCategoria.getOrDefault(d.nomeCategoria, 0.0) + d.despesa.getValor());
            }

            runOnUiThread(() -> preencherDadosRelatorio(total, media, gastosPorCategoria, numeroDeDiasComGasto));
        }).start();
    }

    private void preencherDadosRelatorio(double total, double media, Map<String, Double> gastosPorCategoria, int diasComGasto) {
        DecimalFormat df = new DecimalFormat("0.00");
        tvTotalGasto.setText("R$ " + df.format(total));

        if (diasComGasto > 0) {
            tvLabelMediaDiaria.setText("Média por Dia de Gasto (" + diasComGasto + " dias):");
        } else {
            tvLabelMediaDiaria.setText("Média por Dia de Gasto:");
        }
        tvMediaDiaria.setText("R$ " + df.format(media));

        configurarGraficoPizza(gastosPorCategoria);

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

    private void configurarGraficoPizza(Map<String, Double> gastosPorCategoria) {
        if (gastosPorCategoria.isEmpty() || gastosPorCategoria.values().stream().allMatch(v -> v == 0)) {
            pieChart.setVisibility(View.GONE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);

        List<PieEntry> entradas = new ArrayList<>();
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            entradas.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entradas, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getLegend().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
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
