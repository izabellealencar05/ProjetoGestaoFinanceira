package com.example.trabalhogestao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class Home extends AppCompatActivity {
    private ImageView foto;
    private TextView tvBemVindo;
    private Button logout, btnCadastrarDespesa;
    private RecyclerView rvDespesas;

    private FirebaseAuth firebaseAuth;
    private AppDatabase db;
    private DespesaDao despesaDao;
    private DespesaAdapter adapter;
    private CredentialManager credentialManager;

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

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(foto);
            tvBemVindo.setText("Bem-vindo, " + user.getDisplayName());
        }

        // DB
        db = AppDatabase.getInstancia(this);
        despesaDao = db.despesaDao();

        // RecyclerView
        carregarDespesas();

        // Botão para cadastrar
        btnCadastrarDespesa.setOnClickListener(view -> {
            startActivity(new Intent(Home.this, CadastroDespesa.class));
        });

        // Logout
        credentialManager = CredentialManager.create(this);
        logout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(getApplicationContext(), "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Home.this, MainActivity.class));
            finish();
        });
    }

    private void carregarDespesas() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Busca as despesas do banco em uma thread de fundo
            List<Despesa> listaDespesas = despesaDao.listarTodas();

            // Atualiza a RecyclerView na thread principal (UI thread)
            runOnUiThread(() -> {
                adapter = new DespesaAdapter(listaDespesas);
                rvDespesas.setLayoutManager(new LinearLayoutManager(this));
                rvDespesas.setAdapter(adapter);
            });
        });
    }

}
