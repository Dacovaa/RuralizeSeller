package com.example.ruralize;

import java.util.ArrayList;
import java.util.List;

public class Produto {
    private String id;
    private String titulo;
    private String descricao;
    private double preco;
    private int estoque;
    private String categoria;
    private List<String> fotosUrls;
    private String empresaId;

    // Construtor vazio necessário
    public Produto() {
        this.fotosUrls = new ArrayList<>();
        // Gerar um ID automático se não for fornecido
        this.id = String.valueOf(System.currentTimeMillis());
    }

    public Produto(String titulo, String descricao, double preco, int estoque, String categoria) {
        this();
        this.titulo = titulo;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
    }

    // Getters e Setters
    public String getId() {
        if (id == null) {
            id = String.valueOf(System.currentTimeMillis());
        }
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

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }
}