package modelo;

import java.math.BigDecimal;

/**
 * Item de um pedido de aquisicao (Requisito 5.6).
 * Contem descricao, quantidade, valor unitario e total do item.
 */
public class ItemPedido {
    private int id;
    private String descricao;
    private int quantidade;
    private String unidade;
    private BigDecimal valorUnitario;

    public ItemPedido(int id, String descricao, int quantidade, String unidade, BigDecimal valorUnitario) {
        this.id = id;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.unidade = (unidade == null || unidade.isBlank()) ? "un" : unidade;
        this.valorUnitario = valorUnitario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    /**
     * Calcula o total do item multiplicando quantidade por valor unitario.
     */
    public BigDecimal getSubtotal() {
        if (valorUnitario == null) {
            return BigDecimal.ZERO;
        }
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public String toString() {
        return quantidade + " " + unidade + " x " + descricao + " (Unitario: R$ " + valorUnitario + ") - Subtotal: R$ " + getSubtotal();
    }
}
