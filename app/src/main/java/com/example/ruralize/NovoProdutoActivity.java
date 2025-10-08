package com.example.ruralize;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class NovoProdutoActivity extends ComponentActivity {

    private int contadorFotos = 0;
    private static final int MAX_FOTOS = 5;
    private LinearLayout containerFotos;

    private final ActivityResultLauncher<Intent> galeriaLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    Uri imagemSelecionada = data.getData();
                                    if (imagemSelecionada != null && contadorFotos < MAX_FOTOS) {
                                        contadorFotos++;
                                        adicionarThumbnail(imagemSelecionada);
                                        atualizarInterface();
                                        Toast.makeText(NovoProdutoActivity.this, "Foto " + contadorFotos + " adicionada!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto);

        setupFunctionality();
    }

    private void setupFunctionality() {
        Button btnVoltar = findViewById(R.id.btnVoltar);
        Button btnAdicionarFoto = findViewById(R.id.btnAdicionarFoto);
        Button btnEnviar = findViewById(R.id.btnEnviar);
        EditText edtTitulo = findViewById(R.id.edtTitulo);
        EditText edtDescricao = findViewById(R.id.edtDescricao);
        containerFotos = findViewById(R.id.containerFotos);

        if (containerFotos == null) {
            containerFotos = new LinearLayout(this);
            containerFotos.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    140
            ));
            containerFotos.setOrientation(LinearLayout.HORIZONTAL);
        }

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAdicionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contadorFotos < MAX_FOTOS) {
                    abrirGaleria();
                } else {
                    Toast.makeText(NovoProdutoActivity.this, "Máximo de " + MAX_FOTOS + " fotos atingido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = edtTitulo.getText().toString().trim();
                String descricao = edtDescricao.getText().toString().trim();

                if (validarFormulario(titulo, descricao)) {
                    enviarProduto(titulo, descricao);
                }
            }
        });

        atualizarInterface();
    }

    private void abrirGaleria() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            galeriaLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir galeria: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void adicionarThumbnail(Uri uri) {
        try {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
            params.setMargins(8, 0, 8, 0);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURI(uri);
            imageView.setBackgroundResource(R.drawable.bg_button_outline_simple);

            containerFotos.addView(imageView);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarInterface() {
        Button btnAdicionarFoto = findViewById(R.id.btnAdicionarFoto);
        TextView txtContador = findViewById(R.id.txtContadorFotos);

        if (txtContador != null) {
            txtContador.setText(contadorFotos + "/" + MAX_FOTOS + " fotos");
        }

        if (contadorFotos >= MAX_FOTOS) {
            btnAdicionarFoto.setEnabled(false);
            btnAdicionarFoto.setText("Máximo atingido");
        } else {
            btnAdicionarFoto.setEnabled(true);
            btnAdicionarFoto.setText("➕ Adicionar Fotos");
        }
    }

    private boolean validarFormulario(String titulo, String descricao) {
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Digite um título para o anúncio", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (descricao.isEmpty()) {
            Toast.makeText(this, "Digite uma descrição para o produto", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (contadorFotos == 0) {
            Toast.makeText(this, "Adicione pelo menos uma foto", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void enviarProduto(String titulo, String descricao) {
        Button btnEnviar = findViewById(R.id.btnEnviar);

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NovoProdutoActivity.this, "✅ Produto '" + titulo + "' enviado com " + contadorFotos + " fotos!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(NovoProdutoActivity.this, GerenciarProdutosActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }
}