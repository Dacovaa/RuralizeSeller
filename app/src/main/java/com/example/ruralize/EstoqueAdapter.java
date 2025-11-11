package com.example.ruralize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EstoqueAdapter extends RecyclerView.Adapter<EstoqueAdapter.EstoqueViewHolder> {

    public interface OnEstoqueListener {
        void onSalvarEstoque(Produto produto, int novoEstoque);
    }

    private final List<Produto> produtos = new ArrayList<>();
    private final Map<String, Integer> estoqueEditado = new HashMap<>();
    private final Set<String> itensCarregando = new HashSet<>();
    private final OnEstoqueListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public EstoqueAdapter(List<Produto> produtosIniciais, OnEstoqueListener listener) {
        this.listener = listener;
        atualizarLista(produtosIniciais);
    }

    @NonNull
    @Override
    public EstoqueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_estoque_produto, parent, false);
        return new EstoqueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EstoqueViewHolder holder, int position) {
        Produto produto = produtos.get(position);
        holder.bind(produto);
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public void atualizarLista(List<Produto> novosProdutos) {
        produtos.clear();
        estoqueEditado.clear();

        if (novosProdutos != null) {
            produtos.addAll(novosProdutos);
            for (Produto produto : novosProdutos) {
                if (produto != null && produto.getId() != null) {
                    estoqueEditado.put(produto.getId(), produto.getEstoque());
                }
            }
        }

        notifyDataSetChanged();
    }

    public List<Produto> getProdutos() {
        return new ArrayList<>(produtos);
    }

    public void setItemCarregando(String produtoId, boolean carregando) {
        if (produtoId == null) return;
        if (carregando) {
            itensCarregando.add(produtoId);
        } else {
            itensCarregando.remove(produtoId);
        }
        int index = encontrarPosicao(produtoId);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    public void confirmarAtualizacao(String produtoId, int novoEstoque) {
        if (produtoId == null) return;

        estoqueEditado.put(produtoId, novoEstoque);

        int index = encontrarPosicao(produtoId);
        if (index >= 0) {
            Produto produto = produtos.get(index);
            produto.setEstoque(novoEstoque);
            notifyItemChanged(index);
        }
    }

    private int encontrarPosicao(String produtoId) {
        if (produtoId == null) return -1;
        for (int i = 0; i < produtos.size(); i++) {
            Produto produto = produtos.get(i);
            if (produto != null && produtoId.equals(produto.getId())) {
                return i;
            }
        }
        return -1;
    }

    class EstoqueViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtTitulo;
        private final TextView txtCategoria;
        private final TextView txtPreco;
        private final TextView txtEstoqueAtual;
        private final TextView txtAlteracao;
        private final Button btnDiminuir;
        private final Button btnAumentar;
        private final Button btnSalvar;
        private final ProgressBar progressSalvar;

        EstoqueViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloEstoque);
            txtCategoria = itemView.findViewById(R.id.txtCategoriaEstoque);
            txtPreco = itemView.findViewById(R.id.txtPrecoEstoque);
            txtEstoqueAtual = itemView.findViewById(R.id.txtEstoqueAtual);
            txtAlteracao = itemView.findViewById(R.id.txtAlteracaoEstoque);
            btnDiminuir = itemView.findViewById(R.id.btnDiminuirEstoque);
            btnAumentar = itemView.findViewById(R.id.btnAumentarEstoque);
            btnSalvar = itemView.findViewById(R.id.btnSalvarEstoque);
            progressSalvar = itemView.findViewById(R.id.progressSalvarEstoque);
        }

        void bind(Produto produto) {
            if (produto == null) return;

            String produtoId = produto.getId();
            int estoqueInicial = produto.getEstoque();
            Integer estoqueMapeado = estoqueEditado.get(produtoId);
            int estoqueAtual = estoqueMapeado != null ? estoqueMapeado : estoqueInicial;

            txtTitulo.setText(produto.getTitulo() != null ? produto.getTitulo() : "--");
            txtCategoria.setText(produto.getCategoria() != null ? produto.getCategoria() : "Sem categoria");
            txtPreco.setText(currencyFormat.format(produto.getPreco()));
            txtEstoqueAtual.setText(String.valueOf(estoqueAtual));

            int diferenca = estoqueAtual - estoqueInicial;
            if (diferenca == 0) {
                txtAlteracao.setVisibility(View.GONE);
                btnSalvar.setEnabled(false);
            } else {
                txtAlteracao.setVisibility(View.VISIBLE);
                String prefixo = diferenca > 0 ? "+" : "";
                txtAlteracao.setText("Alteração: " + prefixo + diferenca + " un.");
                btnSalvar.setEnabled(true);
            }

            boolean carregando = itensCarregando.contains(produtoId);
            progressSalvar.setVisibility(carregando ? View.VISIBLE : View.GONE);
            btnSalvar.setVisibility(carregando ? View.INVISIBLE : View.VISIBLE);
            btnDiminuir.setEnabled(!carregando);
            btnAumentar.setEnabled(!carregando);

            btnDiminuir.setOnClickListener(v -> {
                Integer valorAtual = estoqueEditado.get(produtoId);
                if (valorAtual == null) {
                    valorAtual = estoqueInicial;
                }
                if (valorAtual > 0) {
                    estoqueEditado.put(produtoId, valorAtual - 1);
                    notifyItemChanged(getBindingAdapterPosition());
                }
            });

            btnAumentar.setOnClickListener(v -> {
                Integer valorAtual = estoqueEditado.get(produtoId);
                if (valorAtual == null) {
                    valorAtual = estoqueInicial;
                }
                estoqueEditado.put(produtoId, valorAtual + 1);
                notifyItemChanged(getBindingAdapterPosition());
            });

            btnSalvar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSalvarEstoque(produto, estoqueEditado.get(produtoId));
                }
            });
        }
    }
}

