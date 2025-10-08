package seller.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.ruralizeseller.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MinhaContaActivity extends ComponentActivity {

    private TextInputEditText edtEmpresa, edtCnpj, edtEmail, edtTelefone;
    private TextInputLayout tilEmpresa, tilCnpj, tilEmail, tilTelefone;
    private MaterialButton btnSalvar, btnAlterarSenha, btnSair;
    private TextView txtDataCadastro, txtStatus;

    // Dados do usuário (simulados)
    private String usuarioEmpresa = "AgroTech Solutions LTDA";
    private String usuarioCnpj = "12345678000195";
    private String usuarioEmail = "contato@agrotech.com.br";
    private String usuarioTelefone = "(11) 99999-9999";
    private String usuarioDataCadastro = "15/03/2024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minha_conta);

        inicializarComponentes();
        carregarDadosUsuario();
        configurarCliques();
        configurarMascaras();
    }

    private void inicializarComponentes() {
        // TextInputLayouts
        tilEmpresa = findViewById(R.id.tilEmpresa);
        tilCnpj = findViewById(R.id.tilCnpj);
        tilEmail = findViewById(R.id.tilEmail);
        tilTelefone = findViewById(R.id.tilTelefone);

        // TextInputEditTexts
        edtEmpresa = findViewById(R.id.edtEmpresa);
        edtCnpj = findViewById(R.id.edtCnpj);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefone = findViewById(R.id.edtTelefone);

        // Botões
        btnSalvar = findViewById(R.id.btnSalvar);
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha);
        btnSair = findViewById(R.id.btnSair);

        // TextViews informativos
        txtDataCadastro = findViewById(R.id.txtDataCadastro);
        txtStatus = findViewById(R.id.txtStatus);
    }

    private void carregarDadosUsuario() {
        // Preencher campos com dados do usuário
        edtEmpresa.setText(usuarioEmpresa);
        edtCnpj.setText(usuarioCnpj);
        edtEmail.setText(usuarioEmail);
        edtTelefone.setText(usuarioTelefone);

        // Preencher informações de cadastro
        txtDataCadastro.setText(usuarioDataCadastro);
        txtStatus.setText("Ativa");
        txtStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
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

    private void configurarMascaras() {
        // Máscara para telefone
        edtTelefone.addTextChangedListener(new TelefoneMask());
    }

    private void validarESalvar() {
        String empresa = edtEmpresa.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();

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

        if (telefone.isEmpty()) {
            tilTelefone.setError("Telefone é obrigatório");
            return;
        }

        String telefoneLimpo = telefone.replaceAll("[^0-9]", "");
        if (telefoneLimpo.length() < 10) {
            tilTelefone.setError("Telefone inválido");
            return;
        }

        // Se todas as validações passarem, salvar alterações
        salvarAlteracoes(empresa, email, telefone);
    }

    private void limparErros() {
        tilEmpresa.setError(null);
        tilEmail.setError(null);
        tilTelefone.setError(null);
    }

    private void salvarAlteracoes(String empresa, String email, String telefone) {
        // Mostrar loading
        btnSalvar.setEnabled(false);
        btnSalvar.setText("SALVANDO...");

        // Simular salvamento (substituir por chamada à API)
        new android.os.Handler().postDelayed(() -> {
            // Atualizar dados do usuário
            usuarioEmpresa = empresa;
            usuarioEmail = email;
            usuarioTelefone = telefone;

            // TODO: Salvar no banco de dados real

            mostrarSucessoSalvamento();

            // Restaurar botão
            btnSalvar.setEnabled(true);
            btnSalvar.setText("SALVAR ALTERAÇÕES");
        }, 1500);
    }

    private void mostrarSucessoSalvamento() {
        Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private void abrirAlteracaoSenha() {
        // Criar diálogo para alteração de senha
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
        // Validações básicas
        if (senhaAtual.isEmpty()) {
            Toast.makeText(this, "Digite sua senha atual", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Verificar se senha atual está correta (comparar com banco de dados)

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

        // Simular alteração de senha
        Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show();
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
        // TODO: Implementar lógica de logout (limpar sessão, etc.)

        // Voltar para tela de login
        Intent intent = new Intent(this, Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show();
    }

    // Classe para máscara de telefone
    private static class TelefoneMask implements android.text.TextWatcher {
        private boolean isUpdating = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(android.text.Editable s) {
            if (isUpdating) {
                isUpdating = false;
                return;
            }

            String str = s.toString().replaceAll("[^\\d]", "");
            if (str.length() > 11) {
                str = str.substring(0, 11);
            }

            StringBuilder formatted = new StringBuilder();
            if (str.length() >= 2) {
                formatted.append("(").append(str.substring(0, 2)).append(") ");
                if (str.length() >= 7) {
                    formatted.append(str.substring(2, 7)).append("-");
                    if (str.length() >= 11) {
                        formatted.append(str.substring(7, 11));
                    } else {
                        formatted.append(str.substring(7));
                    }
                } else {
                    formatted.append(str.substring(2));
                }
            } else {
                formatted.append(str);
            }

            isUpdating = true;
            s.replace(0, s.length(), formatted.toString());
        }
    }
}