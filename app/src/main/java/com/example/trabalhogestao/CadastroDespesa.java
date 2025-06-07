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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_despesa);

        db = AppDatabase.getInstancia(getApplicationContext());

        // Vinculando os componentes do novo XML
        etDescricao = findViewById(R.id.etDescricao);
        etValor = findViewById(R.id.etValor);
        etData = findViewById(R.id.etData);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        btnNovaCategoria = findViewById(R.id.btnNovaCategoria);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnVoltar = findViewById(R.id.btnVoltar);

        configurarSpinner();
        carregarCategorias();

        // Configurando os cliques dos botões
        btnVoltar.setOnClickListener(v -> finish());
        btnNovaCategoria.setOnClickListener(v -> abrirDialogNovaCategoria());
        btnSalvar.setOnClickListener(v -> salvarDespesa());
    }

    private void configurarSpinner() {
        // O ArrayAdapter usará o método toString() da classe Categoria para exibir o nome
        categoriaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaCategorias);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);
    }

    private void carregarCategorias() {
        // Busca as categorias do banco em uma thread separada
        new Thread(() -> {
            List<Categoria> categoriasDoBanco = db.categoriaDao().listarTodas();
            runOnUiThread(() -> {
                listaCategorias.clear();
                listaCategorias.addAll(categoriasDoBanco);
                categoriaAdapter.notifyDataSetChanged(); // Avisa o spinner que os dados mudaram
            });
        }).start();
    }

    private void abrirDialogNovaCategoria() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nova Categoria");

        // Cria um campo de texto para o usuário digitar o nome
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Configura o botão "Salvar" do diálogo
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
            // Tenta inserir a nova categoria no banco
            long novoId = db.categoriaDao().inserir(novaCategoria);

            // Se a inserção falhar (ex: nome duplicado), o ID retornado será -1
            if (novoId != -1) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Categoria salva!", Toast.LENGTH_SHORT).show();
                    carregarCategorias(); // Recarrega o spinner para exibir a nova categoria
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erro: Categoria já existe.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void salvarDespesa() {
        String descricao = etDescricao.getText().toString().trim();
        String valorStr = etValor.getText().toString().trim();
        String dataInput = etData.getText().toString().trim();

        // Pega o objeto Categoria inteiro que foi selecionado no spinner
        Categoria categoriaSelecionada = (Categoria) spinnerCategoria.getSelectedItem();

        if (descricao.isEmpty() || valorStr.isEmpty() || dataInput.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoriaSelecionada == null) {
            Toast.makeText(this, "Nenhuma categoria selecionada. Crie uma primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação do valor
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

        // Validação da data
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dataFormatada;
        try {
            Date date = inputFormat.parse(dataInput);
            dataFormatada = dbFormat.format(date);
        } catch (ParseException e) {
            Toast.makeText(this, "Data inválida. Use o formato dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pega o ID da categoria que foi selecionada
        int categoriaId = categoriaSelecionada.getId();
        Despesa despesa = new Despesa(descricao, valor, dataFormatada, categoriaId);

        new Thread(() -> {
            db.despesaDao().inserir(despesa);
            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa salva com sucesso!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Avisa a tela Home que uma nova despesa foi salva
                finish(); // Fecha a tela de cadastro
            });
        }).start();
    }
}