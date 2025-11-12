package com.example.ruralize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VendaResumoAdapter extends RecyclerView.Adapter<VendaResumoAdapter.VendaResumoViewHolder> {

    private final List<VendaResumo> itens = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
    private final SimpleDateFormat[] isoFormatters = new SimpleDateFormat[]{
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd", Locale.US)
    };

    public VendaResumoAdapter() {
        for (SimpleDateFormat isoFormatter : isoFormatters) {
            isoFormatter.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        }
        displayFormat.setTimeZone(java.util.TimeZone.getDefault());
    }

    @NonNull
    @Override
    public VendaResumoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venda_resumo, parent, false);
        return new VendaResumoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendaResumoViewHolder holder, int position) {
        holder.bind(itens.get(position));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public void atualizarItens(List<VendaResumo> novosItens) {
        itens.clear();
        if (novosItens != null) {
            itens.addAll(novosItens);
        }
        notifyDataSetChanged();
    }

    class VendaResumoViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtTituloProduto;
        private final TextView txtUnidadesVendidas;
        private final TextView txtReceitaGerada;
        private final TextView txtUltimaVenda;

        VendaResumoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTituloProduto = itemView.findViewById(R.id.txtTituloProdutoVenda);
            txtUnidadesVendidas = itemView.findViewById(R.id.txtQuantidadeVendida);
            txtReceitaGerada = itemView.findViewById(R.id.txtReceitaGerada);
            txtUltimaVenda = itemView.findViewById(R.id.txtUltimaVenda);
        }

        void bind(VendaResumo resumo) {
            txtTituloProduto.setText(resumo.getProdutoTitulo() != null ? resumo.getProdutoTitulo() : "Produto");
            txtUnidadesVendidas.setText(resumo.getUnidadesVendidas() + " un. vendidas");
            txtReceitaGerada.setText(currencyFormat.format(resumo.getReceitaGerada()));
            txtUltimaVenda.setText(formatarData(resumo.getDataUltimaVenda()));
        }

        private String formatarData(String dataIso) {
            if (dataIso == null || dataIso.isEmpty()) {
                return "Sem vendas registradas";
            }
            try {
                Date data = tentarParse(dataIso);
                if (data != null) {
                    return "Última venda em " + displayFormat.format(data);
                }
            } catch (Exception ignored) {
            }
            return "Última venda em " + dataIso;
        }

        private Date tentarParse(String dataIso) throws ParseException {
            for (SimpleDateFormat formatter : isoFormatters) {
                try {
                    return formatter.parse(dataIso);
                } catch (ParseException ignored) {
                }
            }
            throw new ParseException("Formato desconhecido", 0);
        }
    }
}

