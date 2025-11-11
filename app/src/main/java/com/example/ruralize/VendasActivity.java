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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VendasActivity extends BaseDrawerActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerVendas;
    private TextView txtSemVendas;
    private ProgressBar progressGlobal;
    private TextView txtResumoTotalPedidos;
    private TextView txtResumoUnidadesVendidas;
    private TextView txtResumoReceitaTotal;

    private final OkHttpClient client = new OkHttpClient();
    private FirebaseAuth mAuth;
    private VendaResumoAdapter vendaResumoAdapter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_vendas);
        setToolbarTitle("Balanço de Vendas");

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        inicializarComponentes();
        configurarRecycler();
        configurarAcoes();
        carregarVendas();
    }

    private void inicializarComponentes() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshVendas);
        recyclerVendas = findViewById(R.id.recyclerResumoVendas);
        txtSemVendas = findViewById(R.id.txtSemVendas);
        progressGlobal = findViewById(R.id.progressCarregandoVendas);
        txtResumoTotalPedidos = findViewById(R.id.txtTotalPedidos);
        txtResumoUnidadesVendidas = findViewById(R.id.txtUnidadesVendidas);
        txtResumoReceitaTotal = findViewById(R.id.txtReceitaTotal);
    }

    private void configurarRecycler() {
        vendaResumoAdapter = new VendaResumoAdapter();
        recyclerVendas.setLayoutManager(new LinearLayoutManager(this));
        recyclerVendas.setAdapter(vendaResumoAdapter);
    }

    private void configurarAcoes() {
        swipeRefreshLayout.setOnRefreshListener(this::carregarVendas);
    }

    private void carregarVendas() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        exibirLoading(true);

        String uid = currentUser.getUid();
        String url = ApiConfig.salesByUser(uid); // TODO: ajustar conforme as rotas da nova API

        Request request = new Request.Builder()
                .url(url)
                // TODO: adicionar cabeçalhos (ex.: Authorization) se necessários na nova API
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    exibirLoading(false);
                    Toast.makeText(VendasActivity.this, "Erro ao carregar vendas: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                List<Venda> vendas = interpretarVendas(responseBody);
                List<VendaResumo> balanco = gerarBalancoPorProduto(vendas);

                runOnUiThread(() -> {
                    atualizarInterface(vendas, balanco);
                    exibirLoading(false);
                });
            }
        });
    }

    private List<Venda> interpretarVendas(String responseBody) {
        List<Venda> vendas = new ArrayList<>();
        if (responseBody == null || responseBody.isEmpty()) {
            return vendas;
        }

        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVenda = jsonArray.getJSONObject(i);
                Venda venda = new Venda(
                        jsonVenda.optString("id"),
                        jsonVenda.optString("produtoId"),
                        jsonVenda.optString("produtoTitulo", jsonVenda.optString("produtoNome")),
                        jsonVenda.optInt("quantidade"),
                        jsonVenda.optDouble("valorTotal"),
                        jsonVenda.optDouble("precoUnitario"),
                        jsonVenda.optString("data")
                );
                vendas.add(venda);
            }
        } catch (JSONException e) {
            runOnUiThread(() -> Toast.makeText(VendasActivity.this, "Erro ao interpretar dados de vendas", Toast.LENGTH_SHORT).show());
        }

        return vendas;
    }

    private List<VendaResumo> gerarBalancoPorProduto(List<Venda> vendas) {
        if (vendas == null) {
            return Collections.emptyList();
        }

        Map<String, VendaResumo> mapa = new HashMap<>();

        for (Venda venda : vendas) {
            if (venda == null) continue;

            String produtoId = venda.getProdutoId() != null ? venda.getProdutoId() : venda.getProdutoTitulo();
            if (produtoId == null) {
                produtoId = "produto_" + mapa.size();
            }

            VendaResumo resumo = mapa.get(produtoId);
            if (resumo == null) {
                resumo = new VendaResumo(produtoId, venda.getProdutoTitulo());
                mapa.put(produtoId, resumo);
            }

            resumo.acumularVenda(venda);
        }

        return new ArrayList<>(mapa.values());
    }

    private void atualizarInterface(List<Venda> vendas, List<VendaResumo> balanco) {
        vendaResumoAdapter.atualizarItens(balanco);
        atualizarResumo(vendas);

        boolean vazio = balanco == null || balanco.isEmpty();
        txtSemVendas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        recyclerVendas.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private void atualizarResumo(List<Venda> vendas) {
        if (vendas == null) {
            vendas = Collections.emptyList();
        }

        int totalPedidos = vendas.size();
        int unidadesVendidas = 0;
        double receitaTotal = 0.0;

        for (Venda venda : vendas) {
            if (venda == null) continue;
            unidadesVendidas += venda.getQuantidade();
            receitaTotal += venda.getValorTotal();
        }

        txtResumoTotalPedidos.setText(String.valueOf(totalPedidos));
        txtResumoUnidadesVendidas.setText(String.valueOf(unidadesVendidas));
        txtResumoReceitaTotal.setText(currencyFormat.format(receitaTotal));
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



