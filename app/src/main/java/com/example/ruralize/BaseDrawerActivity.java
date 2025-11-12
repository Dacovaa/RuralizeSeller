package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseDrawerActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_drawer);

        drawerLayout = findViewById(R.id.drawerLayoutBase);
        navigationView = findViewById(R.id.navigationViewBase);
        toolbar = findViewById(R.id.toolbarBase);
        firebaseAuth = FirebaseAuth.getInstance();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelectedInternal);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        throw new UnsupportedOperationException("Use setContentLayout() instead.");
    }

    protected void setContentLayout(@LayoutRes int layoutResID) {
        View contentFrame = findViewById(R.id.contentFrame);
        if (contentFrame instanceof android.widget.FrameLayout) {
            ((android.widget.FrameLayout) contentFrame).removeAllViews();
            LayoutInflater.from(this).inflate(layoutResID, (android.widget.FrameLayout) contentFrame, true);
        }

        navigationView.getMenu().setGroupCheckable(0, true, true);
        int menuId = getCurrentMenuItemId();
        if (menuId != 0) {
            navigationView.setCheckedItem(menuId);
        }
    }

    protected void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private boolean onNavigationItemSelectedInternal(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(navigationView);
        int id = menuItem.getItemId();
        if (id == getCurrentMenuItemId()) {
            return true;
        }

        Intent intent = null;
        if (id == R.id.nav_dashboard) {
            intent = new Intent(this, DashboardActivity.class);
        } else if (id == R.id.nav_produtos) {
            intent = new Intent(this, GerenciarProdutosActivity.class);
        } else if (id == R.id.nav_vendas) {
            intent = new Intent(this, VendasActivity.class);
        } else if (id == R.id.nav_catalogo) {
            onCatalogoSelected();
        } else if (id == R.id.nav_estoque) {
            intent = new Intent(this, EstoqueActivity.class);
        } else if (id == R.id.nav_entregas) {
            intent = new Intent(this, EntregasActivity.class);
        } else if (id == R.id.nav_minha_conta) {
            intent = new Intent(this, MinhaContaActivity.class);
        } else if (id == R.id.nav_sair) {
            realizarLogout();
            return true;
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return true;
    }

    protected void onCatalogoSelected() {
        Toast.makeText(this, "Catálogo disponível em breve.", Toast.LENGTH_SHORT).show();
    }

    protected void realizarLogout() {
        firebaseAuth.signOut();
        Intent intent = new Intent(this, Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    protected int getCurrentMenuItemId() {
        return 0;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && navigationView != null && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }
}

