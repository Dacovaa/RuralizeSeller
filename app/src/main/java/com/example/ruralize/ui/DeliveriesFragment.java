package com.example.ruralize.ui;

import android.app.AlertDialog;
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

import com.example.ruralize.Entrega;
import com.example.ruralize.EntregaAdapter;
import com.example.ruralize.R;
import com.example.ruralize.network.RetrofitClient;
import com.example.ruralize.network.services.DeliveryService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveriesFragment extends Fragment implements EntregaAdapter.OnEntregaClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerEntregas;
    private TextView txtSemEntregas;
    private ProgressBar progressGlobal;
    private TextView txtTotalPendentes;
    private TextView txtTotalEmRota;
    private TextView txtTotalEntregues;

    private FirebaseAuth firebaseAuth;
    private EntregaAdapter entregaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deliveries, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        inicializarComponentes(view);
        configurarRecycler();
        configurarAcoes();
        carregarEntregas();

        return view;
    }

    private void inicializarComponentes(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshEntregas);
        recyclerEntregas = view.findViewById(R.id.recyclerEntregas);
        txtSemEntregas = view.findViewById(R.id.txtSemEntregas);
        progressGlobal = view.findViewById(R.id.progressCarregandoEntregas);
        txtTotalPendentes = view.findViewById(R.id.txtTotalPendentes);
        txtTotalEmRota = view.findViewById(R.id.txtTotalEmRota);
        txtTotalEntregues = view.findViewById(R.id.txtTotalEntregues);
    }

    private void configurarRecycler() {
        entregaAdapter = new EntregaAdapter(this);
        recyclerEntregas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerEntregas.setAdapter(entregaAdapter);
    }

    private void configurarAcoes() {
        swipeRefreshLayout.setOnRefreshListener(this::carregarEntregas);
    }

    private void carregarEntregas() {
        FirebaseUser usuario = firebaseAuth.getCurrentUser();
        if (usuario == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        exibirLoading(true);

        DeliveryService deliveryService = RetrofitClient.getClient().create(DeliveryService.class);
        deliveryService.getDeliveries(usuario.getUid()).enqueue(new Callback<List<Entrega>>() {
            @Override
            public void onResponse(@NonNull Call<List<Entrega>> call, @NonNull Response<List<Entrega>> response) {
                if (!isAdded()) return;
                exibirLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    atualizarInterface(response.body());
                } else {
                    Toast.makeText(requireContext(), "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Entrega>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                exibirLoading(false);
                Toast.makeText(requireContext(), "Erro ao carregar entregas: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarInterface(List<Entrega> entregas) {
        entregaAdapter.atualizarLista(entregas);
        atualizarResumo(entregas);

        boolean vazio = entregas == null || entregas.isEmpty();
        txtSemEntregas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        recyclerEntregas.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private void atualizarResumo(List<Entrega> entregas) {
        if (entregas == null) {
            entregas = Collections.emptyList();
        }

        Map<String, Integer> contador = new HashMap<>();
        for (Entrega entrega : entregas) {
            if (entrega == null) continue;
            String status = entrega.getStatus() != null ? entrega.getStatus().toLowerCase(Locale.ROOT) : com.example.ruralize.utils.OrderStatus.PENDING;
            contador.put(status, contador.getOrDefault(status, 0) + 1);
        }

        int pendentes = contador.getOrDefault(com.example.ruralize.utils.OrderStatus.PENDING, 0) 
                      + contador.getOrDefault(com.example.ruralize.utils.OrderStatus.PREPARING, 0);
        int emRota = contador.getOrDefault(com.example.ruralize.utils.OrderStatus.SHIPPED, 0);
        int entregues = contador.getOrDefault(com.example.ruralize.utils.OrderStatus.DELIVERED, 0);

        txtTotalPendentes.setText(String.valueOf(pendentes));
        txtTotalEmRota.setText(String.valueOf(emRota));
        txtTotalEntregues.setText(String.valueOf(entregues));
    }

    private void exibirLoading(boolean exibindo) {
        progressGlobal.setVisibility(exibindo ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onEntregaSelecionada(Entrega entrega) {
        if (entrega == null) return;
        mostrarDialogoStatus(entrega);
    }

    private void mostrarDialogoStatus(Entrega entrega) {
        String[] options = {
            com.example.ruralize.utils.OrderStatus.getLabel(com.example.ruralize.utils.OrderStatus.PENDING),
            com.example.ruralize.utils.OrderStatus.getLabel(com.example.ruralize.utils.OrderStatus.PREPARING),
            com.example.ruralize.utils.OrderStatus.getLabel(com.example.ruralize.utils.OrderStatus.SHIPPED),
            com.example.ruralize.utils.OrderStatus.getLabel(com.example.ruralize.utils.OrderStatus.DELIVERED),
            com.example.ruralize.utils.OrderStatus.getLabel(com.example.ruralize.utils.OrderStatus.CANCELLED)
        };
        
        String[] values = {
            com.example.ruralize.utils.OrderStatus.PENDING,
            com.example.ruralize.utils.OrderStatus.PREPARING,
            com.example.ruralize.utils.OrderStatus.SHIPPED,
            com.example.ruralize.utils.OrderStatus.DELIVERED,
            com.example.ruralize.utils.OrderStatus.CANCELLED
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Atualizar Status")
                .setItems(options, (dialog, which) -> {
                    String novoStatus = values[which];
                    atualizarStatusServidor(entrega, novoStatus);
                })
                .show();
    }

    private void atualizarStatusServidor(Entrega entrega, String novoStatus) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> statusData = new HashMap<>();
        statusData.put("status", novoStatus);

        DeliveryService deliveryService = RetrofitClient.getClient().create(DeliveryService.class);
        deliveryService.updateDeliveryStatus(user.getUid(), entrega.getId(), statusData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Status atualizado!", Toast.LENGTH_SHORT).show();
                    carregarEntregas();
                } else {
                    Toast.makeText(requireContext(), "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro ao atualizar status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
