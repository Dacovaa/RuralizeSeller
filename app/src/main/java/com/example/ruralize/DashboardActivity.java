package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        inicializarComponentes();
        setupToolbar();
        setupNavigationDrawer();
        configurarCliques();
    }

    private void inicializarComponentes() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setTitle("Dashboard");
    }

    private void setupNavigationDrawer() {
        // Abrir/fechar drawer ao clicar no ícone do menu
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });

        // Configurar clique nos itens do menu
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.nav_produtos) {
                Intent intent = new Intent(this, GerenciarProdutosActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_vendas) {
                getSupportActionBar().setTitle("Vendas");
                // Aqui você pode carregar a tela de Vendas
            } else if (id == R.id.nav_catalogo) {
                getSupportActionBar().setTitle("Catálogo");
                // Aqui você pode carregar a tela de Catálogo
            } else if (id == R.id.nav_estoque) {
                getSupportActionBar().setTitle("Estoque");
                // Aqui você pode carregar a tela de Estoque
            } else if (id == R.id.nav_entregas) {
                getSupportActionBar().setTitle("Entregas");
                // Aqui você pode carregar a tela de Entregas
            } else if (id == R.id.nav_minha_conta) {
                Intent intent = new Intent(this, MinhaContaActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_sair) {
                fazerLogout();
            }

            // Fechar drawer após seleção
            drawerLayout.closeDrawer(navigationView);
            return true;
        });
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

        // Botão Sair no header (mantido para compatibilidade)
        findViewById(R.id.tvSair).setOnClickListener(v -> fazerLogout());
    }

    private void fazerLogout() {
        mAuth.signOut();
        Intent intent = new Intent(DashboardActivity.this, Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }
}