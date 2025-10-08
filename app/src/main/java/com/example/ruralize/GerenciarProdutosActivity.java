package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GerenciarProdutosActivity extends ComponentActivity implements ProdutoAdapter.OnProdutoClickListener {

    private RecyclerView recyclerViewProdutos;
    private TextView txtSemProdutos;
    private ProdutoAdapter produtoAdapter;
    private ProdutoManager produtoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_produtos);

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
        // Se não há produtos, carregar alguns de exemplo
        if (produtoManager.getProdutos().isEmpty()) {
            carregarProdutosExemplo();
        }

        produtoAdapter.atualizarLista(produtoManager.getProdutos());
        verificarListaVazia();
    }

    private void carregarProdutosExemplo() {
        // Adicionar produtos de exemplo apenas uma vez
        produtoManager.adicionarProduto(new Produto(
                "Arroz Orgânico Premium",
                "Arroz integral orgânico cultivado sem agrotóxicos, colhido manualmente",
                25.90,
                150,
                "Grãos e Cereais"
        ));

        produtoManager.adicionarProduto(new Produto(
                "Mel Puro de Eucalipto",
                "Mel 100% natural das flores de eucalipto, processado artesanalmente",
                42.50,
                75,
                "Mel e Derivados"
        ));

        produtoManager.adicionarProduto(new Produto(
                "Ovos Caipira Frescos",
                "Ovos de galinhas criadas soltas com alimentação natural e sem hormônios",
                18.00,
                200,
                "Ovos"
        ));
        produtoManager.adicionarProduto(new Produto(
                "Ovos Caipira Frescos",
                "Ovos de galinhas criadas soltas com alimentação natural e sem hormônios",
                18.00,
                200,
                "Ovos"
        ));
        produtoManager.adicionarProduto(new Produto(
                "Ovos Caipira Frescos",
                "Ovos de galinhas criadas soltas com alimentação natural e sem hormônios",
                18.00,
                200,
                "Ovos"
        ));
        produtoManager.adicionarProduto(new Produto(
                "Ovos Caipira Frescos",
                "Ovos de galinhas criadas soltas com alimentação natural e sem hormônios",
                18.00,
                200,
                "Ovos"
        ));
        produtoManager.adicionarProduto(new Produto(
                "Ovos Caipira Frescos",
                "Ovos de galinhas criadas soltas com alimentação natural e sem hormônios",
                18.00,
                200,
                "Ovos"
        ));

        Toast.makeText(this, "Produtos de exemplo carregados", Toast.LENGTH_SHORT).show();
    }

    private void verificarListaVazia() {
        List<Produto> produtos = produtoManager.getProdutos();
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
        intent.putExtra("PRODUTO_ID", produto.getId());
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
                .setPositiveButton("Excluir", (dialog, which) -> {
                    produtoManager.removerProduto(produto);
                    produtoAdapter.atualizarLista(produtoManager.getProdutos());
                    verificarListaVazia();
                    Toast.makeText(this, "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar produtos quando retornar à tela
        carregarProdutos();
    }
}