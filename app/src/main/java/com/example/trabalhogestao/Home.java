package com.example.trabalhogestao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Home extends AppCompatActivity implements DespesaAdapter.OnItemClickListener {
    private ImageView foto;
    private TextView tvBemVindo;
    private Button logout, btnCadastrarDespesa, btnGerarRelatorio;
    private RecyclerView rvDespesas;

    private FirebaseAuth firebaseAuth;
    private AppDatabase db;
    private DespesaDao despesaDao;
    private DespesaAdapter adapter;

    private final ActivityResultLauncher<Intent> cadastroDespesaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    carregarDespesas();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        foto = findViewById(R.id.imageViewFoto);
        tvBemVindo = findViewById(R.id.textViewNome);
        logout = findViewById(R.id.buttonLogout);
        btnCadastrarDespesa = findViewById(R.id.btnCadastrarDespesa);
        rvDespesas = findViewById(R.id.rvDespesas);
        btnGerarRelatorio = findViewById(R.id.btnGerarRelatorio);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(foto);
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                tvBemVindo.setText("Bem-vindo(a), " + user.getDisplayName());
            } else {
                tvBemVindo.setText("Bem-vindo(a)!");
            }
        }


        db = AppDatabase.getInstancia(this);
        despesaDao = db.despesaDao();

        carregarDespesas();

        btnCadastrarDespesa.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, CadastroDespesa.class);
            cadastroDespesaLauncher.launch(intent);
        });

        // Logout
        logout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(getApplicationContext(), "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Home.this, MainActivity.class));
            finish();
        });

        btnGerarRelatorio.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Relatorio.class);
            startActivity(intent);
        });
    }

    private void carregarDespesas() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<DespesaComCategoria> listaDespesas = despesaDao.listarTodasComCategoria();
            runOnUiThread(() -> {
                adapter = new DespesaAdapter(listaDespesas, this);
                rvDespesas.setLayoutManager(new LinearLayoutManager(this));
                rvDespesas.setAdapter(adapter);
            });
        });
    }

    @Override
    public void onEditClick(Despesa despesa) {
        Intent intent = new Intent(Home.this, CadastroDespesa.class);
        intent.putExtra("DESPESA_ID", despesa.getId());
        cadastroDespesaLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(Despesa despesa) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir esta despesa?")
                .setPositiveButton("Sim", (dialog, which) -> excluirDespesa(despesa))
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirDespesa(Despesa despesa) {
        new Thread(() -> {
            db.despesaDao().deletar(despesa);
            runOnUiThread(this::carregarDespesas);
        }).start();
    }
}