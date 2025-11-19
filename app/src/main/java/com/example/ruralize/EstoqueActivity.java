package com.example.ruralize;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EstoqueActivity extends BaseDrawerActivity implements EstoqueAdapter.OnEstoqueListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View blocoResumo;
    private TextView txtSemProdutos;
    private TextView txtTotalProdutos;
    private TextView txtTotalUnidades;
    private TextView txtProdutosBaixoEstoque;
    private ProgressBar progressGlobal;
    private EstoqueAdapter estoqueAdapter;

    private final OkHttpClient client = new OkHttpClient();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_estoque);
        setToolbarTitle("Controle de Estoque");

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        inicializarComponentes();
        configurarRecyclerView();
        configurarAcoes();
        carregarProdutos();
    }

    private void inicializarComponentes() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshEstoque);
        recyclerView = findViewById(R.id.recyclerEstoque);
        blocoResumo = findViewById(R.id.containerResumoEstoque);
        txtSemProdutos = findViewById(R.id.txtSemProdutosEstoque);
        txtTotalProdutos = findViewById(R.id.txtTotalProdutos);
        txtTotalUnidades = findViewById(R.id.txtTotalUnidades);
        txtProdutosBaixoEstoque = findViewById(R.id.txtProdutosBaixoEstoque);
        progressGlobal = findViewById(R.id.progressCarregandoEstoque);
    }

    private void configurarRecyclerView() {
        estoqueAdapter = new EstoqueAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(estoqueAdapter);
    }

    private void configurarAcoes() {
        swipeRefreshLayout.setOnRefreshListener(this::carregarProdutos);
    }

    private void carregarProdutos() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        exibirLoading(true);

        String uid = currentUser.getUid();
        String url = ApiConfig.productsByUser(uid); // TODO: ajustar para o endpoint da nova API

        Request request = new Request.Builder()
                .url(url)
                // TODO: incluir cabeçalhos adicionais (ex.: Authorization) se a nova API exigir
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    exibirLoading(false);
                    Toast.makeText(EstoqueActivity.this, "Erro ao carregar estoque: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        exibirLoading(false);
                        Toast.makeText(EstoqueActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String responseBody = response.body().string();
                List<Produto> produtos = new ArrayList<>();

                try {
                    JSONArray jsonArray = new JSONArray(responseBody);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonProduto = jsonArray.getJSONObject(i);
                        Produto produto = new Produto(
                                jsonProduto.optString("id"),
                                jsonProduto.optString("titulo"),
                                jsonProduto.optString("descricao"),
                                jsonProduto.optDouble("preco"),
                                jsonProduto.optInt("estoque"),
                                jsonProduto.optString("categoria"),
                                (List<String>) jsonProduto.getJSONArray("fotos")
                        );
                        produtos.add(produto);
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(EstoqueActivity.this, "Erro ao interpretar dados", Toast.LENGTH_SHORT).show());
                }

                runOnUiThread(() -> {
                    atualizarInterfaceComProdutos(produtos);
                    exibirLoading(false);
                });
            }
        });
    }

    private void exibirLoading(boolean exibindo) {
        if (exibindo) {
            progressGlobal.setVisibility(View.VISIBLE);
        } else {
            progressGlobal.setVisibility(View.GONE);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void atualizarInterfaceComProdutos(List<Produto> produtos) {
        estoqueAdapter.atualizarLista(produtos);
        atualizarResumo(produtos);

        if (produtos == null || produtos.isEmpty()) {
            txtSemProdutos.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            blocoResumo.setVisibility(View.GONE);
        } else {
            txtSemProdutos.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            blocoResumo.setVisibility(View.VISIBLE);
        }
    }

    private void atualizarResumo(List<Produto> produtos) {
        if (produtos == null) {
            produtos = Collections.emptyList();
        }

        int totalProdutos = produtos.size();
        int totalUnidades = 0;
        int baixoEstoque = 0;

        for (Produto produto : produtos) {
            int estoque = produto.getEstoque();
            totalUnidades += estoque;
            if (estoque <= 5) {
                baixoEstoque++;
            }
        }

        txtTotalProdutos.setText(String.valueOf(totalProdutos));
        txtTotalUnidades.setText(String.valueOf(totalUnidades));
        txtProdutosBaixoEstoque.setText(String.valueOf(baixoEstoque));
    }

    @Override
    public void onSalvarEstoque(Produto produto, int novoEstoque) {
        if (produto == null || produto.getId() == null) {
            Toast.makeText(this, "Produto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (novoEstoque < 0) {
            Toast.makeText(this, "O estoque não pode ser negativo", Toast.LENGTH_SHORT).show();
            return;
        }

        estoqueAdapter.setItemCarregando(produto.getId(), true);
        executarAtualizacaoEstoque(produto, novoEstoque);
    }

    private void executarAtualizacaoEstoque(Produto produto, int novoEstoque) {
        JSONObject jsonBody = new JSONObject();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        String uid = currentUser.getUid();
        try {
            jsonBody.put("estoque", novoEstoque);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao preparar atualização", Toast.LENGTH_SHORT).show();
            estoqueAdapter.setItemCarregando(produto.getId(), false);
            return;
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.productUpdate(uid, produto.getId()))
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    estoqueAdapter.setItemCarregando(produto.getId(), false);
                    Toast.makeText(EstoqueActivity.this, "Erro ao atualizar estoque: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    estoqueAdapter.setItemCarregando(produto.getId(), false);

                    if (response.isSuccessful()) {
                        produto.setEstoque(novoEstoque);
                        estoqueAdapter.confirmarAtualizacao(produto.getId(), novoEstoque);
                        atualizarResumo(estoqueAdapter.getProdutos());
                        Toast.makeText(EstoqueActivity.this, "Estoque atualizado!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EstoqueActivity.this, "Falha na atualização: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_estoque;
    }
}



