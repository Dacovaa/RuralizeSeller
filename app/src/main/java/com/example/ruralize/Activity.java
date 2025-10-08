package com.example.ruralize;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class Activity extends ComponentActivity {

    private TextInputEditText edtCnpj, edtSenha;
    private TextInputLayout tilCnpj, tilSenha;
    private MaterialButton btnEntrar, btnCadastro;

    private OkHttpClient client;
    private Gson gson;
    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        // Inicializar OkHttpClient e Gson
        client = new OkHttpClient();
        gson = new Gson();

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        inicializarComponentes();
        configurarCliques();


    }

    private void inicializarComponentes() {
        edtCnpj = findViewById(R.id.edtCnpj);
        edtSenha = findViewById(R.id.edtSenha);
        tilCnpj = findViewById(R.id.tilCnpj);
        tilSenha = findViewById(R.id.tilSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCadastro = findViewById(R.id.btnCadastro);
    }

    private void configurarCliques() {
        // Esqueceu a senha
        findViewById(R.id.tvForgot).setOnClickListener(v -> new RecuperacaoSenhaActivity());

        // Botão Entrar
        btnEntrar.setOnClickListener(v -> validarELogar());

        // Botão Cadastro
        btnCadastro.setOnClickListener(v -> abrirCadastro());
    }

    private void validarELogar() {
        String cnpj = edtCnpj.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // Limpar erros anteriores
        tilCnpj.setError(null);
        tilSenha.setError(null);

        // Validações
        if (cnpj.isEmpty()) {
            tilCnpj.setError("CNPJ é obrigatório");
            return;
        }

        if (cnpj.length() != 14) {
            tilCnpj.setError("CNPJ deve ter 14 dígitos");
            return;
        }

        if (senha.isEmpty()) {
            tilSenha.setError("Senha é obrigatória");
            return;
        }

        if (senha.length() < 6) {
            tilSenha.setError("Senha deve ter pelo menos 6 caracteres");
            return;
        }

        // Se todas as validações passarem, fazer login
        fazerLogin(cnpj, senha);
    }

    private void fazerLogin(String email, String senha) {
        // Mostrar loading
        btnEntrar.setEnabled(false);
        btnEntrar.setText("ENTRANDO...");

        // Simular processo de login
        new android.os.Handler().postDelayed(() -> {
            autenticarComFirebase(email, senha);
        }, 2000);
    }

    private void autenticarComFirebase(String email, String senha) {
        // Primeiro, precisamos obter o email associado ao CNPJ
        // Você pode ter uma coleção no Firestore que mapeia CNPJ para email
        // Por enquanto, vou assumir que você tem uma forma de obter o email do CNPJ

        // Se você tiver uma API para mapear CNPJ para email, use aqui:

        if (email == null || email.isEmpty()) {
            // Se não encontrou email para o CNPJ
            btnEntrar.setEnabled(true);
            btnEntrar.setText("ENTRAR");
            return;
        }


        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(Activity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Falha no login
                        runOnUiThread(() -> {
                            btnEntrar.setEnabled(true);
                            btnEntrar.setText("ENTRAR");
                        });
                    }
                });
    }
    private void abrirCadastro() {
        Intent intent = new Intent(this, CadastroActivity.class);
        startActivity(intent);
    }
}
