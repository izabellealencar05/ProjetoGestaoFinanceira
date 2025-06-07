package com.example.trabalhogestao;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Relatorio extends AppCompatActivity {

    private Button btnRelatorioSemanal, btnRelatorioMensal, btnRelatorioAnual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        // Inicializa botões
        btnRelatorioSemanal = findViewById(R.id.btnRelatorioSemanal);
        btnRelatorioMensal = findViewById(R.id.btnRelatorioMensal);
        btnRelatorioAnual = findViewById(R.id.btnRelatorioAnual);

        // Configura ações ao clicar
        btnRelatorioSemanal.setOnClickListener(v -> gerarRelatorioSemanal());
        btnRelatorioMensal.setOnClickListener(v -> gerarRelatorioMensal());
        btnRelatorioAnual.setOnClickListener(v -> gerarRelatorioAnual());
    }

    private void gerarRelatorioSemanal() {
        Toast.makeText(this, "Gerando relatório semanal...", Toast.LENGTH_SHORT).show();
        // Aqui você pode implementar a lógica de gerar relatório semanal
    }

    private void gerarRelatorioMensal() {
        Toast.makeText(this, "Gerando relatório mensal...", Toast.LENGTH_SHORT).show();
        // Aqui você pode implementar a lógica de gerar relatório mensal
    }

    private void gerarRelatorioAnual() {
        Toast.makeText(this, "Gerando relatório anual...", Toast.LENGTH_SHORT).show();
        // Aqui você pode implementar a lógica de gerar relatório anual
    }
}
