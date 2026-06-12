package com.example.ruralize.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ruralize.Notificacao;
import com.example.ruralize.R;
import com.example.ruralize.ResumoVendas;
import com.example.ruralize.network.RetrofitClient;
import com.example.ruralize.network.services.NotificationService;
import com.example.ruralize.network.services.SalesService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private TextView txtTotalVendas, txtTotalPedidos, txtAtividadeRecente;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        txtTotalVendas = view.findViewById(R.id.txtTotalVendas);
        txtTotalPedidos = view.findViewById(R.id.txtTotalPedidos);
        txtAtividadeRecente = view.findViewById(R.id.txtAtividadeRecente);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        carregarResumoVendas();
        carregarNotificacoesRecentes();
    }

    private void carregarResumoVendas() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String uid = currentUser.getUid();
        SalesService salesService = RetrofitClient.getClient().create(SalesService.class);
        salesService.getSalesSummary(uid).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Response<okhttp3.ResponseBody> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            // Handle plain numbers or JSON
                            ResumoVendas resumo;
                            if (responseBody.trim().startsWith("{")) {
                                org.json.JSONObject json = new org.json.JSONObject(responseBody);
                                double total = json.optDouble("total", 0);
                                int totalOrders = json.optInt("totalOrders", 0);
                                int qty = json.optInt("orderProductQuantity", 0);
                                resumo = new ResumoVendas(total, totalOrders, qty);
                            } else {
                                double total = Double.parseDouble(responseBody.trim());
                                resumo = new ResumoVendas(total, 0, 0); // fallback
                            }
                            atualizarResumo(resumo);
                        } catch (Exception e) {
                            android.util.Log.e("Dashboard", "Erro ao processar resumo: " + e.getMessage(), e);
                            Toast.makeText(getContext(), "Erro de formatação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Erro ao carregar vendas: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                if (isAdded()) {
                    android.util.Log.e("Dashboard", "Erro de conexão", t);
                    String errorMsg = t.getMessage() != null ? t.getMessage() : "Causa desconhecida";
                    Toast.makeText(getContext(), "Erro de conexão: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void atualizarResumo(ResumoVendas resumo) {
        if (txtTotalVendas != null) {
            txtTotalVendas.setText(currencyFormat.format(resumo.getTotal()));
        }
        if (txtTotalPedidos != null) {
            int totalPedidos = resumo.getTotalOrders();
            txtTotalPedidos.setText(
                    totalPedidos + (totalPedidos == 1 ? " pedido" : " pedidos")
            );
        }
    }

    private void carregarNotificacoesRecentes() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        NotificationService notificationService = RetrofitClient.getClient().create(NotificationService.class);
        notificationService.getNotifications(uid).enqueue(new Callback<List<Notificacao>>() {
            @Override
            public void onResponse(@NonNull Call<List<Notificacao>> call, @NonNull Response<List<Notificacao>> response) {
                if (isAdded() && getContext() != null && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Notificacao last = response.body().get(0);
                    String title = last.getTitle();
                    String message = last.getMessage();

                    if (txtAtividadeRecente != null) {
                        txtAtividadeRecente.setText(title + ": " + message);
                        txtAtividadeRecente.setTextColor(requireContext().getColor(R.color.text_primary));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Notificacao>> call, @NonNull Throwable t) {
                android.util.Log.e("Dashboard", "Erro ao carregar notificações", t);
            }
        });
    }
}
