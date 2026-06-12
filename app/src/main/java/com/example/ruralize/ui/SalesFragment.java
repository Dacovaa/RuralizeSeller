package com.example.ruralize.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ruralize.R;
import com.example.ruralize.ResumoVendas;
import com.example.ruralize.network.RetrofitClient;
import com.example.ruralize.network.services.SalesService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalesFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtSemVendas;
    private ProgressBar progressGlobal;
    private TextView txtResumoTotalPedidos;
    private TextView txtResumoUnidadesVendidas;
    private TextView txtResumoReceitaTotal;
    private RecyclerView recyclerView;

    private FirebaseAuth mAuth;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public SalesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        inicializarComponentes(view);
        configurarAcoes();
        carregarResumo();
    }

    private void inicializarComponentes(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshVendas);
        txtSemVendas = view.findViewById(R.id.txtSemVendas);
        progressGlobal = view.findViewById(R.id.progressCarregandoVendas);
        txtResumoTotalPedidos = view.findViewById(R.id.txtTotalPedidos);
        txtResumoUnidadesVendidas = view.findViewById(R.id.txtUnidadesVendidas);
        txtResumoReceitaTotal = view.findViewById(R.id.txtReceitaTotal);
        recyclerView = view.findViewById(R.id.recyclerResumoVendas);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void configurarAcoes() {
        swipeRefreshLayout.setOnRefreshListener(this::carregarResumo);
    }

    private void carregarResumo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            }
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        exibirLoading(true);

        String uid = currentUser.getUid();
        SalesService salesService = RetrofitClient.getClient().create(SalesService.class);
        salesService.getSalesSummary(uid).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Response<okhttp3.ResponseBody> response) {
                if (!isAdded()) return;
                exibirLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
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
                        android.util.Log.e("Sales", "Erro ao processar resumo de vendas", e);
                        Toast.makeText(requireContext(), "Erro de processamento", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                exibirLoading(false);
                String errorMsg = t.getMessage() != null ? t.getMessage() : "Erro desconhecido";
                Toast.makeText(requireContext(), "Erro ao carregar resumo: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarResumo(ResumoVendas resumo) {
        if (!isAdded()) return;
        
        if (resumo == null) {
            mostrarResumoZerado();
            return;
        }

        double total = resumo.getTotal();
        int totalOrders = resumo.getTotalOrders();
        int orderProductQuantity = resumo.getOrderProductQuantity();

        txtResumoReceitaTotal.setText(currencyFormat.format(total));
        txtResumoTotalPedidos.setText(String.valueOf(totalOrders));
        txtResumoUnidadesVendidas.setText(String.valueOf(orderProductQuantity));

        boolean semVendas = totalOrders == 0;
        txtSemVendas.setVisibility(semVendas ? View.VISIBLE : View.GONE);
    }

    private void mostrarResumoZerado() {
        if (!isAdded()) return;
        txtResumoReceitaTotal.setText("R$ 0,00");
        txtResumoTotalPedidos.setText("0");
        txtResumoUnidadesVendidas.setText("0");
        txtSemVendas.setVisibility(View.VISIBLE);
    }

    private void exibirLoading(boolean exibindo) {
        if (!isAdded()) return;
        progressGlobal.setVisibility(exibindo ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }
}
