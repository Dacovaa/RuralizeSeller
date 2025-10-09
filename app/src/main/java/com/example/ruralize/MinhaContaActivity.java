package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MinhaContaActivity extends ComponentActivity {

    private TextInputEditText edtEmpresa, edtCnpj, edtEmail;
    private TextInputLayout tilEmpresa, tilCnpj, tilEmail;
    private MaterialButton btnSalvar, btnAlterarSenha, btnSair;
    private TextView txtDataCadastro, txtStatus;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minha_conta);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            buscarDadosUsuarioDaApi(uid);
        }

        inicializarComponentes();
        configurarCliques();
    }

    private void inicializarComponentes() {
        // TextInputLayouts
        tilEmpresa = findViewById(R.id.tilEmpresa);
        tilCnpj = findViewById(R.id.tilCnpj);
        tilEmail = findViewById(R.id.tilEmail);

        // TextInputEditTexts
        edtEmpresa = findViewById(R.id.edtEmpresa);
        edtCnpj = findViewById(R.id.edtCnpj);
        edtEmail = findViewById(R.id.edtEmail);

        // Botões
        btnSalvar = findViewById(R.id.btnSalvar);
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha);
        btnSair = findViewById(R.id.btnSair);

        // TextViews informativos
        txtDataCadastro = findViewById(R.id.txtDataCadastro);
        txtStatus = findViewById(R.id.txtStatus);
    }

    private void buscarDadosUsuarioDaApi(String uid) {

        String url = String.format("https://ruralize-api.vercel.app/auth/%s", uid);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .get();

        okhttp3.Request request = requestBuilder.build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MinhaContaActivity.this, "Falha ao carregar dados", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseBody);

                        String empresa = json.getString("displayName");
                        String cnpj = json.getString("cnpj");
                        String email = json.getString("email");
                        JSONObject dataCadastro = json.getJSONObject("createdAt");

                        long seconds = dataCadastro.getLong("_seconds");
                        long nanos = dataCadastro.getLong("_nanoseconds");

                        long timestampMillis = seconds * 1000 + nanos / 1000000;
                        Date date = new Date(timestampMillis);

                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String dataFormatada = displayFormat.format(date);

                        runOnUiThread(() -> {
                            edtEmpresa.setText(empresa);
                            edtCnpj.setText(cnpj);
                            edtEmail.setText(email);
                            txtDataCadastro.setText(dataFormatada);
                            txtStatus.setText("Ativa");
                            txtStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MinhaContaActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void configurarCliques() {
        // Botão Voltar
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        // Botão Salvar
        btnSalvar.setOnClickListener(v -> validarESalvar());

        // Botão Alterar Senha
        btnAlterarSenha.setOnClickListener(v -> abrirAlteracaoSenha());

        // Botão Sair
        btnSair.setOnClickListener(v -> confirmarSaida());
    }

    private void validarESalvar() {
        String empresa = edtEmpresa.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String cnpj = edtCnpj.getText().toString().trim();

        // Limpar erros anteriores
        limparErros();

        // Validações
        if (empresa.isEmpty()) {
            tilEmpresa.setError("Nome da empresa é obrigatório");
            return;
        }

        if (empresa.length() < 3) {
            tilEmpresa.setError("Nome da empresa deve ter pelo menos 3 caracteres");
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

        // Se todas as validações passarem, salvar alterações
        salvarAlteracoes(empresa, email, cnpj);
    }

    private void limparErros() {
        tilEmpresa.setError(null);
        tilEmail.setError(null);
    }

    private void salvarAlteracoes(String empresa, String email, String cnpj) {
        btnSalvar.setEnabled(false);
        btnSalvar.setText("Salvando...");

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("uid", uid);
            jsonBody.put("email", email);
            jsonBody.put("displayName", empresa);
            jsonBody.put("cnpj", cnpj);


            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://ruralize-api.vercel.app/auth/update")
                    .patch(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        mostrarErroAtualizar();
                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar alterações");
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseBody = response.body().string();
                    final int statusCode = response.code();

                    runOnUiThread(() -> {
                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar alterações");

                        if (response.isSuccessful()) {
                            mostrarSucessoAtualizar();
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                String errorMessage = errorJson.optString("message", "Erro desconhecido");
                            } catch (JSONException e) {
                            }
                        }
                    });
                }
            });

        } catch (JSONException e) {
            runOnUiThread(() -> {
                btnSalvar.setEnabled(true);
                btnSalvar.setText("Salvar alterações");
                mostrarErroAtualizar();
            });
        }
    }


    private void mostrarErroAtualizar() {
        new AlertDialog.Builder(this)
                .setTitle("Erro ao atualizar cadastro")
                .setMessage("Não foi possível atualizar seus dados. Tente novamente.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void mostrarErroAtualizarSenha() {
        new AlertDialog.Builder(this)
                .setTitle("Erro ao atualizar senha")
                .setMessage("Não foi possível atualizar sua senha. Tente novamente.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void mostrarSucessoAtualizar() {
        new AlertDialog.Builder(this)
                .setTitle("Cadastro Atualizado!")
                .setMessage("Sua conta foi atualizada com sucesso!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void mostrarSucessoAtualizarSenha() {
        new AlertDialog.Builder(this)
                .setTitle("Senha Atualizada!")
                .setMessage("Sua senha foi atualizada com sucesso!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void abrirAlteracaoSenha() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alterar Senha");

        // Layout personalizado para o diálogo
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_alterar_senha, null);
        builder.setView(dialogView);

        TextInputEditText edtSenhaAtual = dialogView.findViewById(R.id.edtSenhaAtual);
        TextInputEditText edtNovaSenha = dialogView.findViewById(R.id.edtNovaSenha);
        TextInputEditText edtConfirmarSenha = dialogView.findViewById(R.id.edtConfirmarSenha);

        builder.setPositiveButton("Alterar", (dialog, which) -> {
            String senhaAtual = edtSenhaAtual.getText().toString();
            String novaSenha = edtNovaSenha.getText().toString();
            String confirmarSenha = edtConfirmarSenha.getText().toString();

            validarEAlterarSenha(senhaAtual, novaSenha, confirmarSenha);
        });

        builder.setNegativeButton("Cancelar", null);

        builder.show();
    }

    private void validarEAlterarSenha(String senhaAtual, String novaSenha, String confirmarSenha) {

        if (senhaAtual.isEmpty()) {
            Toast.makeText(this, "Digite sua senha atual", Toast.LENGTH_SHORT).show();
            return;
        }

        if (novaSenha.isEmpty()) {
            Toast.makeText(this, "Digite a nova senha", Toast.LENGTH_SHORT).show();
            return;
        }

        if (novaSenha.length() < 6) {
            Toast.makeText(this, "A nova senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("uid", uid);
            jsonBody.put("password", novaSenha);


            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://ruralize-api.vercel.app/auth/updatePassword")
                    .patch(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        mostrarErroAtualizarSenha();
                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar alterações");
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseBody = response.body().string();
                    final int statusCode = response.code();

                    runOnUiThread(() -> {
                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar alterações");

                        if (response.isSuccessful()) {
                            mostrarSucessoAtualizarSenha();
                        } else {
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                String errorMessage = errorJson.optString("message", "Erro desconhecido");
                            } catch (JSONException e) {
                            }
                        }
                    });
                }
            });

        } catch (JSONException e) {
            runOnUiThread(() -> {
                btnSalvar.setEnabled(true);
                btnSalvar.setText("Salvar alterações");
                mostrarErroAtualizar();
            });
        }
    }

    private void confirmarSaida() {
        new AlertDialog.Builder(this)
                .setTitle("Sair da Conta")
                .setMessage("Tem certeza que deseja sair? Você precisará fazer login novamente.")
                .setPositiveButton("Sair", (dialog, which) -> {
                    sairDaConta();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void sairDaConta() {

        Intent intent = new Intent(this, Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show();
    }
}