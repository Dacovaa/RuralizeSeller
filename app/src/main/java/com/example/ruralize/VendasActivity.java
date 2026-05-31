package com.example.ruralize;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ruralize.network.ApiConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VendasActivity extends BaseDrawerActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtSemVendas;
    private ProgressBar progressGlobal;
    private TextView txtResumoTotalPedidos;
    private TextView txtResumoUnidadesVendidas;
    private TextView txtResumoReceitaTotal;

    private final OkHttpClient client = new OkHttpClient();
    private FirebaseAuth mAuth;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_vendas);
        setToolbarTitle("Balanço de Vendas");

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        inicializarComponentes();
        configurarAcoes();
        carregarResumo();
    }

    private void inicializarComponentes() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshVendas);
        txtSemVendas = findViewById(R.id.txtSemVendas);
        progressGlobal = findViewById(R.id.progressCarregandoVendas);
        txtResumoTotalPedidos = findViewById(R.id.txtTotalPedidos);
        txtResumoUnidadesVendidas = findViewById(R.id.txtUnidadesVendidas);
        txtResumoReceitaTotal = findViewById(R.id.txtReceitaTotal);
    }

    private void configurarAcoes() {
        swipeRefreshLayout.setOnRefreshListener(this::carregarResumo);
    }

    private void carregarResumo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        exibirLoading(true);

        String uid = currentUser.getUid();
        String url = ApiConfig.salesByUser(uid);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    exibirLoading(false);
                    Toast.makeText(VendasActivity.this, "Erro ao carregar resumo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        exibirLoading(false);
                        Toast.makeText(VendasActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String responseBody = response.body().string();

                runOnUiThread(() -> atualizarResumo(responseBody));
            }
        });
    }

    private void atualizarResumo(String json) {
        if (json == null || json.isEmpty()) {
            mostrarResumoZerado();
            return;
        }

        try {
            JSONObject obj = new JSONObject(json);

            double total = obj.optDouble("total", 0.0);
            int totalOrders = obj.optInt("totalOrders", 0);
            int orderProductQuantity = obj.optInt("orderProductQuantity", 0);

            txtResumoReceitaTotal.setText(currencyFormat.format(total));
            txtResumoTotalPedidos.setText(String.valueOf(totalOrders));
            txtResumoUnidadesVendidas.setText(String.valueOf(orderProductQuantity));

            boolean semVendas = totalOrders == 0;
            txtSemVendas.setVisibility(semVendas ? View.VISIBLE : View.GONE);

        } catch (JSONException e) {
            Toast.makeText(this, "Nenhuma Venda Efetuada", Toast.LENGTH_SHORT).show();
            mostrarResumoZerado();
        }

        exibirLoading(false);
    }

    private void mostrarResumoZerado() {
        txtResumoReceitaTotal.setText("R$ 0,00");
        txtResumoTotalPedidos.setText("0");
        txtResumoUnidadesVendidas.setText("0");
        txtSemVendas.setVisibility(View.VISIBLE);
    }

    private void exibirLoading(boolean exibindo) {
        progressGlobal.setVisibility(exibindo ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_vendas;
    }
}
