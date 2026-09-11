package modelo;

import java.util.Objects;

/**
 * Departamento ao qual os usuarios e os pedidos de aquisicao estao vinculados.
 */
public class Departamento {

    private final int id;
    private String nome;
    private String sigla;
    private java.math.BigDecimal limitePorPedido;

    public Departamento(int id, String nome, String sigla, java.math.BigDecimal limitePorPedido) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do departamento e obrigatorio.");
        }
        if (sigla == null || sigla.isBlank()) {
            throw new IllegalArgumentException("Sigla do departamento e obrigatoria.");
        }
        if (limitePorPedido == null || limitePorPedido.signum() < 0) {
            throw new IllegalArgumentException("Limite por pedido deve ser um valor positivo.");
        }
        this.id = id;
        this.nome = nome.trim();
        this.sigla = sigla.trim().toUpperCase();
        this.limitePorPedido = limitePorPedido;
    }

    public Departamento(int id, String nome, String sigla) {
        this(id, nome, sigla, new java.math.BigDecimal("10000.00"));
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do departamento e obrigatorio.");
        }
        this.nome = nome.trim();
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            throw new IllegalArgumentException("Sigla do departamento e obrigatoria.");
        }
        this.sigla = sigla.trim().toUpperCase();
    }

    public java.math.BigDecimal getLimitePorPedido() {
        return limitePorPedido;
    }

    public void setLimitePorPedido(java.math.BigDecimal limitePorPedido) {
        if (limitePorPedido == null || limitePorPedido.signum() < 0) {
            throw new IllegalArgumentException("Limite por pedido deve ser um valor positivo.");
        }
        this.limitePorPedido = limitePorPedido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Departamento outro && outro.id == this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "[%d] %s (%s) - Limite: R$ %s".formatted(id, nome, sigla, limitePorPedido);
    }
}
