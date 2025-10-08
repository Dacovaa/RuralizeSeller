package seller.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;

import seller.view.NovoProdutoActivity;
import com.example.ruralizeseller.R;

public class GerenciarProdutosActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_produtos);

        configurarCliques();
    }

    private void configurarCliques() {
        // Botão Voltar
        findViewById(R.id.btnVoltar).setOnClickListener(v -> {
            finish();
        });

        // Botão Novo Produto
        findViewById(R.id.btnNovoProduto).setOnClickListener(v -> {
            Intent intent = new Intent(this, NovoProdutoActivity.class);
            startActivity(intent);
        });
    }
}