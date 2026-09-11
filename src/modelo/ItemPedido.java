package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Item de um pedido de aquisicao (o que sera comprado, em que quantidade e por
 * qual valor unitario estimado).
 */
public class ItemPedido {

    private final int id;
    private String descricao;
    private int quantidade;
    private String unidade;
    private BigDecimal valorUnitario;

    public ItemPedido(int id, String descricao, int quantidade, String unidade, BigDecimal valorUnitario) {
        this.id = id;
        setDescricao(descricao);
        setQuantidade(quantidade);
        setUnidade(unidade);
        setValorUnitario(valorUnitario);
    }

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public final void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descricao do item e obrigatoria.");
        }
        this.descricao = descricao.trim();
    }

    public int getQuantidade() {
        return quantidade;
    }

    public final void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public final void setUnidade(String unidade) {
        this.unidade = (unidade == null || unidade.isBlank()) ? "un" : unidade.trim();
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public final void setValorUnitario(BigDecimal valorUnitario) {
        if (valorUnitario == null) {
            throw new IllegalArgumentException("Valor unitario e obrigatorio.");
        }
        if (valorUnitario.signum() < 0) {
            throw new IllegalArgumentException("Valor unitario nao pode ser negativo.");
        }
        this.valorUnitario = valorUnitario.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSubtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ItemPedido outro && outro.id == this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "%dx %s (%s) - unit. %s - subtotal %s".formatted(quantidade, descricao, unidade, valorUnitario, getSubtotal());
    }
}
