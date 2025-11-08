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

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class NovoProdutoActivity extends ComponentActivity {

    private int contadorFotos = 0;
    private static final int MAX_FOTOS = 5;
    private LinearLayout containerFotos;
    private Spinner spinnerCategoria;
    private EditText edtPreco, edtEstoque;
    private boolean modoEdicao = false;
    private String produtoId = null;
    private TextView produtoTitulo;
    private FirebaseAuth mAuth;
    private final java.util.List<Uri> fotosSelecionadas = new java.util.ArrayList<>();
    private final java.util.List<String> fotosUrls = new java.util.ArrayList<>();

    private interface UploadCallback {
        void onUploadComplete(java.util.List<String> urls);
        void onUploadError(Exception e);
    }


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
                                        fotosSelecionadas.add(imagemSelecionada);
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

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        modoEdicao = getIntent().getBooleanExtra("MODO_EDICAO", false);
        produtoId = getIntent().getStringExtra("ID");

        setupFunctionality();
        preencherCamposSeModoEdicao();
    }

    private void preencherCamposSeModoEdicao() {
        Intent intent = getIntent();
        boolean modoEdicao = intent.getBooleanExtra("MODO_EDICAO", false);

        if (modoEdicao) {
            String titulo = intent.getStringExtra("TITULO");
            String descricao = intent.getStringExtra("DESCRICAO");
            double preco = intent.getDoubleExtra("PRECO", 0.0);
            int estoque = intent.getIntExtra("ESTOQUE", 0);
            String categoria = intent.getStringExtra("CATEGORIA");

            EditText edtTitulo = findViewById(R.id.edtTitulo);
            EditText edtDescricao = findViewById(R.id.edtDescricao);
            edtPreco = findViewById(R.id.edtPreco);
            edtEstoque = findViewById(R.id.edtEstoque);
            spinnerCategoria = findViewById(R.id.spinnerCategoria);

            edtTitulo.setText(titulo);
            edtDescricao.setText(descricao);
            edtPreco.setText(String.valueOf(preco));
            edtEstoque.setText(String.valueOf(estoque));

            if (categoria != null) {
                ArrayAdapter adapter = (ArrayAdapter) spinnerCategoria.getAdapter();
                int pos = adapter.getPosition(categoria);
                if (pos >= 0) {
                    spinnerCategoria.setSelection(pos);
                }
            }

            Button btnEnviar = findViewById(R.id.btnEnviar);
            btnEnviar.setText("Salvar Alterações");
        }
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

                if (modoEdicao && produtoId != null) {
                    atualizarProduto(produtoId, titulo, descricao, preco, estoque, categoria);
                } else {
                    enviarProduto(titulo, descricao, preco, estoque, categoria);
                }
            }
        });

        atualizarInterface();
    }

    private void atualizarProduto(String id, String titulo, String descricao, String preco, String estoque, String categoria) {
        Button btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Salvando...");

        FirebaseUser currentUser = mAuth.getCurrentUser();

        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("titulo", titulo);
            jsonBody.put("descricao", descricao);
            jsonBody.put("fotos", new org.json.JSONArray());
            jsonBody.put("preco", Double.parseDouble(preco));
            jsonBody.put("estoque", Integer.parseInt(estoque));
            jsonBody.put("categoria", categoria);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json; charset=utf-8");
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody.toString(), JSON);

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://ruralize-api.vercel.app/products/" + id)
                .patch(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(NovoProdutoActivity.this, "Erro ao atualizar produto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnEnviar.setEnabled(true);
                    btnEnviar.setText("Salvar Alterações");
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(NovoProdutoActivity.this, "✅ Produto atualizado com sucesso!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(NovoProdutoActivity.this, GerenciarProdutosActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(NovoProdutoActivity.this, "❌ Erro: " + response.code(), Toast.LENGTH_LONG).show();
                        btnEnviar.setEnabled(true);
                        btnEnviar.setText("Salvar Alterações");
                    }
                });
            }
        });
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

        return true;
    }

    private void enviarProduto(String titulo, String descricao, String preco, String estoque, String categoria) {
        Button btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        if (fotosSelecionadas.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos uma foto!", Toast.LENGTH_SHORT).show();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
            return;
        }

        uploadFotosParaStorage(uid, new UploadCallback() {
            @Override
            public void onUploadComplete(java.util.List<String> urls) {
                org.json.JSONObject jsonBody = new org.json.JSONObject();
                try {
                    jsonBody.put("titulo", titulo);
                    jsonBody.put("descricao", descricao);
                    jsonBody.put("fotos", new org.json.JSONArray(urls));
                    jsonBody.put("preco", Double.parseDouble(preco));
                    jsonBody.put("estoque", Integer.parseInt(estoque));
                    jsonBody.put("categoria", categoria);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(NovoProdutoActivity.this, "Erro ao montar JSON", Toast.LENGTH_SHORT).show();
                    return;
                }

                okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json; charset=utf-8");
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody.toString(), JSON);

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("https://ruralize-api.vercel.app/products")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(NovoProdutoActivity.this, "Erro ao enviar produto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            btnEnviar.setEnabled(true);
                            btnEnviar.setText("Enviar Produto");
                        });
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                Toast.makeText(NovoProdutoActivity.this, "✅ Produto cadastrado com sucesso!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(NovoProdutoActivity.this, GerenciarProdutosActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(NovoProdutoActivity.this, "❌ Erro: " + response.code(), Toast.LENGTH_LONG).show();
                                btnEnviar.setEnabled(true);
                                btnEnviar.setText("Enviar Produto");
                            }
                        });
                    }
                });
            }

            @Override
            public void onUploadError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(NovoProdutoActivity.this, "Erro ao enviar fotos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnEnviar.setEnabled(true);
                    btnEnviar.setText("Enviar Produto");
                });
            }
        });
    }

    private void uploadFotosParaStorage(String userId, UploadCallback callback) {
        com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        com.google.firebase.storage.StorageReference storageRef = storage.getReference();

        fotosUrls.clear();

        new Thread(() -> {
            try {
                for (Uri fotoUri : fotosSelecionadas) {
                    String nomeArquivo = "produtos/" + userId + "/" + System.currentTimeMillis() + ".jpg";
                    com.google.firebase.storage.StorageReference fotoRef = storageRef.child(nomeArquivo);

                    java.io.InputStream stream = getContentResolver().openInputStream(fotoUri);
                    fotoRef.putStream(stream).addOnSuccessListener(taskSnapshot ->
                            fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                fotosUrls.add(uri.toString());
                                if (fotosUrls.size() == fotosSelecionadas.size()) {
                                    callback.onUploadComplete(fotosUrls);
                                }
                            }).addOnFailureListener(callback::onUploadError)
                    ).addOnFailureListener(callback::onUploadError);
                }
            } catch (Exception e) {
                callback.onUploadError(e);
            }
        }).start();
    }

}