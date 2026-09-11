package repositorio;

import java.util.function.Function;
import modelo.PedidoAquisicao;
import modelo.StatusPedido;
import modelo.Usuario;

import java.util.List;

public class PedidoRepositorio extends RepositorioEmMemoria<PedidoAquisicao> {

    @Override
    protected Function<PedidoAquisicao, Integer> extratorDeId() {
        return PedidoAquisicao::getId;
    }

    public List<PedidoAquisicao> listarPorSolicitante(Usuario solicitante) {
        return listarTodos().stream()
                .filter(pedido -> pedido.getSolicitante().equals(solicitante))
                .toList();
    }

    public List<PedidoAquisicao> listarPorStatus(StatusPedido status) {
        return listarTodos().stream()
                .filter(pedido -> pedido.getStatus() == status)
                .toList();
    }
}
