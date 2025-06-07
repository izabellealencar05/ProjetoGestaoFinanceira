package com.example.trabalhogestao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Home extends AppCompatActivity {
    private ImageView foto;
    private TextView tvBemVindo;
    private Button logout, btnCadastrarDespesa, btnGerarRelatorio;
    private RecyclerView rvDespesas;

    private FirebaseAuth firebaseAuth;
    private AppDatabase db;
    private DespesaDao despesaDao;
    private DespesaAdapter adapter; // Esta é a única declaração necessária

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Views
        foto = findViewById(R.id.imageViewFoto);
        tvBemVindo = findViewById(R.id.textViewNome);
        logout = findViewById(R.id.buttonLogout);
        btnCadastrarDespesa = findViewById(R.id.btnCadastrarDespesa);
        rvDespesas = findViewById(R.id.rvDespesas);
        btnGerarRelatorio = findViewById(R.id.btnGerarRelatorio);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(foto);
            tvBemVindo.setText("Bem-vindo (a), " + user.getDisplayName());
        }

        // DB
        db = AppDatabase.getInstancia(this);
        despesaDao = db.despesaDao();

        // RecyclerView
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
            // A lógica aqui está perfeita, usando o novo método do DAO
            List<DespesaComCategoria> listaDespesas = despesaDao.listarTodasComCategoria();

            runOnUiThread(() -> {
                adapter = new DespesaAdapter(listaDespesas);
                rvDespesas.setLayoutManager(new LinearLayoutManager(this));
                rvDespesas.setAdapter(adapter);
            });
        });
    }

    // Usando o método mais moderno para aguardar o resultado de uma Activity
    private final ActivityResultLauncher<Intent> cadastroDespesaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Se a despesa foi salva com sucesso, atualiza a lista
                    carregarDespesas();
                }
            }
    );
}