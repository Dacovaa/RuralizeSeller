package com.example.ruralize;

import org.json.JSONArray;

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

    public Produto(String id, String titulo, String descricao, double preco, int estoque, String categoria, List<String> fotosUrls) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        this.fotosUrls = fotosUrls;
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
}