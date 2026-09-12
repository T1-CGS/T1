package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um pedido de aquisicao de itens (Requisito 5).
 * Vinculado ao funcionario solicitante e ao seu departamento.
 */
public class PedidoAquisicao {
    private int id;
    private Usuario solicitante;
    private Departamento departamento;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataConclusao;
    private StatusPedido status;
    private List<ItemPedido> itens;

    public PedidoAquisicao(int id, Usuario solicitante) {
        this.id = id;
        this.solicitante = solicitante;
        this.departamento = (solicitante != null) ? solicitante.getDepartamento() : null;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPedido.ABERTO;
        this.itens = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
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

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void adicionarItem(ItemPedido item) {
        if (item != null) {
            this.itens.add(item);
        }
    }

    public boolean removerItem(int idItem) {
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

    @Override
    public String toString() {
        String siglaDepto = (departamento != null) ? departamento.getSigla() : "N/A";
        String nomeSolicitante = (solicitante != null) ? solicitante.getNome() : "N/A";
        return "Pedido #" + id + " [" + status.getDescricao() + "] - Depto: " + siglaDepto +
                " - Solicitante: " + nomeSolicitante + " - Total: R$ " + getValorTotal();
    }
}
