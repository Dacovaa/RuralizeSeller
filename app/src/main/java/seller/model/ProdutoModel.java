package seller.model;

/**
 * =========================================================
 *  Classe: ProdutoModel
 *  Camada: MODEL
 *  Função: Representa os dados de um produto do vendedor.
 * =========================================================
 */

public class ProdutoModel {

    // ===== Atributos =====
    private int id;                 // Identificador único do produto
    private String nome;            // Nome do produto
    private String descricao;       // Descrição do produto
    private double preco;           // Preço unitário
    private int estoque;            // Quantidade disponível
    private String categoria;       // Categoria do produto
    private String imagemUrl;       // Caminho/URL da imagem do produto
    private boolean ativo;          // Status do produto (ativo/inativo)

    // ===== Construtor padrão =====
    public ProdutoModel() {}

    // ===== Construtor completo =====
    public ProdutoModel(int id, String nome, String descricao, double preco, int estoque, String categoria, String imagemUrl, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        this.imagemUrl = imagemUrl;
        this.ativo = ativo;
    }

    // ===== Getters e Setters =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // ===== Métodos úteis =====

    /**
     * Retorna o preço formatado em R$ para exibição.
     */
    public String getPrecoFormatado() {
        return String.format("R$ %.2f", preco);
    }

    /**
     * Verifica se o produto está disponível em estoque.
     */
    public boolean isDisponivel() {
        return estoque > 0 && ativo;
    }

    @Override
    public String toString() {
        return "Produto: " + nome + " | Preço: " + getPrecoFormatado();
    }
}