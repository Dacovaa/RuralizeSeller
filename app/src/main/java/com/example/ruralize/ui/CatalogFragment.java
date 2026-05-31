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
import com.example.ruralize.network.RetrofitClient;
import com.example.ruralize.network.services.ProductService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogFragment extends Fragment implements ProdutoAdapter.OnProdutoClickListener {

    private RecyclerView recyclerViewProdutos;
    private TextView txtSemProdutos;
    private ProdutoAdapter produtoAdapter;
    private ProdutoManager produtoManager;
    private FirebaseAuth mAuth;

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
        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        productService.getProducts(uid).enqueue(new Callback<List<Produto>>() {
            @Override
            public void onResponse(@NonNull Call<List<Produto>> call, @NonNull Response<List<Produto>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Produto> produtos = response.body();
                        produtoAdapter.atualizarLista(produtos);
                        verificarListaVazia(produtos);
                    } else {
                        Toast.makeText(requireContext(), "Erro ao carregar produtos: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Produto>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        productService.deleteProduct(uid, produtoId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();
                        carregarProdutos();
                    } else {
                        Toast.makeText(requireContext(), "Erro ao excluir produto: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Erro de conexão ao excluir produto", Toast.LENGTH_SHORT).show();
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
