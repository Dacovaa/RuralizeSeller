package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GerenciarProdutosActivity extends ComponentActivity implements ProdutoAdapter.OnProdutoClickListener {

    private RecyclerView recyclerViewProdutos;
    private TextView txtSemProdutos;
    private ProdutoAdapter produtoAdapter;
    private ProdutoManager produtoManager;
    private FirebaseAuth mAuth;

    private final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://ruralize-api.vercel.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_produtos);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        inicializarComponentes();
        configurarRecyclerView();
        configurarCliques();
        carregarProdutos();
    }

    private void inicializarComponentes() {
        recyclerViewProdutos = findViewById(R.id.recyclerViewProdutos);
        txtSemProdutos = findViewById(R.id.txtSemProdutos);
        produtoManager = ProdutoManager.getInstance();
    }

    private void configurarRecyclerView() {
        produtoAdapter = new ProdutoAdapter(produtoManager.getProdutos(), this);
        recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProdutos.setAdapter(produtoAdapter);
    }

    private void configurarCliques() {
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        findViewById(R.id.btnNovoProduto).setOnClickListener(v -> {
            Intent intent = new Intent(this, NovoProdutoActivity.class);
            startActivity(intent);
        });
    }

    private void carregarProdutos() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String url = BASE_URL + "/products/" + uid;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(GerenciarProdutosActivity.this, "Erro ao carregar produtos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    List<Produto> produtos = new ArrayList<>();

                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonProduto = jsonArray.getJSONObject(i);
                            Produto p = new Produto(
                                    jsonProduto.getString("id"),
                                    jsonProduto.getString("titulo"),
                                    jsonProduto.getString("descricao"),
                                    jsonProduto.getDouble("preco"),
                                    jsonProduto.getInt("estoque"),
                                    jsonProduto.getString("categoria")
                            );
                            produtos.add(p);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(() -> {
                        produtoAdapter.atualizarLista(produtos);
                        verificarListaVazia(produtos);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(GerenciarProdutosActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void verificarListaVazia(List<Produto> produtos) {
        if (produtos.isEmpty()) {
            txtSemProdutos.setVisibility(View.VISIBLE);
            recyclerViewProdutos.setVisibility(View.GONE);
        } else {
            txtSemProdutos.setVisibility(View.GONE);
            recyclerViewProdutos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditarClick(Produto produto) {
        Toast.makeText(this, "Editando: " + produto.getTitulo(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, NovoProdutoActivity.class);
        intent.putExtra("MODO_EDICAO", true);
        intent.putExtra("ID", produto.getId());
        intent.putExtra("TITULO", produto.getTitulo());
        intent.putExtra("DESCRICAO", produto.getDescricao());
        intent.putExtra("PRECO", produto.getPreco());
        intent.putExtra("ESTOQUE", produto.getEstoque());
        intent.putExtra("CATEGORIA", produto.getCategoria());
        startActivity(intent);
    }

    @Override
    public void onExcluirClick(Produto produto) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o produto '" + produto.getTitulo() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirProduto(produto.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirProduto(String produtoId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String url = BASE_URL + "/products/" + uid + "/" + produtoId;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(GerenciarProdutosActivity.this, "Erro ao excluir produto", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(GerenciarProdutosActivity.this, "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();
                        carregarProdutos();
                    } else {
                        Toast.makeText(GerenciarProdutosActivity.this, "Erro ao excluir produto: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarProdutos();
    }
}