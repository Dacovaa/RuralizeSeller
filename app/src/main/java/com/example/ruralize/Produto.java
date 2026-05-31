package com.example.ruralize;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Produto {
    @SerializedName("id")
    private String id;
    
    @SerializedName("titulo")
    private String titulo;
    
    @SerializedName("descricao")
    private String descricao;
    
    @SerializedName("preco")
    private double preco;
    
    @SerializedName("estoque")
    private int estoque;
    
    @SerializedName("categoria")
    private String categoria;
    
    @SerializedName("empresaId")
    private String empresaId;
    
    @SerializedName("fotos")
    private List<String> fotosUrls;

    @SerializedName("options")
    private List<Option> options;

    public Produto(String id, String titulo, String descricao, double preco, int estoque, String categoria, List<String> fotosUrls) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        this.fotosUrls = fotosUrls;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = empresaId;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public List<String> getFotosUrls() { return fotosUrls; }
    public void setFotosUrls(List<String> fotosUrls) { this.fotosUrls = fotosUrls; }

    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }

    public static class Suboption {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;

        public Suboption(String id, String name) {
            this.id = id;
            this.name = name;
        }
        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static class Option {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;
        @SerializedName("suboptions")
        private List<Suboption> suboptions;

        public Option(String id, String name, List<Suboption> suboptions) {
            this.id = id;
            this.name = name;
            this.suboptions = suboptions;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public List<Suboption> getSuboptions() { return suboptions; }
    }
}