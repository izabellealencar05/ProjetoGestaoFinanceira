package com.example.trabalhogestao;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Relatorio extends AppCompatActivity {

    private Button btnRelatorioSemanal, btnRelatorioMensal, btnRelatorioAnual;
    private AppDatabase db;
    private DespesaDao despesaDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        btnRelatorioSemanal = findViewById(R.id.btnRelatorioSemanal);
        btnRelatorioMensal = findViewById(R.id.btnRelatorioMensal);
        btnRelatorioAnual = findViewById(R.id.btnRelatorioAnual);

        db = AppDatabase.getInstancia(getApplicationContext());
        despesaDao = db.despesaDao();

        btnRelatorioSemanal.setOnClickListener(v -> gerarRelatorio("semana"));
        btnRelatorioMensal.setOnClickListener(v -> gerarRelatorio("mes"));
        btnRelatorioAnual.setOnClickListener(v -> gerarRelatorio("ano"));
    }

    private void gerarRelatorio(String tipoPeriodo) {
        // Calcular dataInicio e dataFim no formato yyyy-MM-dd
        String dataInicio, dataFim;

        Calendar hoje = Calendar.getInstance();
        Calendar inicio = (Calendar) hoje.clone();

        switch (tipoPeriodo) {
            case "semana":
                inicio.set(Calendar.DAY_OF_WEEK, inicio.getFirstDayOfWeek());
                break;
            case "mes":
                inicio.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "ano":
                inicio.set(Calendar.DAY_OF_YEAR, 1);
                break;
        }

        dataInicio = formatarData(inicio.getTime());
        dataFim = formatarData(hoje.getTime());

        new Thread(() -> {
            List<Despesa> despesas = despesaDao.listarPorPeriodo(dataInicio, dataFim);

            final double[] somaTotal = {0};
            final String[] categoriaMax = {null};
            Map<String, Double> gastosPorCategoria = new HashMap<>();

            for (Despesa d : despesas) {
                somaTotal[0] += d.getValor();
                String cat = d.getCategoria();
                gastosPorCategoria.put(cat, gastosPorCategoria.getOrDefault(cat, 0.0) + d.getValor());
            }

            double maxGasto = 0;
            for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
                if (entry.getValue() > maxGasto) {
                    maxGasto = entry.getValue();
                    categoriaMax[0] = entry.getKey();
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Total: R$ " + somaTotal[0] + "\nMaior gasto: " + categoriaMax[0], Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    private String formatarData(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(data);
    }
}
