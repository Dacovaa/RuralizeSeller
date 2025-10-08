package com.example.ruralize;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
    private Spinner spinnerCategoria;
    private EditText edtPreco, edtEstoque;

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
        edtPreco = findViewById(R.id.edtPreco);
        edtEstoque = findViewById(R.id.edtEstoque);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        containerFotos = findViewById(R.id.containerFotos);

        // Configurar o Spinner de categorias
        configurarSpinnerCategorias();

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
                String preco = edtPreco.getText().toString().trim();
                String estoque = edtEstoque.getText().toString().trim();
                String categoria = spinnerCategoria.getSelectedItem().toString();

                if (validarFormulario(titulo, descricao, preco, estoque, categoria)) {
                    enviarProduto(titulo, descricao, preco, estoque, categoria);
                }
            }
        });

        atualizarInterface();
    }

    private void configurarSpinnerCategorias() {
        // Defina as categorias que fazem sentido para produtos rurais
        String[] categorias = {
                "Selecione uma categoria",
                "Grãos e Cereais",
                "Frutas",
                "Verduras e Legumes",
                "Laticínios",
                "Carnes",
                "Ovos",
                "Mel e Derivados",
                "Plantas e Mudas",
                "Artesanato Rural",
                "Outros"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
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

    private boolean validarFormulario(String titulo, String descricao, String preco, String estoque, String categoria) {
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Digite um título para o anúncio", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (preco.isEmpty()) {
            Toast.makeText(this, "Digite o preço do produto", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double precoValue = Double.parseDouble(preco);
            if (precoValue <= 0) {
                Toast.makeText(this, "O preço deve ser maior que zero", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Digite um preço válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (estoque.isEmpty()) {
            Toast.makeText(this, "Digite a quantidade em estoque", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int estoqueValue = Integer.parseInt(estoque);
            if (estoqueValue < 0) {
                Toast.makeText(this, "O estoque não pode ser negativo", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Digite um valor válido para o estoque", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (categoria.equals("Selecione uma categoria")) {
            Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show();
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

    private void enviarProduto(String titulo, String descricao, String preco, String estoque, String categoria) {
        Button btnEnviar = findViewById(R.id.btnEnviar);

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NovoProdutoActivity.this,
                        "✅ Produto '" + titulo + "' enviado!\n" +
                                "Preço: R$ " + preco + "\n" +
                                "Estoque: " + estoque + " unidades\n" +
                                "Categoria: " + categoria + "\n" +
                                "Fotos: " + contadorFotos, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(NovoProdutoActivity.this, GerenciarProdutosActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }
}