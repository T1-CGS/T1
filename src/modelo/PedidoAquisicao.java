package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pedido de aquisicao aberto por um usuario em nome de um departamento.
 */
public class PedidoAquisicao {

    private final int id;
    private final Usuario solicitante;
    private final Departamento departamento;
    private final LocalDateTime dataCriacao;
    private final List<ItemPedido> itens = new ArrayList<>();
    private final AtomicInteger sequenciaItens = new AtomicInteger(1);

    private String justificativa;
    private StatusPedido status = StatusPedido.ABERTO;

    private LocalDateTime dataConclusao;
    private Usuario responsavelDecisao;
    private LocalDateTime dataDecisao;
    private String observacaoDecisao;

    public PedidoAquisicao(int id, Usuario solicitante, String justificativa) {
        this.id = id;
        this.solicitante = Objects.requireNonNull(solicitante, "Solicitante e obrigatorio.");
        this.departamento = solicitante.getDepartamento();
        this.dataCriacao = LocalDateTime.now();
        setJustificativa(justificativa);
    }

    public int getId() {
        return id;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public final void setJustificativa(String justificativa) {
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("Justificativa do pedido e obrigatoria.");
        }
        this.justificativa = justificativa.trim();
    }

    public StatusPedido getStatus() {
        return status;
    }

    public Usuario getResponsavelDecisao() {
        return responsavelDecisao;
    }

    public LocalDateTime getDataDecisao() {
        return dataDecisao;
    }

    public String getObservacaoDecisao() {
        return observacaoDecisao;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /** Cria e anexa um item ao pedido. So e permitido enquanto o pedido e rascunho. */
    public ItemPedido adicionarItem(String descricao, int quantidade, String unidade, BigDecimal valorUnitario) {
        exigirRascunho("adicionar itens");
        ItemPedido item = new ItemPedido(sequenciaItens.getAndIncrement(), descricao, quantidade, unidade, valorUnitario);
        itens.add(item);
        return item;
    }

    public boolean removerItem(int idItem) {
        exigirRascunho("remover itens");
        return itens.removeIf(item -> item.getId() == idItem);
    }

    public BigDecimal getValorTotal() {
        return itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Envia o pedido para analise. Exige ao menos um item. */
    public void enviarParaAnalise(Usuario autor) {
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens nao pode ser enviado para analise.");
        }
        alterarStatus(StatusPedido.EM_ANALISE, autor, null);
    }

    /** Aprova ou reprova o pedido. Somente administradores podem decidir. */
    public void registrarDecisao(StatusPedido decisao, Usuario administrador, String observacao) {
        if (decisao == null || !decisao.exigeAdministrador()) {
            throw new IllegalArgumentException("Decisao deve ser APROVADO ou REPROVADO.");
        }
        alterarStatus(decisao, administrador, observacao);
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void cancelar(Usuario autor, String motivo) {
        alterarStatus(StatusPedido.CANCELADO, autor, motivo);
    }

    public void concluir(Usuario autor) {
        this.dataConclusao = LocalDateTime.now();
        alterarStatus(StatusPedido.CONCLUIDO, autor, null);
    }

    /**
     * Transicao unica de status, validando o fluxo e o papel de quem executa.
     */
    public void alterarStatus(StatusPedido novoStatus, Usuario autor, String observacao) {
        Objects.requireNonNull(novoStatus, "Novo status e obrigatorio.");
        Objects.requireNonNull(autor, "Autor da alteracao e obrigatorio.");
        if (!status.podeTransitarPara(novoStatus)) {
            throw new IllegalStateException(
                    "Transicao invalida: %s -> %s.".formatted(status.getDescricao(), novoStatus.getDescricao()));
        }
        if (novoStatus.exigeAdministrador() && !autor.isAdministrador()) {
            throw new IllegalStateException(
                    "Somente um administrador pode marcar o pedido como %s.".formatted(novoStatus.getDescricao()));
        }
        this.status = novoStatus;
        if (novoStatus.exigeAdministrador()) {
            this.responsavelDecisao = autor;
            this.dataDecisao = LocalDateTime.now();
        }
        if (observacao != null && !observacao.isBlank()) {
            this.observacaoDecisao = observacao.trim();
        }
    }

    private void exigirRascunho(String acao) {
        if (status != StatusPedido.ABERTO && status != StatusPedido.RASCUNHO) {
            throw new IllegalStateException("So e possivel %s enquanto o pedido esta aberto.".formatted(acao));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof PedidoAquisicao outro && outro.id == this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido #%d - %s - %s - %d item(ns) - total %s".formatted(
                id, departamento.getSigla(), status.getDescricao(), itens.size(), getValorTotal());
    }
}
