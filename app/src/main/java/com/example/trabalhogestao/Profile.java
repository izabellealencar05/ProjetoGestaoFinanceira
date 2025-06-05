package com.example.trabalhogestao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.credentials.CredentialManager;


public class Profile extends AppCompatActivity {
    private ImageView foto;
    private TextView nome;
    private Button logout;
    private FirebaseAuth firebaseAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        foto = findViewById(R.id.imageViewFoto);
        nome = findViewById(R.id.textViewNome);
        logout = findViewById(R.id.buttonLogout);

        // Inicializa autenticaçõa Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Inicializa Usuário Firebase
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Verifica se o usuário está logado
        if (firebaseUser != null) {
            // Captura a foto e o nome do usuário logado
            Glide.with(this).load(firebaseUser.getPhotoUrl()).into(foto);
            nome.setText(firebaseUser.getDisplayName());
        }

        // Inicializa Credential Manager
        credentialManager = CredentialManager.create(this);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //faz logout
                firebaseAuth.signOut();

                // Exibe mensagem de logout
                Toast.makeText(getApplicationContext(), "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show();

                // Redireciona para a tela de login
                startActivity(new Intent(Profile.this, MainActivity.class));
                finish(); // Fecha a activity atual
            }
        });

    }
}