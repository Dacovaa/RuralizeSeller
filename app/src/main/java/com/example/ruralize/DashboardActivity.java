package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.example.ruralize.network.ApiConfig;
import com.example.ruralize.view.MiniBarChartView;
import com.example.ruralize.view.MiniLineChartView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardActivity extends BaseDrawerActivity {

    private final OkHttpClient client = new OkHttpClient();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private TextView txtTotalVendas;
    private TextView txtTotalPedidos;
    private MiniLineChartView chartVendas;
    private MiniBarChartView chartPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.content_dashboard);
        setToolbarTitle("Dashboard");

        inicializarComponentes();
        configurarAcoes();
        carregarResumoVendas();
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_dashboard;
    }

    private void inicializarComponentes() {
        txtTotalVendas = findViewById(R.id.txtTotalVendas);
        txtTotalPedidos = findViewById(R.id.txtTotalPedidosDashboard);
        chartVendas = findViewById(R.id.chartVendas);
        chartPedidos = findViewById(R.id.chartPedidos);

        chartVendas.setData(criarPlaceholderValores(7));
        chartPedidos.setData(criarPlaceholderValores(7));
    }

    private void configurarAcoes() {
        CardView cardProdutos = findViewById(R.id.cardProdutos);
        cardProdutos.setOnClickListener(v -> startActivity(new Intent(this, GerenciarProdutosActivity.class)));

        CardView cardVendas = findViewById(R.id.cardVendas);
        cardVendas.setOnClickListener(v -> startActivity(new Intent(this, VendasActivity.class)));

        CardView cardConta = findViewById(R.id.cardConta);
        cardConta.setOnClickListener(v -> startActivity(new Intent(this, MinhaContaActivity.class)));
    }

    private void carregarResumoVendas() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String url = ApiConfig.salesByUser(uid);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Erro ao carregar vendas: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseBody = response.body().string();
                ResumoVendas resumo = interpretarVendas(responseBody);
                runOnUiThread(() -> atualizarResumo(resumo));
            }
        });
    }

    private ResumoVendas interpretarVendas(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return new ResumoVendas(0, 0);
        }

        try {
            JSONObject json = new JSONObject(responseBody);

            double total = json.optDouble("total", 0);
            int totalOrders = json.optInt("totalOrders", 0);

            return new ResumoVendas(total, totalOrders);

        } catch (JSONException e) {
            runOnUiThread(() ->
                    Toast.makeText(DashboardActivity.this, "Nenhuma Venda Efetuada", Toast.LENGTH_SHORT).show()
            );
            return new ResumoVendas(0, 0);
        }
    }

    private void atualizarResumo(ResumoVendas resumo) {

        double totalReceita = resumo.getTotal();
        int totalPedidos = resumo.getTotalOrders();

        txtTotalVendas.setText(currencyFormat.format(totalReceita));
        txtTotalPedidos.setText(
                totalPedidos + (totalPedidos == 1 ? " pedido" : " pedidos")
        );

        chartVendas.setData(criarPlaceholderValores(7));
        chartPedidos.setData(criarPlaceholderValores(7));
    }

    private String extrairDataSimples(String dataIso) {
        if (dataIso == null || dataIso.isEmpty()) return null;
        if (dataIso.length() >= 10) {
            return dataIso.substring(0, 10);
        }
        return dataIso;
    }

    private List<Float> extrairUltimosValores(Map<String, Float> mapa, int limite) {
        if (mapa == null || mapa.isEmpty()) {
            return criarPlaceholderValores(limite);
        }

        List<Float> valores = new ArrayList<>(mapa.values());
        if (valores.size() > limite) {
            valores = valores.subList(valores.size() - limite, valores.size());
        }
        return valores;
    }

    private List<Float> criarPlaceholderValores(int quantidade) {
        List<Float> placeholder = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            placeholder.add(0f);
        }
        return placeholder;
    }
}
