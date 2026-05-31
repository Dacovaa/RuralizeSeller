package com.example.ruralize;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.example.ruralize.network.RetrofitClient;
import com.example.ruralize.network.services.ProductService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NovoProdutoActivity extends ComponentActivity {

    private int contadorFotos = 0;
    private static final int MAX_FOTOS = 5;
    private LinearLayout containerFotos;
    private Spinner spinnerCategoria;
    private EditText edtPreco, edtEstoque, edtTitulo, edtDescricao, edtOpcaoNome, edtOpcaoValores;
    private Button btnEnviar, btnVoltar, btnAdicionarFoto;
    private boolean modoEdicao = false;
    private String produtoId = null;
    private TextView txtContador;
    private FirebaseAuth mAuth;
    private Uri cameraImageUri;
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

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            if (cameraImageUri != null && contadorFotos < MAX_FOTOS) {
                                contadorFotos++;
                                adicionarThumbnail(cameraImageUri);
                                fotosSelecionadas.add(cameraImageUri);
                                atualizarInterface();
                                Toast.makeText(NovoProdutoActivity.this, "Foto tirada adicionada!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

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

            // Popula opções se existirem no ProdutoManager
            if (produtoId != null) {
                Produto p = ProdutoManager.getInstance().getProdutoPorId(produtoId);
                if (p != null && p.getOptions() != null && !p.getOptions().isEmpty()) {
                    Produto.Option firstOption = p.getOptions().get(0);
                    edtOpcaoNome.setText(firstOption.getName());

                    StringBuilder valores = new StringBuilder();
                    java.util.List<Produto.Suboption> subs = firstOption.getSuboptions();
                    if (subs != null) {
                        for (int i = 0; i < subs.size(); i++) {
                            valores.append(subs.get(i).getName());
                            if (i < subs.size() - 1) {
                                valores.append(", ");
                            }
                        }
                    }
                    edtOpcaoValores.setText(valores.toString());
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

    private void mostrarOpcaoDeImagem() {
        String[] opcoes = {"Tirar foto", "Escolher da galeria"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Adicionar imagem")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        abrirCamera();
                    } else {
                        abrirGaleria();
                    }
                });

        builder.show();
    }

    private void abrirCamera() {
        try {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            String nomeArquivo = "foto_" + System.currentTimeMillis() + ".jpg";
            java.io.File arquivo = new java.io.File(getExternalFilesDir(null), nomeArquivo);

            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    arquivo
            );

            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri);

            cameraLauncher.launch(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFunctionality() {
        btnVoltar = findViewById(R.id.btnVoltar);
        btnAdicionarFoto = findViewById(R.id.btnAdicionarFoto);
        btnEnviar = findViewById(R.id.btnEnviar);
        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtPreco = findViewById(R.id.edtPreco);
        edtEstoque = findViewById(R.id.edtEstoque);
        edtOpcaoNome = findViewById(R.id.edtOpcaoNome);
        edtOpcaoValores = findViewById(R.id.edtOpcaoValores);
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
                    mostrarOpcaoDeImagem();
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

    private java.util.List<Produto.Option> parseOptions() {
        String nome = edtOpcaoNome.getText().toString().trim();
        String valoresStr = edtOpcaoValores.getText().toString().trim();

        if (nome.isEmpty() || valoresStr.isEmpty()) {
            return null;
        }

        java.util.List<Produto.Suboption> suboptions = new java.util.ArrayList<>();
        String[] valores = valoresStr.split(",");
        for (int i = 0; i < valores.length; i++) {
            String valor = valores[i].trim();
            if (!valor.isEmpty()) {
                suboptions.add(new Produto.Suboption(java.util.UUID.randomUUID().toString(), valor));
            }
        }

        if (suboptions.isEmpty()) return null;

        java.util.List<Produto.Option> optionsList = new java.util.ArrayList<>();
        optionsList.add(new Produto.Option(java.util.UUID.randomUUID().toString(), nome, suboptions));
        return optionsList;
    }

    private void atualizarProduto(String id, String titulo, String descricao, String preco, String estoque, String categoria) {
        btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Salvando...");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Salvar Alterações");
            return;
        }

        String uid = currentUser.getUid();

        if (fotosSelecionadas.isEmpty() && fotosUrls.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos uma foto!", Toast.LENGTH_SHORT).show();
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Salvar Alterações");
            return;
        }

        if (validarCampos()) return;

        Produto produto = new Produto(id, titulo, descricao, Double.parseDouble(preco), Integer.parseInt(estoque), categoria, fotosUrls);
        produto.setOptions(parseOptions());
        
        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        productService.updateProduct(uid, id, produto).enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(@NonNull Call<Produto> call, @NonNull Response<Produto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String produtoIdResponse = response.body().getId();

                    if (!fotosSelecionadas.isEmpty()) {
                        enviarFotosSequentialmente(uid, produtoIdResponse, btnEnviar, "update");
                    } else {
                        Toast.makeText(NovoProdutoActivity.this, "Produto atualizado com sucesso!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(NovoProdutoActivity.this, "Erro: " + response.code(), Toast.LENGTH_LONG).show();
                    btnEnviar.setEnabled(true);
                    btnEnviar.setText("Salvar Alterações");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Produto> call, @NonNull Throwable t) {
                Toast.makeText(NovoProdutoActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Salvar Alterações");
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
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Enviar Produto");
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

        Produto produto = new Produto(null, titulo, descricao, Double.parseDouble(preco), Integer.parseInt(estoque), categoria, new ArrayList<>());
        produto.setEmpresaId(uid);
        produto.setOptions(parseOptions());

        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        productService.createProduct(produto).enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(@NonNull Call<Produto> call, @NonNull Response<Produto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String produtoIdResponse = response.body().getId();
                    enviarFotosSequentialmente(uid, produtoIdResponse, btnEnviar, "insert");
                } else {
                    Toast.makeText(NovoProdutoActivity.this, "Erro: " + response.code(), Toast.LENGTH_LONG).show();
                    btnEnviar.setEnabled(true);
                    btnEnviar.setText("Enviar Produto");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Produto> call, @NonNull Throwable t) {
                Toast.makeText(NovoProdutoActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Enviar Produto");
            }
        });
    }

    private void configurarSpinnerCategorias() {
        String[] categorias = {
                "Selecione uma categoria",
                "Rações e Concentrados",
                "Suplementos e Vitaminas",
                "Ferraduras e Ferramentas",
                "Selaria e Equipamentos",
                "Higiene e Cuidados",
                "Medicamentos Veterinários",
                "Acessórios para Estábulo",
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

    public byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void uploadFotoParaAPI(String empresaId, String produtoId, Uri fotoUri, Runnable onComplete, Consumer<String> onError) {
        try {
            Log.d("RURALIZE_DEBUG", "Iniciando processamento da imagem: " + fotoUri.toString());

            InputStream inputStream = getContentResolver().openInputStream(fotoUri);
            if (inputStream == null) {
                runOnUiThread(() -> onError.accept("Não foi possível abrir a foto."));
                return;
            }

            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                runOnUiThread(() -> onError.accept("Erro ao decodificar a imagem."));
                return;
            }

            // --- REDIMENSIONAMENTO ---
            int maxWidth = 1200;
            Bitmap resizedBitmap;

            if (originalBitmap.getWidth() > maxWidth) {
                float ratio = (float) originalBitmap.getWidth() / (float) originalBitmap.getHeight();
                int newHeight = (int) (maxWidth / ratio);
                resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, maxWidth, newHeight, true);
                originalBitmap.recycle();
            } else {
                resizedBitmap = originalBitmap;
            }

            // --- COMPRESSÃO ---
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] bytes = baos.toByteArray();

            resizedBitmap.recycle();
            baos.close();

            Log.d("RURALIZE_DEBUG", "Tamanho final comprimido: " + (bytes.length / 1024) + " KB");

            // --- VERIFICAÇÃO DE SEGURANÇA ---
            if (bytes.length > 4 * 1024 * 1024) {
                runOnUiThread(() -> onError.accept("A imagem ainda está muito grande para o servidor."));
                return;
            }

            // --- PREPARAÇÃO DO REQUEST COM RETROFIT ---
            String uniqueName = "foto_" + java.util.UUID.randomUUID() + ".jpg";

            RequestBody fileBody = RequestBody.create(
                    bytes,
                    MediaType.parse("image/jpeg")
            );

            MultipartBody.Part multipartBodyPart = MultipartBody.Part.createFormData("file", uniqueName, fileBody);

            ProductService productService = RetrofitClient.getClient().create(ProductService.class);
            productService.uploadImage(empresaId, produtoId, multipartBodyPart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d("RURALIZE_DEBUG", "Upload ok!");
                        runOnUiThread(onComplete);
                    } else {
                        Log.e("RURALIZE_ERRO", "Status " + response.code());
                        runOnUiThread(() -> onError.accept("Servidor erro " + response.code()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e("RURALIZE_ERRO", "Falha de rede: " + t.getMessage());
                    runOnUiThread(() -> onError.accept("Erro de conexão: " + t.getMessage()));
                }
            });

        } catch (Exception e) {
            Log.e("RURALIZE_ERRO", "Erro crítico: " + e.getMessage());
            runOnUiThread(() -> onError.accept("Erro interno: " + e.getMessage()));
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
                (mensagemErro) -> { // Agora recebemos o erro real aqui
                    Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG).show();
                    btnEnviar.setEnabled(true);
                    btnEnviar.setText("Enviar Produto");
                }
        );
    }

}