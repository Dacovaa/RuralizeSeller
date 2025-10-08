package com.example.ruralize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private List<Produto> produtos;
    private OnProdutoClickListener listener;

    public interface OnProdutoClickListener {
        void onEditarClick(Produto produto);
        void onExcluirClick(Produto produto);
    }

    public ProdutoAdapter(List<Produto> produtos, OnProdutoClickListener listener) {
        this.produtos = produtos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = produtos.get(position);
        holder.bind(produto);
    }

    @Override
    public int getItemCount() {
        return produtos != null ? produtos.size() : 0;
    }

    public void atualizarLista(List<Produto> novosProdutos) {
        this.produtos = novosProdutos;
        notifyDataSetChanged();
    }

    public class ProdutoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtDescricao, txtPreco, txtEstoque, txtCategoria;
        Button btnEditar, btnExcluir;

        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializar views
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtPreco = itemView.findViewById(R.id.txtPreco);
            txtEstoque = itemView.findViewById(R.id.txtEstoque);
            txtCategoria = itemView.findViewById(R.id.txtCategoria);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }

        public void bind(Produto produto) {
            if (produto == null) return;

            // Configurar dados do produto
            txtTitulo.setText(produto.getTitulo() != null ? produto.getTitulo() : "");
            txtDescricao.setText(produto.getDescricao() != null ? produto.getDescricao() : "");

            // Formatar preço
            NumberFormat formatador = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String precoFormatado = formatador.format(produto.getPreco());
            txtPreco.setText(precoFormatado);

            txtEstoque.setText("Estoque: " + produto.getEstoque() + " unidades");
            txtCategoria.setText(produto.getCategoria() != null ? produto.getCategoria() : "");

            // Configurar cliques dos botões
            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditarClick(produto);
                }
            });

            btnExcluir.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExcluirClick(produto);
                }
            });
        }
    }
}