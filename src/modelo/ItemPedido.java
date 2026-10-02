package modelo;

import java.math.BigDecimal;

/**
 * Item de um pedido de aquisicao (Requisito 5.6).
 * Contem descricao, quantidade, valor unitario e total do item.
 *
 * O item e imutavel: depois de criado nao pode ser alterado. Assim, nem o
 * pedido nem quem guardou uma referencia ao item conseguem mudar os dados de
 * um pedido ja aprovado, reprovado ou concluido (Issue #2).
 */
public class ItemPedido {
    private final int id;
    private final String descricao;
    private final int quantidade;
    private final String unidade;
    private final BigDecimal valorUnitario;

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

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
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
