package com.example.ruralize;

import android.app.AlertDialog;
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
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.ruralize.network.ApiConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class NovoProdutoActivity extends ComponentActivity {

    private int contadorFotos = 0;
    private static final int MAX_FOTOS = 5;
    private LinearLayout containerFotos;
    private Spinner spinnerCategoria;
    private EditText edtPreco, edtEstoque, edtTitulo, edtDescricao;
    private Button btnEnviar, btnVoltar, btnAdicionarFoto;
    private boolean modoEdicao = false;
    private String produtoId = null;
    private TextView txtContador;
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
            ArrayList<String> fotos = intent.getStringArrayListExtra("FOTOSURL");

            edtTitulo = findViewById(R.id.edtTitulo);
            edtDescricao = findViewById(R.id.edtDescricao);
            edtPreco = findViewById(R.id.edtPreco);
            edtEstoque = findViewById(R.id.edtEstoque);
            spinnerCategoria = findViewById(R.id.spinnerCategoria);
            containerFotos = findViewById(R.id.containerFotos);

            edtTitulo.setText(titulo);
            edtDescricao.setText(descricao);
            edtPreco.setText(String.valueOf(preco));
            edtEstoque.setText(String.valueOf(estoque));

            for (String url : fotos) {
                ImageView imageView = getImageView();
                imageView.setOnLongClickListener(v -> {
                    mostrarConfirmacaoExclusao(url, imageView);
                    return true;
                });
                Glide.with(this).load(url).into(imageView);
                containerFotos.addView(imageView);
                fotosUrls.add(url);
                contadorFotos++;
                atualizarInterface();
            }

            if (categoria != null) {
                ArrayAdapter adapter = (ArrayAdapter) spinnerCategoria.getAdapter();
                int pos = adapter.getPosition(categoria);
                if (pos >= 0) {
                    spinnerCategoria.setSelection(pos);
                }
            }

            btnEnviar = findViewById(R.id.btnEnviar);
            btnEnviar.setText("Salvar Alterações");
        }
    }

    private void mostrarConfirmacaoExclusao(final String url, final ImageView imageView) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Foto")
                .setMessage("Tem certeza que deseja remover esta foto? A alteração será salva quando você atualizar o produto.")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> {
                    fotosUrls.remove(url);
                    containerFotos.removeView(imageView);
                    contadorFotos--;
                    atualizarInterface();
                    Toast.makeText(this, "Foto marcada para exclusão.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @NonNull
    private ImageView getImageView() {
        ImageView imageView = new ImageView(this);
        int size = (int) (120 * getResources().getDisplayMetrics().density);
        int padding = (int) (4 * getResources().getDisplayMetrics().density);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        params.setMargins(8, 0, 8, 0);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setMaxWidth(size);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundResource(R.drawable.bg_button_outline_simple);
        return imageView;
    }

    private void setupFunctionality() {
        btnVoltar = findViewById(R.id.btnVoltar);
        btnAdicionarFoto = findViewById(R.id.btnAdicionarFoto);
        btnEnviar = findViewById(R.id.btnEnviar);
        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtPreco = findViewById(R.id.edtPreco);
        edtEstoque = findViewById(R.id.edtEstoque);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        containerFotos = findViewById(R.id.containerFotos);

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
        btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Salvando...");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        if (fotosSelecionadas.isEmpty() && fotosUrls.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos uma foto!", Toast.LENGTH_SHORT).show();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Salvar alterações");
            return;
        }

        if (validarCampos()) return;

        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("titulo", titulo);
            jsonBody.put("descricao", descricao);
            jsonBody.put("preco", Double.parseDouble(preco));
            jsonBody.put("estoque", Integer.parseInt(estoque));
            jsonBody.put("categoria", categoria);
            org.json.JSONArray urlsArray = new org.json.JSONArray(fotosUrls);
            jsonBody.put("fotos", urlsArray);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json; charset=utf-8");
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody.toString(), JSON);

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(ApiConfig.productUpdate(uid, id))
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
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(NovoProdutoActivity.this, "❌ Erro: " + response.code(), Toast.LENGTH_LONG).show();
                            btnEnviar.setEnabled(true);
                            btnEnviar.setText("Salvar Alterações");
                        });
                    }
                    String responseBody = null;
                    try {
                        assert response.body() != null;
                        responseBody = response.body().string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseBody);
                        String produtoId = json.getString("id");

                        if (!fotosSelecionadas.isEmpty()) {
                            enviarFotosSequentialmente(uid, produtoId, btnEnviar, "update");
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(NovoProdutoActivity.this, "Produto atualizado com sucesso!", Toast.LENGTH_LONG).show();
                                finish();
                            });
                        }

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(NovoProdutoActivity.this, "Erro ao ler resposta do servidor", Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        });
    }

    private void enviarProduto(String titulo, String descricao, String preco, String estoque, String categoria) {
        btnEnviar = findViewById(R.id.btnEnviar);
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

        if (validarCampos()) return;

        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("titulo", titulo);
            jsonBody.put("descricao", descricao);
            jsonBody.put("preco", Double.parseDouble(preco));
            jsonBody.put("estoque", Integer.parseInt(estoque));
            jsonBody.put("categoria", categoria);
            jsonBody.put("empresaId", uid);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(NovoProdutoActivity.this, "Erro ao montar JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json; charset=utf-8");
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody.toString(), JSON);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(ApiConfig.productsCollection())
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
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(NovoProdutoActivity.this, "❌ Erro: " + response.code(), Toast.LENGTH_LONG).show();
                            btnEnviar.setEnabled(true);
                            btnEnviar.setText("Enviar Produto");
                        });
                    }
                    String responseBody = null;
                    try {
                        assert response.body() != null;
                        responseBody = response.body().string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseBody);
                        String produtoId = json.getString("id");

                        enviarFotosSequentialmente(uid, produtoId, btnEnviar, "insert");

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(NovoProdutoActivity.this, "Erro ao ler resposta do servidor", Toast.LENGTH_LONG).show();
                        });
                    }

                });
            }
        });
    }

    private void configurarSpinnerCategorias() {
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
            ImageView imageView = getImageView();
            Glide.with(this).load(uri).into(imageView);
            containerFotos.addView(imageView);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarInterface() {
        btnAdicionarFoto = findViewById(R.id.btnAdicionarFoto);
        txtContador = findViewById(R.id.txtContadorFotos);

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

    private boolean validarCampos() {
        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescricao = findViewById(R.id.edtDescricao);
        String titulo = edtTitulo.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String precoStr = edtPreco.getText().toString().trim();
        String estoqueStr = edtEstoque.getText().toString().trim();
        String categoria = spinnerCategoria.getSelectedItem().toString();

        if (titulo.isEmpty()) {
            edtTitulo.setError("Informe o título");
            edtTitulo.requestFocus();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
            return true;
        }

        if (precoStr.isEmpty()) {
            edtPreco.setError("Informe o preço");
            edtPreco.requestFocus();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
            return true;
        }

        if (estoqueStr.isEmpty()) {
            edtEstoque.setError("Informe o estoque");
            edtEstoque.requestFocus();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
            return true;
        }

        if (categoria.equals("Selecione uma categoria")) {
            Toast.makeText(this, "Escolha uma categoria", Toast.LENGTH_SHORT).show();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
            return true;
        }

        if (descricao.isEmpty()) {
            edtDescricao.setError("Informe a descrição");
            edtDescricao.requestFocus();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
            return true;
        }

        try {
            Double.parseDouble(precoStr);
        } catch (NumberFormatException e) {
            edtPreco.setError("Preço inválido");
            edtPreco.requestFocus();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
            return true;
        }

        try {
            Integer.parseInt(estoqueStr);
        } catch (NumberFormatException e) {
            edtEstoque.setError("Estoque inválido");
            edtEstoque.requestFocus();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
            return true;
        }

        return false;
    }

    private void uploadFotoParaAPI(String empresaId, String produtoId, Uri fotoUri, Runnable onComplete, Runnable onError) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fotoUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String uniqueName = "foto_" + java.util.UUID.randomUUID() + ".jpg";

            okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(
                    bytes,
                    okhttp3.MediaType.parse("image/jpeg")
            );

            okhttp3.MultipartBody requestBody = new okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart("file", uniqueName, fileBody)
                    .build();

            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(ApiConfig.uploadProductImage(empresaId, produtoId))
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(onError);
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(onError);
                        return;
                    }
                    runOnUiThread(onComplete);
                }
            });

        } catch (Exception e) {
            runOnUiThread(onError);
        }
    }

    private void enviarFotosSequentialmente(String empresaId, String produtoId, Button btnEnviar, String type) {
        if (fotosSelecionadas.isEmpty()) {
            runOnUiThread(() -> {
                if (Objects.equals(type, "update")) {
                    Toast.makeText(this, "Produto atualizado com sucesso!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Produto cadastrado com sucesso!", Toast.LENGTH_LONG).show();
                }
                finish();
            });
            return;
        }

        Uri foto = fotosSelecionadas.remove(0);

        uploadFotoParaAPI(empresaId, produtoId, foto,
                () -> enviarFotosSequentialmente(empresaId, produtoId, btnEnviar, type),
                () -> {
                    Toast.makeText(this, "Erro ao fazer upload da foto!", Toast.LENGTH_SHORT).show();
                    btnEnviar.setEnabled(true);
                    btnEnviar.setText("Enviar Produto");
                }
        );
    }

}