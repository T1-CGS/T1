package modelo;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de um pedido de aquisicao.
 *
 * <pre>
 * RASCUNHO -> EM_ANALISE -> APROVADO -> CONCLUIDO
 *                        -> REPROVADO
 * RASCUNHO / EM_ANALISE / APROVADO -> CANCELADO
 * </pre>
 */
public enum StatusPedido {
    ABERTO("Aberto"),
    RASCUNHO("Rascunho"),
    EM_ANALISE("Em analise"),
    APROVADO("Aprovado"),
    REPROVADO("Reprovado"),
    CANCELADO("Cancelado"),
    CONCLUIDO("Concluido");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Status para os quais este status pode evoluir. */
    public Set<StatusPedido> proximosPermitidos() {
        return switch (this) {
            case ABERTO -> EnumSet.of(APROVADO, REPROVADO, CANCELADO);
            case RASCUNHO -> EnumSet.of(ABERTO, EM_ANALISE, CANCELADO);
            case EM_ANALISE -> EnumSet.of(APROVADO, REPROVADO, CANCELADO);
            case APROVADO -> EnumSet.of(CONCLUIDO, CANCELADO);
            case REPROVADO, CANCELADO, CONCLUIDO -> EnumSet.noneOf(StatusPedido.class);
        };
    }

    public boolean podeTransitarPara(StatusPedido destino) {
        return destino != null && proximosPermitidos().contains(destino);
    }

    /** Status terminal: nao admite mais nenhuma transicao. */
    public boolean isFinal() {
        return proximosPermitidos().isEmpty();
    }

    /** Somente administradores decidem (aprovam/reprovam) um pedido. */
    public boolean exigeAdministrador() {
        return this == APROVADO || this == REPROVADO;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
