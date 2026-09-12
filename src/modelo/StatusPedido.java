package modelo;

/**
 * Estados do ciclo de vida de um pedido de aquisicao (Requisito 5.5).
 */
public enum StatusPedido {
    ABERTO("Aberto"),
    APROVADO("Aprovado"),
    REPROVADO("Reprovado"),
    CONCLUIDO("Concluido");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
