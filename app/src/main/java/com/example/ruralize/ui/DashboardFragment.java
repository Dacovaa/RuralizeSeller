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

import com.example.ruralize.R;
import com.example.ruralize.ResumoVendas;
import com.example.ruralize.network.ApiConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private final OkHttpClient client = new OkHttpClient();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private TextView txtTotalVendas, txtTotalPedidos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        txtTotalVendas = view.findViewById(R.id.txtTotalVendas);
        txtTotalPedidos = view.findViewById(R.id.txtTotalPedidos);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        carregarResumoVendas();
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
        String url = ApiConfig.salesByUser(uid);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Erro ao carregar vendas: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), "Erro: " + response.code(), Toast.LENGTH_SHORT).show()
                        );
                    }
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                ResumoVendas resumo = interpretarVendas(responseBody);
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> atualizarResumo(resumo));
                }
            }
        });
    }

    private ResumoVendas interpretarVendas(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return new ResumoVendas(0, 0);
        }

        try {
            JSONObject json = new JSONObject(responseBody);
            double total = json.optDouble("total", 0);
            int totalOrders = json.optInt("totalOrders", 0);
            return new ResumoVendas(total, totalOrders);
        } catch (JSONException e) {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erro ao processar dados de vendas", Toast.LENGTH_SHORT).show()
                );
            }
            return new ResumoVendas(0, 0);
        }
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
}
