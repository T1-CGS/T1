package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um pedido de aquisicao de itens (Requisito 5).
 * Vinculado ao funcionario solicitante e ao seu departamento.
 *
 * Ciclo de vida (Issue #2): ABERTO -> APROVADO ou REPROVADO, e APROVADO -> CONCLUIDO.
 * Somente pedidos abertos podem ter dados ou itens alterados. Aprovados,
 * reprovados e concluidos ficam bloqueados e nunca voltam a ser abertos;
 * a unica transicao depois da aprovacao e a conclusao.
 */
public class PedidoAquisicao {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private Usuario solicitante;
    private Departamento departamento;
    private final LocalDateTime dataCriacao;
    private LocalDateTime dataConclusao;
    private StatusPedido status;
    private final List<ItemPedido> itens;

    public PedidoAquisicao(int id, Usuario solicitante) {
        this(id, solicitante, LocalDateTime.now());
    }

    /**
     * Permite informar a data de criacao, usada pela carga inicial para ter
     * pedidos de exemplo em datas passadas. A data nao pode estar no futuro.
     */
    public PedidoAquisicao(int id, Usuario solicitante, LocalDateTime dataCriacao) {
        if (dataCriacao == null || dataCriacao.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data de criacao do pedido deve ser informada e nao pode estar no futuro.");
        }
        this.id = id;
        this.solicitante = solicitante;
        this.departamento = (solicitante != null) ? solicitante.getDepartamento() : null;
        this.dataCriacao = dataCriacao;
        this.status = StatusPedido.ABERTO;
        this.itens = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        verificarEditavel();
        this.id = id;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        verificarEditavel();
        this.solicitante = solicitante;
        if (solicitante != null) {
            this.departamento = solicitante.getDepartamento();
        }
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    /**
     * Data e hora em que o pedido foi concluido. Fica nula enquanto o pedido
     * nao estiver com status CONCLUIDO.
     */
    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public StatusPedido getStatus() {
        return status;
    }

    /**
     * Pedido so pode ser alterado enquanto estiver aberto.
     */
    public boolean isEditavel() {
        return status == StatusPedido.ABERTO;
    }

    public void aprovar() {
        verificarPodeSerAvaliado();
        this.status = StatusPedido.APROVADO;
    }

    public void reprovar() {
        verificarPodeSerAvaliado();
        this.status = StatusPedido.REPROVADO;
    }

    /**
     * Marca um pedido aprovado como concluido (entregue), registrando
     * automaticamente a data e hora da conclusao.
     */
    public void concluir() {
        if (status != StatusPedido.APROVADO) {
            throw new IllegalStateException("Somente pedidos aprovados podem ser concluidos. Pedido #" + id
                    + " esta " + status.getDescricao() + ".");
        }
        this.status = StatusPedido.CONCLUIDO;
        this.dataConclusao = LocalDateTime.now();
    }

    /**
     * Retorna uma copia somente leitura dos itens. Alteracoes nos itens devem
     * passar por adicionarItem/removerItem, que respeitam o bloqueio.
     */
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(new ArrayList<>(itens));
    }

    public void adicionarItem(ItemPedido item) {
        verificarEditavel();
        if (item != null) {
            this.itens.add(item);
        }
    }

    public boolean removerItem(int idItem) {
        verificarEditavel();
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getId() == idItem) {
                itens.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula o valor total do pedido somando o subtotal de cada item.
     */
    public BigDecimal getValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    private void verificarEditavel() {
        if (!isEditavel()) {
            throw new IllegalStateException("Pedido #" + id + " esta " + status.getDescricao()
                    + " e nao pode mais ser alterado.");
        }
    }

    private void verificarPodeSerAvaliado() {
        if (status != StatusPedido.ABERTO) {
            throw new IllegalStateException("Pedido #" + id + " esta " + status.getDescricao()
                    + " e nao pode ser avaliado novamente.");
        }
    }

    @Override
    public String toString() {
        String siglaDepto = (departamento != null) ? departamento.getSigla() : "N/A";
        String nomeSolicitante = (solicitante != null) ? solicitante.getNome() : "N/A";
        String texto = "Pedido #" + id + " [" + status.getDescricao() + "] - Depto: " + siglaDepto +
                " - Solicitante: " + nomeSolicitante + " - Total: R$ " + getValorTotal();
        if (dataConclusao != null) {
            texto += " - Concluido em: " + dataConclusao.format(FORMATO_DATA);
        }
        return texto;
    }
}
