package com.example.trabalhogestao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CadastroDespesa extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_despesa);
        EditText etDescricao = findViewById(R.id.etDescricao);
        EditText etValor = findViewById(R.id.etValor);
        EditText etData = findViewById(R.id.etData);
        EditText etCategoria = findViewById(R.id.etCategoria);
        Button btnSalvar = findViewById(R.id.btnSalvar);
        Button btnVoltar = findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
            finish(); // opcional, para fechar a activity atual e não ficar no back stack
        });
        btnSalvar.setOnClickListener(v -> {
            String descricao = etDescricao.getText().toString().trim();
            String valorStr = etValor.getText().toString().trim();
            String data = etData.getText().toString().trim();
            String categoria = etCategoria.getText().toString().trim();

            if (descricao.isEmpty() || valorStr.isEmpty() || data.isEmpty() || categoria.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double valor;
            try {
                valor = Double.parseDouble(valorStr);
                if (valor <= 0) {
                    Toast.makeText(this, "Valor deve ser maior que zero", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            Despesa despesa = new Despesa(descricao, valor, data, categoria);

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstancia(getApplicationContext());
                db.despesaDao().inserir(despesa);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Despesa salva com sucesso!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);  // <- Aqui dentro
                    finish();              // <- Aqui dentro
                });
            }).start();
        });
    }}