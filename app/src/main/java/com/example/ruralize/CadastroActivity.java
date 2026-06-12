package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.ruralize.network.ApiConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText edtEmpresa, edtCnpj, edtEmail, edtSenha;
    private TextInputLayout tilEmpresa, tilCnpj, tilEmail, tilSenha;
    private MaterialButton btnCadastrar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();
        configurarCliques();

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
    }

    private void mostrarSucessoCadastro() {
        new AlertDialog.Builder(this)
                .setTitle("Cadastro Realizado!")
                .setMessage("Sua conta foi criada com sucesso. Faça login para continuar.")
                .setPositiveButton("OK", (dialog, which) -> {
                    startActivity(new Intent(this, Activity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarErroCadastro() {
        new AlertDialog.Builder(this)
                .setTitle("Erro no Cadastro")
                .setMessage("Não foi possível criar sua conta. Tente novamente.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void inicializarComponentes() {
        edtEmpresa = findViewById(R.id.edtEmpresa);
        edtCnpj = findViewById(R.id.edtCnpj);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        tilEmpresa = findViewById(R.id.tilEmpresa);
        tilCnpj = findViewById(R.id.tilCnpj);
        tilEmail = findViewById(R.id.tilEmail);
        tilSenha = findViewById(R.id.tilSenha);

        btnCadastrar = findViewById(R.id.btnCadastrar);
    }

    private void configurarCliques() {
        // Texto Login
        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, Activity.class));
            finish();
        });

        // Botão Cadastrar
        btnCadastrar.setOnClickListener(v -> validarECadastrar());
    }

    private void validarECadastrar() {
        String empresa = edtEmpresa.getText().toString().trim();
        String cnpj = edtCnpj.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // Limpar erros anteriores
        limparErros();

        // Validações
        if (empresa.isEmpty()) {
            tilEmpresa.setError("Nome da empresa é obrigatório");
            return;
        }

        if (cnpj.isEmpty()) {
            tilCnpj.setError("CNPJ é obrigatório");
            return;
        }

        if (cnpj.length() != 14) {
            tilCnpj.setError("CNPJ deve ter 14 dígitos");
            return;
        }
        if (email.isEmpty()) {
            tilEmail.setError("Email é obrigatório");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email inválido");
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

        // Se todas as validações passarem, fazer cadastro
        fazerCadastro(empresa, cnpj, email, senha);
    }

    private void limparErros() {
        tilEmpresa.setError(null);
        tilCnpj.setError(null);
        tilEmail.setError(null);
        tilSenha.setError(null);
    }

    private void fazerCadastro(String empresa, String cnpj, String email, String senha) {
        // Mostrar loading
        btnCadastrar.setEnabled(false);
        btnCadastrar.setText("Cadastrando...");

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveExtraUserData(cnpj, empresa, email, senha);
                    } else {
                        Toast.makeText(this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        btnCadastrar.setEnabled(true);
        btnCadastrar.setText("Cadastrar");
    }

    private void saveExtraUserData(String cnpj, String empresa, String email, String senha) {

        btnCadastrar.setEnabled(false);
        btnCadastrar.setText("Salvando...");

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("email", email);
        data.put("password", senha);
        data.put("displayName", empresa);
        data.put("cnpj", cnpj);

        com.example.ruralize.network.services.AuthService authService = com.example.ruralize.network.RetrofitClient.getClient().create(com.example.ruralize.network.services.AuthService.class);
        authService.signUp(data).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<okhttp3.ResponseBody> call, @androidx.annotation.NonNull retrofit2.Response<okhttp3.ResponseBody> response) {
                runOnUiThread(() -> {
                    btnCadastrar.setEnabled(true);
                    btnCadastrar.setText("Cadastrar");

                    if (response.isSuccessful()) {
                        mostrarSucessoCadastro();
                    } else {
                        mostrarErroCadastro();
                    }
                });
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<okhttp3.ResponseBody> call, @androidx.annotation.NonNull Throwable t) {
                runOnUiThread(() -> {
                    btnCadastrar.setEnabled(true);
                    btnCadastrar.setText("Cadastrar");
                    mostrarErroCadastro();
                });
            }
        });
    }
}


