package com.example.ruralize;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

public class CadastroActivity extends ComponentActivity {

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
        btnCadastrar.setText("CADASTRANDO...");

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Usuário criado com sucesso
                        String uid = mAuth.getCurrentUser().getUid();

                        // 2️ Chamar API da sua backend para salvar dados extras
                        saveExtraUserData(uid, cnpj, empresa, email,senha);
                    } else {
                        // Erro ao criar usuário
                        Toast.makeText(this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        // Restaurar botão
        btnCadastrar.setEnabled(true);
        btnCadastrar.setText("CADASTRAR");
    }

    private void saveExtraUserData(String uid, String cnpj, String empresa, String email, String senha) {
        // Mostrar loading
        btnCadastrar.setEnabled(false);
        btnCadastrar.setText("SALVANDO...");

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        try {
            // Criar o JSON object com os dados
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", senha);
            jsonBody.put("displayName", empresa);
            jsonBody.put("cnpj", cnpj);

            Log.d("CADASTRO_API", "Enviando: " + jsonBody.toString());

            // Criar o request body
            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            // Criar a request
            Request request = new Request.Builder()
                    .url("https://ruralize-api.vercel.app/auth/signup")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Fazer a chamada assíncrona
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("CADASTRO_API", "Erro na chamada: " + e.getMessage());
                    runOnUiThread(() -> {
                        mostrarErroCadastro("Erro de conexão: " + e.getMessage());
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText("CADASTRAR");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseBody = response.body().string();
                    final int statusCode = response.code();

                    Log.d("CADASTRO_API", "Resposta: " + responseBody);
                    Log.d("CADASTRO_API", "Código: " + statusCode);

                    runOnUiThread(() -> {
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText("CADASTRAR");

                        if (response.isSuccessful()) {
                            // Cadastro bem-sucedido na API
                            mostrarSucessoCadastro();
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                String errorMessage = errorJson.optString("message", "Erro desconhecido");
                                mostrarErroCadastro();
                            } catch (JSONException e) {
                                mostrarErroCadastro();
                            }
                        }
                    });
                }
            });

        } catch (JSONException e) {
            Log.e("CADASTRO_API", "Erro ao criar JSON: " + e.getMessage());
            runOnUiThread(() -> {
                btnCadastrar.setEnabled(true);
                btnCadastrar.setText("CADASTRAR");
                mostrarErroCadastro();
            });
        }
    }
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
