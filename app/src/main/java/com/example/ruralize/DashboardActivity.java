package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.cardview.widget.CardView;

public class DashboardActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        configurarCliques();
    }

    private void configurarCliques() {
        // Card Gerenciar Produtos
        CardView cardProdutos = findViewById(R.id.cardProdutos);
        cardProdutos.setOnClickListener(v -> {
            Intent intent = new Intent(this, GerenciarProdutosActivity.class);
            startActivity(intent);
        });

        // Card Minha Conta
        CardView cardConta = findViewById(R.id.cardConta);
        cardConta.setOnClickListener(v -> {
            Intent intent = new Intent(this, MinhaContaActivity.class);
            startActivity(intent);
        });

        // Botão Sair
        findViewById(R.id.tvSair).setOnClickListener(v -> {
            finish(); // Volta para a tela de login
        });
    }
}