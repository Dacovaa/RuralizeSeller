package com.example.ruralize;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.ruralize.ui.CatalogFragment;
import com.example.ruralize.ui.DashboardFragment;
import com.example.ruralize.ui.DeliveriesFragment;
import com.example.ruralize.ui.ProfileFragment;
import com.example.ruralize.ui.SalesFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "Ruralize";
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
                title = "Dashboard";
            } else if (itemId == R.id.nav_catalog) {
                selectedFragment = new CatalogFragment();
                title = "Catálogo";
            } else if (itemId == R.id.nav_sales) {
                selectedFragment = new SalesFragment();
                title = "Vendas";
            } else if (itemId == R.id.nav_deliveries) {
                selectedFragment = new DeliveriesFragment();
                title = "Entregas";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "Minha Conta";
            }

            if (selectedFragment != null) {
                toolbar.setTitle(title);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            toolbar.setTitle("Dashboard");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }
}
