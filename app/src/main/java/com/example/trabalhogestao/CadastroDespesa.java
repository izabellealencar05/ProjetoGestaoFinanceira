package com.example.trabalhogestao;

import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CadastroDespesa extends AppCompatActivity {

    private EditText etDescricao, etValor, etData;
    private Spinner spinnerCategoria;
    private ImageButton btnNovaCategoria;
    private Button btnSalvar, btnVoltar;

    private AppDatabase db;
    private List<Categoria> listaCategorias = new ArrayList<>();
    private ArrayAdapter<Categoria> categoriaAdapter;

    private Despesa despesaParaEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_despesa);

        db = AppDatabase.getInstancia(getApplicationContext());

        etDescricao = findViewById(R.id.etDescricao);
        etValor = findViewById(R.id.etValor);
        etData = findViewById(R.id.etData);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        btnNovaCategoria = findViewById(R.id.btnNovaCategoria);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnVoltar = findViewById(R.id.btnVoltar);

        configurarSpinner();
        carregarCategorias();

        btnVoltar.setOnClickListener(v -> finish());
        btnNovaCategoria.setOnClickListener(v -> abrirDialogNovaCategoria());
        btnSalvar.setOnClickListener(v -> salvarDespesa());

        if (getIntent().hasExtra("DESPESA_ID")) {
            setTitle("Editar Despesa");
            int despesaId = getIntent().getIntExtra("DESPESA_ID", -1);
            if (despesaId != -1) {
                carregarDespesaParaEdicao(despesaId);
            }
        } else {
            setTitle("Cadastrar Despesa");
        }
    }

    private void carregarDespesaParaEdicao(int despesaId) {
        new Thread(() -> {
            despesaParaEditar = db.despesaDao().getDespesaById(despesaId);
            if (despesaParaEditar != null) {
                runOnUiThread(this::preencherCamposParaEdicao);
            }
        }).start();
    }

    private void preencherCamposParaEdicao() {
        etDescricao.setText(despesaParaEditar.getDescricao());
        etValor.setText(String.valueOf(despesaParaEditar.getValor()));

        SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatoExibicao = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date data = formatoBanco.parse(despesaParaEditar.getData());
            etData.setText(formatoExibicao.format(data));
        } catch (ParseException e) {
            etData.setText(despesaParaEditar.getData());
        }

        for (int i = 0; i < categoriaAdapter.getCount(); i++) {
            Categoria c = categoriaAdapter.getItem(i);
            if (c != null && c.getId() == despesaParaEditar.getCategoriaId()) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }
    }

    private void configurarSpinner() {
        categoriaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaCategorias);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);
    }

    private void carregarCategorias() {
        new Thread(() -> {
            List<Categoria> categoriasDoBanco = db.categoriaDao().listarTodas();
            runOnUiThread(() -> {
                listaCategorias.clear();
                listaCategorias.addAll(categoriasDoBanco);
                categoriaAdapter.notifyDataSetChanged();
                if (despesaParaEditar != null) {
                    preencherCamposParaEdicao();
                }
            });
        }).start();
    }

    private void abrirDialogNovaCategoria() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nova Categoria");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nomeCategoria = input.getText().toString().trim();
            if (!nomeCategoria.isEmpty()) {
                salvarNovaCategoria(nomeCategoria);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void salvarNovaCategoria(String nome) {
        Categoria novaCategoria = new Categoria(nome);
        new Thread(() -> {
            try {
                db.categoriaDao().inserir(novaCategoria);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Categoria salva!", Toast.LENGTH_SHORT).show();
                    carregarCategorias();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Categoria já existe!", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void salvarDespesa() {
        String descricao = etDescricao.getText().toString().trim();
        String valorStr = etValor.getText().toString().trim();
        String dataInput = etData.getText().toString().trim();
        Categoria categoriaSelecionada = (Categoria) spinnerCategoria.getSelectedItem();

        if (descricao.isEmpty() || valorStr.isEmpty() || dataInput.isEmpty() || categoriaSelecionada == null) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(valorStr);
            if(valor <= 0) {
                Toast.makeText(this, "Valor deve ser maior que zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataFormatada;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dataInput);
            dataFormatada = dbFormat.format(date);
        } catch(ParseException e) {
            Toast.makeText(this, "Data inválida. Use o formato dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoriaId = categoriaSelecionada.getId();

        if (despesaParaEditar != null) {
            despesaParaEditar.setDescricao(descricao);
            despesaParaEditar.setValor(valor);
            despesaParaEditar.setData(dataFormatada);
            despesaParaEditar.setCategoriaId(categoriaId);
            atualizarDespesa(despesaParaEditar);
        } else {
            Despesa novaDespesa = new Despesa(descricao, valor, dataFormatada, categoriaId);
            inserirDespesa(novaDespesa);
        }
    }

    private void inserirDespesa(Despesa despesa) {
        new Thread(() -> {
            db.despesaDao().inserir(despesa);
            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa salva com sucesso!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }

    private void atualizarDespesa(Despesa despesa) {
        new Thread(() -> {
            db.despesaDao().atualizar(despesa);
            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }
}
