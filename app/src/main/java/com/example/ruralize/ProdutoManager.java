package com.example.ruralize;

import java.util.ArrayList;
import java.util.List;

public class ProdutoManager {
    private static ProdutoManager instance;
    private List<Produto> produtos;

    private ProdutoManager() {
        produtos = new ArrayList<>();
    }

    public static ProdutoManager getInstance() {
        if (instance == null) {
            instance = new ProdutoManager();
        }
        return instance;
    }

    public void adicionarProduto(Produto produto) {
        // Garantir que o produto tenha um ID
        if (produto.getId() == null) {
            produto.setId(String.valueOf(System.currentTimeMillis()));
        }
        produtos.add(produto);
    }

    public void removerProduto(Produto produto) {
        produtos.remove(produto);
    }

    public void removerProdutoPorId(String id) {
        for (int i = 0; i < produtos.size(); i++) {
            if (produtos.get(i).getId().equals(id)) {
                produtos.remove(i);
                break;
            }
        }
    }

    public void atualizarProduto(Produto produtoAtualizado) {
        for (int i = 0; i < produtos.size(); i++) {
            // Verificar se o ID existe antes de comparar
            if (produtos.get(i).getId() != null &&
                    produtos.get(i).getId().equals(produtoAtualizado.getId())) {
                produtos.set(i, produtoAtualizado);
                break;
            }
        }
    }

    public List<Produto> getProdutos() {
        return new ArrayList<>(produtos);
    }

    public Produto getProdutoPorId(String id) {
        for (Produto produto : produtos) {
            if (produto.getId() != null && produto.getId().equals(id)) {
                return produto;
            }
        }
        return null;
    }

    public Produto getProdutoPorTitulo(String titulo) {
        for (Produto produto : produtos) {
            if (produto.getTitulo().equals(titulo)) {
                return produto;
            }
        }
        return null;
    }
}