package seller.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;

import seller.view.DashboardActivity;import seller.view.RecuperacaoSenhaActivity;

import com.example.ruralizeseller.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Activity extends ComponentActivity {

    private TextInputEditText edtCnpj, edtSenha;
    private TextInputLayout tilCnpj, tilSenha;
    private MaterialButton btnEntrar, btnCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

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
        findViewById(R.id.tvForgot).setOnClickListener(v -> abrirRecuperacaoSenha());

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

        if (!validarCNPJ(cnpj)) {
            tilCnpj.setError("CNPJ inválido");
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

    private void fazerLogin(String cnpj, String senha) {
        // Mostrar loading
        btnEntrar.setEnabled(false);
        btnEntrar.setText("ENTRANDO...");

        // Simular processo de login
        new android.os.Handler().postDelayed(() -> {
            if (autenticarNaAPI(cnpj, senha)) {
                // Login bem-sucedido - redirecionar para Dashboard
                Intent intent = new Intent(Activity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Login falhou
                mostrarErroLogin();
                btnEntrar.setEnabled(true);
                btnEntrar.setText("ENTRAR");
            }
        }, 2000);
    }

    private boolean autenticarNaAPI(String cnpj, String senha) {
        // TODO: Implementar chamada real à API
        // Por enquanto, simula um login bem-sucedido para CNPJ específico
        return cnpj.equals("12345678000195") && senha.equals("123456");
    }

    private void mostrarErroLogin() {
        new AlertDialog.Builder(this)
                .setTitle("Erro no Login")
                .setMessage("CNPJ ou senha incorretos. Verifique suas credenciais.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void abrirRecuperacaoSenha() {
        Intent intent = new Intent(this, RecuperacaoSenhaActivity.class);
        startActivity(intent);
    }

    private void abrirCadastro() {
        Intent intent = new Intent(this, CadastroActivity.class);
        startActivity(intent);
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