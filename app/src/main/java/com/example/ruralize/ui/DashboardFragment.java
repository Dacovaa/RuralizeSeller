package com.example.ruralize.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ruralize.R;

public class DashboardFragment extends Fragment {

    private TextView txtTotalVendas, txtTotalPedidos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        txtTotalVendas = view.findViewById(R.id.txtTotalVendas);
        txtTotalPedidos = view.findViewById(R.id.txtTotalPedidos);
        return view;
    }
}
