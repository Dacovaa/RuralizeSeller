package com.example.ruralize.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ruralize.NovoProdutoActivity;
import com.example.ruralize.Produto;
import com.example.ruralize.ProdutoAdapter;
import com.example.ruralize.ProdutoManager;
import com.example.ruralize.R;
import com.example.ruralize.network.ApiConfig;
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

public class CatalogFragment extends Fragment implements ProdutoAdapter.OnProdutoClickListener {

    private RecyclerView recyclerViewProdutos;
    private TextView txtSemProdutos;
    private ProdutoAdapter produtoAdapter;
    private ProdutoManager produtoManager;
    private FirebaseAuth mAuth;

    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        inicializarComponentes(view);
        configurarRecyclerView();
        configurarCliques(view);
        carregarProdutos();
    }

    private void inicializarComponentes(View view) {
        recyclerViewProdutos = view.findViewById(R.id.recyclerViewProdutos);
        txtSemProdutos = view.findViewById(R.id.txtSemProdutos);
        produtoManager = ProdutoManager.getInstance();
    }

    private void configurarRecyclerView() {
        produtoAdapter = new ProdutoAdapter(produtoManager.getProdutos(), this);
        recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewProdutos.setAdapter(produtoAdapter);
    }

    private void configurarCliques(View view) {
        view.findViewById(R.id.btnNovoProduto).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NovoProdutoActivity.class);
            startActivity(intent);
        });
    }

    private void carregarProdutos() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String uid = currentUser.getUid();
        String url = ApiConfig.productsByUser(uid);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    List<Produto> produtos = new ArrayList<>();

                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonProduto = jsonArray.getJSONObject(i);
                            JSONArray fotosJson = jsonProduto.optJSONArray("fotos");
                            List<String> fotosList = new ArrayList<>();

                            if (fotosJson != null) {
                                for (int j = 0; j < fotosJson.length(); j++) {
                                    fotosList.add(fotosJson.getString(j));
                                }
                            }
                            Produto p = new Produto(
                                    jsonProduto.getString("id"),
                                    jsonProduto.getString("titulo"),
                                    jsonProduto.getString("descricao"),
                                    jsonProduto.getDouble("preco"),
                                    jsonProduto.getInt("estoque"),
                                    jsonProduto.getString("categoria"),
                                    fotosList
                            );
                            produtos.add(p);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            produtoAdapter.atualizarLista(produtos);
                            verificarListaVazia(produtos);
                        });
                    }
                } else {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
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
        Toast.makeText(requireContext(), "Editando: " + produto.getTitulo(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), NovoProdutoActivity.class);
        intent.putExtra("MODO_EDICAO", true);
        intent.putExtra("ID", produto.getId());
        intent.putExtra("TITULO", produto.getTitulo());
        intent.putExtra("DESCRICAO", produto.getDescricao());
        intent.putExtra("PRECO", produto.getPreco());
        intent.putExtra("ESTOQUE", produto.getEstoque());
        intent.putExtra("CATEGORIA", produto.getCategoria());
        intent.putStringArrayListExtra("FOTOSURL", (ArrayList<String>) produto.getFotosUrls());
        startActivity(intent);
    }

    @Override
    public void onExcluirClick(Produto produto) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o produto '" + produto.getTitulo() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirProduto(produto.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirProduto(String produtoId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String url = ApiConfig.productDelete(uid, produtoId);

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Erro ao excluir produto", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();
                            carregarProdutos();
                        } else {
                            Toast.makeText(requireContext(), "Erro ao excluir produto: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarProdutos();
    }
}
