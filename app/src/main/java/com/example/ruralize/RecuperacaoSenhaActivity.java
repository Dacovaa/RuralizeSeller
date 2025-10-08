package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RecuperacaoSenhaActivity extends ComponentActivity {

    private TextInputEditText edtIdentificacao;
    private TextInputLayout tilIdentificacao;
    private MaterialButton btnRecuperar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperacao_senha);

        inicializarComponentes();
        configurarCliques();
    }

    private void inicializarComponentes() {
        edtIdentificacao = findViewById(R.id.edtIdentificacao);
        tilIdentificacao = findViewById(R.id.tilIdentificacao);
        btnRecuperar = findViewById(R.id.btnRecuperar);
    }

    private void configurarCliques() {
        // Texto Login
        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, Activity.class));
            finish();
        });

        // Botão Recuperar
        btnRecuperar.setOnClickListener(v -> validarERecuperar());
    }

    private void validarERecuperar() {
        String identificacao = edtIdentificacao.getText().toString().trim();

        // Limpar erro anterior
        tilIdentificacao.setError(null);

        // Validações
        if (identificacao.isEmpty()) {
            tilIdentificacao.setError("CNPJ ou email é obrigatório");
            return;
        }

        // Verificar se é email ou CNPJ
        boolean isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(identificacao).matches();
        boolean isCnpj = identificacao.replaceAll("[^0-9]", "").length() == 14;

        if (!isEmail && !isCnpj) {
            tilIdentificacao.setError("Informe um CNPJ válido (14 dígitos) ou email válido");
            return;
        }

        if (isCnpj && !validarCNPJ(identificacao.replaceAll("[^0-9]", ""))) {
            tilIdentificacao.setError("CNPJ inválido");
            return;
        }

        // Se todas as validações passarem, enviar recuperação
        enviarRecuperacao(identificacao, isEmail ? "email" : "cnpj");
    }

    private void enviarRecuperacao(String identificacao, String tipo) {
        // Mostrar loading
        btnRecuperar.setEnabled(false);
        btnRecuperar.setText("ENVIANDO...");

        // Simular processo de recuperação
        new android.os.Handler().postDelayed(() -> {
            if (enviarRecuperacaoNaAPI(identificacao, tipo)) {
                mostrarSucessoRecuperacao();
            } else {
                mostrarErroRecuperacao();
            }

            // Restaurar botão
            btnRecuperar.setEnabled(true);
            btnRecuperar.setText("RECUPERAR SENHA");
        }, 2000);
    }

    private boolean enviarRecuperacaoNaAPI(String identificacao, String tipo) {
        // TODO: Implementar chamada real à API
        return true;
    }

    private void mostrarSucessoRecuperacao() {
        new AlertDialog.Builder(this)
                .setTitle("Email Enviado!")
                .setMessage("Enviamos um link de recuperação para seu email cadastrado. Verifique sua caixa de entrada.")
                .setPositiveButton("OK", (dialog, which) -> {
                    startActivity(new Intent(this, Activity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarErroRecuperacao() {
        new AlertDialog.Builder(this)
                .setTitle("Erro na Recuperação")
                .setMessage("Não foi possível enviar o email de recuperação. Verifique se o CNPJ/email está correto.")
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean validarCNPJ(String cnpj) {
        // Remover caracteres não numéricos
        cnpj = cnpj.replaceAll("[^0-9]", "");

        if (cnpj.length() != 14) return false;
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            int soma = 0;
            int peso = 2;
            for (int i = 11; i >= 0; i--) {
                soma += (cnpj.charAt(i) - '0') * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }
            int digito1 = 11 - (soma % 11);
            if (digito1 >= 10) digito1 = 0;

            soma = 0;
            peso = 2;
            for (int i = 12; i >= 0; i--) {
                soma += (cnpj.charAt(i) - '0') * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }
            int digito2 = 11 - (soma % 11);
            if (digito2 >= 10) digito2 = 0;

            return (cnpj.charAt(12) - '0' == digito1) && (cnpj.charAt(13) - '0' == digito2);
        } catch (Exception e) {
            return false;
        }
    }
}