package seller.controller;

import android.util.Log;

import seller.model.ProdutoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================
 *  Classe: ProdutoController
 *  Camada: CONTROLLER
 *  Função: Controla a lógica de negócio relacionada aos produtos.
 * =========================================================
 */

public class ProdutoController {

    // ===== Lista simulando um banco de dados local (prototipagem) =====
    private final List<ProdutoModel> listaProdutos = new ArrayList<>();

    /**
     * Adiciona um novo produto à lista.
     * @param produto objeto ProdutoModel a ser adicionado
     */
    public void adicionarProduto(ProdutoModel produto) {
        if (produto != null && produto.getNome() != null && !produto.getNome().isEmpty()) {
            listaProdutos.add(produto);
            Log.i("ProdutoController", "Produto adicionado: " + produto.getNome());
        } else {
            Log.e("ProdutoController", "Erro: produto inválido ao adicionar");
        }
    }

    /**
     * Retorna todos os produtos cadastrados.
     * @return lista de produtos
     */
    public List<ProdutoModel> listarProdutos() {
        return new ArrayList<>(listaProdutos);
    }

    /**
     * Atualiza as informações de um produto existente.
     * @param id identificador do produto a ser atualizado
     * @param novoProduto dados atualizados
     */
    public void atualizarProduto(int id, ProdutoModel novoProduto) {
        for (int i = 0; i < listaProdutos.size(); i++) {
            ProdutoModel existente = listaProdutos.get(i);
            if (existente.getId() == id) {
                listaProdutos.set(i, novoProduto);
                Log.i("ProdutoController", "Produto atualizado: " + novoProduto.getNome());
                return;
            }
        }
        Log.e("ProdutoController", "Produto com ID " + id + " não encontrado.");
    }

    /**
     * Remove um produto da lista.
     * @param id identificador do produto a ser removido
     */
    public void removerProduto(int id) {
        listaProdutos.removeIf(produto -> produto.getId() == id);
        Log.i("ProdutoController", "Produto removido com ID: " + id);
    }

    /**
     * Busca um produto pelo ID.
     * @param id identificador do produto
     * @return ProdutoModel encontrado ou null
     */
    public ProdutoModel buscarProdutoPorId(int id) {
        for (ProdutoModel produto : listaProdutos) {
            if (produto.getId() == id) {
                return produto;
            }
        }
        return null;
    }

    /**
     * Busca produtos por nome (case insensitive).
     * @param nome nome ou parte do nome do produto
     * @return lista de produtos que contêm o termo no nome
     */
    public List<ProdutoModel> buscarProdutosPorNome(String nome) {
        List<ProdutoModel> encontrados = new ArrayList<>();
        for (ProdutoModel produto : listaProdutos) {
            if (produto.getNome().toLowerCase().contains(nome.toLowerCase())) {
                encontrados.add(produto);
            }
        }
        return encontrados;
    }
}
