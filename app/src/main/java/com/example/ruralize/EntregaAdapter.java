package com.example.ruralize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntregaAdapter extends RecyclerView.Adapter<EntregaAdapter.EntregaViewHolder> {

    public interface OnEntregaClickListener {
        void onEntregaSelecionada(Entrega entrega);
    }

    private final List<Entrega> entregas = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final OnEntregaClickListener listener;

    public EntregaAdapter(OnEntregaClickListener listener) {
        this.listener = listener;
    }

    public void atualizarLista(List<Entrega> novasEntregas) {
        entregas.clear();
        if (novasEntregas != null) {
            entregas.addAll(novasEntregas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntregaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrega, parent, false);
        return new EntregaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntregaViewHolder holder, int position) {
        holder.bind(entregas.get(position));
    }

    @Override
    public int getItemCount() {
        return entregas.size();
    }

    class EntregaViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtCliente;
        private final TextView txtStatus;
        private final TextView txtValor;
        private final TextView txtEndereco;
        private final TextView txtData;
        private final View statusIndicator;

        EntregaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCliente = itemView.findViewById(R.id.txtClienteEntrega);
            txtStatus = itemView.findViewById(R.id.txtStatusEntrega);
            txtValor = itemView.findViewById(R.id.txtValorEntrega);
            txtEndereco = itemView.findViewById(R.id.txtEnderecoEntrega);
            txtData = itemView.findViewById(R.id.txtDataEntrega);
            statusIndicator = itemView.findViewById(R.id.viewIndicadorStatus);
        }

        void bind(Entrega entrega) {
            if (entrega == null) return;

            txtCliente.setText(entrega.getClienteNome() != null ? entrega.getClienteNome() : "Cliente");
            txtStatus.setText(entrega.getStatus() != null ? entrega.getStatus() : "Indefinido");
            txtValor.setText(currencyFormat.format(entrega.getValorTotal()));
            txtEndereco.setText(entrega.getEndereco() != null ? entrega.getEndereco() : "Sem endereço");
            txtData.setText(entrega.getDataEntrega() != null ? entrega.getDataEntrega() : "--/--");

            statusIndicator.setBackgroundResource(obterCorStatus(entrega.getStatus()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEntregaSelecionada(entrega);
                }
            });
        }

        private int obterCorStatus(String status) {
            if (status == null) return R.color.green_light;
            switch (status.toLowerCase(Locale.ROOT)) {
                case "entregue":
                    return R.color.green_mid;
                case "em rota":
                case "em trânsito":
                    return R.color.green_light;
                case "pendente":
                case "aguardando":
                    return R.color.text_hint;
                case "cancelada":
                    return R.color.error_red;
                default:
                    return R.color.green_light;
            }
        }
    }
}

