package modelo;

import java.math.BigDecimal;

/**
 * Representa um departamento da empresa.
 * Cada departamento possui um limite maximo permitido por pedido (Requisito 2 e 5.7).
 */
public class Departamento {
    private int id;
    private String nome;
    private String sigla;
    private BigDecimal limitePorPedido;

    public Departamento(int id, String nome, String sigla, BigDecimal limitePorPedido) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.limitePorPedido = limitePorPedido;
    }

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

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public BigDecimal getLimitePorPedido() {
        return limitePorPedido;
    }

    public void setLimitePorPedido(BigDecimal limitePorPedido) {
        this.limitePorPedido = limitePorPedido;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + " (" + sigla + ") - Limite: R$ " + limitePorPedido;
    }
}
