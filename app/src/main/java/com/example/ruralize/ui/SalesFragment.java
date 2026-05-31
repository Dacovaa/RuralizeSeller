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
import com.example.ruralize.network.ApiConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SalesFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtSemVendas;
    private ProgressBar progressGlobal;
    private TextView txtResumoTotalPedidos;
    private TextView txtResumoUnidadesVendidas;
    private TextView txtResumoReceitaTotal;
    private RecyclerView recyclerView;

    private final OkHttpClient client = new OkHttpClient();
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
        String url = ApiConfig.salesByUser(uid);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    exibirLoading(false);
                    Toast.makeText(requireContext(), "Erro ao carregar resumo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!isAdded()) return;
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        exibirLoading(false);
                        Toast.makeText(requireContext(), "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String responseBody = response.body().string();
                requireActivity().runOnUiThread(() -> atualizarResumo(responseBody));
            }
        });
    }

    private void atualizarResumo(String json) {
        if (!isAdded()) return;
        
        if (json == null || json.isEmpty()) {
            mostrarResumoZerado();
            return;
        }

        try {
            JSONObject obj = new JSONObject(json);

            double total = obj.optDouble("total", 0.0);
            int totalOrders = obj.optInt("totalOrders", 0);
            int orderProductQuantity = obj.optInt("orderProductQuantity", 0);

            txtResumoReceitaTotal.setText(currencyFormat.format(total));
            txtResumoTotalPedidos.setText(String.valueOf(totalOrders));
            txtResumoUnidadesVendidas.setText(String.valueOf(orderProductQuantity));

            boolean semVendas = totalOrders == 0;
            txtSemVendas.setVisibility(semVendas ? View.VISIBLE : View.GONE);

        } catch (JSONException e) {
            Toast.makeText(requireContext(), "Nenhuma Venda Efetuada", Toast.LENGTH_SHORT).show();
            mostrarResumoZerado();
        }

        exibirLoading(false);
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
