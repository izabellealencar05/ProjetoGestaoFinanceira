package com.example.trabalhogestao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    SignInButton btSignIn;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btSignIn = findViewById(R.id.signInButton);

        //TODO: O TOKEN DEVE SER TROCADO! DEVE SER O TOKEN DO SEU PROJETO NO FIREBASE
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("769858233897-to1b9l01048ttjbaua7gldhv12gt27l4.apps.googleusercontent.com") //TODO TOKEN A SER TROCADO
                .requestEmail()
                .build();

        // Inicializando serviço de login no Google Client
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);

        btSignIn.setOnClickListener((View.OnClickListener) view -> {
            //Cria um novo intent
            Intent intent = googleSignInClient.getSignInIntent();

            startActivityForResult(intent, 100);
        });

        // Inicializa autenticação firebase
        firebaseAuth = FirebaseAuth.getInstance();
        // Inicializa usuário firebase
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        // Verifica se o usuário foi autenticado
        if (firebaseUser != null) {
            // Se o usuário está autenticado, chama a segunda tela
            startActivity(new Intent(MainActivity.this, Profile.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {

            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (signInAccountTask.isSuccessful()) {

                Toast.makeText(this, "Login com Google bem-sucedido", Toast.LENGTH_SHORT).show();


                try {

                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);

                    if (googleSignInAccount != null) {

                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

                        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    startActivity(new Intent(MainActivity.this, Profile.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                                } else {
                                    Toast.makeText(MainActivity.this, "Erro de autenticação: ", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}