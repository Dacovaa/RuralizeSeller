package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CadastroActivity extends ComponentActivity {

    private TextInputEditText edtEmpresa, edtCnpj, edtEmail, edtSenha;
    private TextInputLayout tilEmpresa, tilCnpj, tilEmail, tilSenha;
    private MaterialButton btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();
        configurarCliques();
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

        if (!validarCNPJ(cnpj)) {
            tilCnpj.setError("CNPJ inválido");
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

        // Simular processo de cadastro
        new android.os.Handler().postDelayed(() -> {
            if (cadastrarNaAPI(empresa, cnpj, email, senha)) {
                mostrarSucessoCadastro();
            } else {
                mostrarErroCadastro();
            }

            // Restaurar botão
            btnCadastrar.setEnabled(true);
            btnCadastrar.setText("CADASTRAR");
        }, 2000);
    }

    private boolean cadastrarNaAPI(String empresa, String cnpj, String email, String senha) {
        // TODO: Implementar chamada real à API
        return true;
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

    private boolean validarCNPJ(String cnpj) {
        // Remover caracteres não numéricos (caso ainda tenha)
        cnpj = cnpj.replaceAll("[^0-9]", "");

        if (cnpj.length() != 14) return false;

        // Verificar se todos os dígitos são iguais
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            // Cálculo do primeiro dígito verificador
            int soma = 0;
            int peso = 2;
            for (int i = 11; i >= 0; i--) {
                soma += (cnpj.charAt(i) - '0') * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }
            int digito1 = 11 - (soma % 11);
            if (digito1 >= 10) digito1 = 0;

            // Cálculo do segundo dígito verificador
            soma = 0;
            peso = 2;
            for (int i = 12; i >= 0; i--) {
                soma += (cnpj.charAt(i) - '0') * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }
            int digito2 = 11 - (soma % 11);
            if (digito2 >= 10) digito2 = 0;

            // Verificar se os dígitos calculados conferem com os informados
            return (cnpj.charAt(12) - '0' == digito1) && (cnpj.charAt(13) - '0' == digito2);
        } catch (Exception e) {
            return false;
        }
    }
}