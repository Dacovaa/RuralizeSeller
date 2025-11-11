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

public class EntregasActivity extends BaseDrawerActivity implements EntregaAdapter.OnEntregaClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerEntregas;
    private TextView txtSemEntregas;
    private ProgressBar progressGlobal;
    private TextView txtTotalPendentes;
    private TextView txtTotalEmRota;
    private TextView txtTotalEntregues;

    private final OkHttpClient client = new OkHttpClient();
    private FirebaseAuth firebaseAuth;
    private EntregaAdapter entregaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_entregas);
        setToolbarTitle("Entregas");

        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        inicializarComponentes();
        configurarRecycler();
        configurarAcoes();
        carregarEntregas();
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_entregas;
    }

    private void inicializarComponentes() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshEntregas);
        recyclerEntregas = findViewById(R.id.recyclerEntregas);
        txtSemEntregas = findViewById(R.id.txtSemEntregas);
        progressGlobal = findViewById(R.id.progressCarregandoEntregas);
        txtTotalPendentes = findViewById(R.id.txtTotalPendentes);
        txtTotalEmRota = findViewById(R.id.txtTotalEmRota);
        txtTotalEntregues = findViewById(R.id.txtTotalEntregues);
    }

    private void configurarRecycler() {
        entregaAdapter = new EntregaAdapter(this);
        recyclerEntregas.setLayoutManager(new LinearLayoutManager(this));
        recyclerEntregas.setAdapter(entregaAdapter);
    }

    private void configurarAcoes() {
        swipeRefreshLayout.setOnRefreshListener(this::carregarEntregas);
    }

    private void carregarEntregas() {
        FirebaseUser usuario = firebaseAuth.getCurrentUser();
        if (usuario == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        exibirLoading(true);

        String url = ApiConfig.deliveriesByUser(usuario.getUid()); // TODO: ajuste conforme a estrutura da nova API

        Request request = new Request.Builder()
                .url(url)
                // TODO: incluir cabeçalhos extras aqui caso a nova API exija autenticação adicional
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    exibirLoading(false);
                    Toast.makeText(EntregasActivity.this, "Erro ao carregar entregas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        exibirLoading(false);
                        Toast.makeText(EntregasActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String responseBody = response.body().string();
                List<Entrega> entregas = interpretarEntregas(responseBody);

                runOnUiThread(() -> {
                    atualizarInterface(entregas);
                    exibirLoading(false);
                });
            }
        });
    }

    private List<Entrega> interpretarEntregas(String responseBody) {
        List<Entrega> entregas = new ArrayList<>();
        if (responseBody == null || responseBody.isEmpty()) {
            return entregas;
        }

        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                Entrega entrega = new Entrega(
                        json.optString("id"),
                        json.optString("pedidoId"),
                        json.optString("clienteNome", json.optString("cliente")),
                        json.optString("status"),
                        json.optString("dataEntrega", json.optString("data")),
                        json.optString("endereco"),
                        json.optDouble("valorTotal"),
                        json.optString("observacao")
                );
                entregas.add(entrega);
            }
        } catch (JSONException e) {
            runOnUiThread(() -> Toast.makeText(EntregasActivity.this, "Erro ao interpretar entregas", Toast.LENGTH_SHORT).show());
        }

        return entregas;
    }

    private void atualizarInterface(List<Entrega> entregas) {
        entregaAdapter.atualizarLista(entregas);
        atualizarResumo(entregas);

        boolean vazio = entregas == null || entregas.isEmpty();
        txtSemEntregas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        recyclerEntregas.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private void atualizarResumo(List<Entrega> entregas) {
        if (entregas == null) {
            entregas = Collections.emptyList();
        }

        Map<String, Integer> contador = new HashMap<>();
        for (Entrega entrega : entregas) {
            if (entrega == null) continue;
            String status = entrega.getStatus() != null ? entrega.getStatus().toLowerCase(Locale.ROOT) : "pendente";
            contador.put(status, contador.getOrDefault(status, 0) + 1);
        }

        txtTotalPendentes.setText(String.valueOf(contador.getOrDefault("pendente", 0) + contador.getOrDefault("aguardando", 0)));
        txtTotalEmRota.setText(String.valueOf(contador.getOrDefault("em rota", 0) + contador.getOrDefault("em trânsito", 0)));
        txtTotalEntregues.setText(String.valueOf(contador.getOrDefault("entregue", 0)));
    }

    private void exibirLoading(boolean exibindo) {
        progressGlobal.setVisibility(exibindo ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onEntregaSelecionada(Entrega entrega) {
        if (entrega == null) return;
        String mensagem = entrega.getObservacao() != null && !entrega.getObservacao().isEmpty()
                ? entrega.getObservacao()
                : "Entrega " + entrega.getStatus();
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}

