package servico;

import modelo.PedidoAquisicao;
import modelo.StatusPedido;
import modelo.Usuario;

import java.util.List;

public class ServicoPedidos {

    //Exclui um pedido da lista se (O pedido precisa estar com status aberto e somente o funcionario que criou o pedido pode excluir.
    public static ResultadoOperacao excluirPedido(List<PedidoAquisicao> pedidos, Usuario usuarioAtual, int idPedido) {
        PedidoAquisicao pedido = buscarPorId(pedidos, idPedido);

        if (pedido == null) {
            return ResultadoOperacao.falha("Pedido #" + idPedido + " nao encontrado.");
        }

        if (pedido.getStatus() != StatusPedido.ABERTO) {
            return ResultadoOperacao.falha("Somente pedidos com status ABERTO podem ser excluidos.");
        }

        boolean ehCriador = pedido.getSolicitante() != null
                && usuarioAtual != null
                && pedido.getSolicitante().getId() == usuarioAtual.getId();

        if (!ehCriador) {
            return ResultadoOperacao.falha("Somente o funcionario que criou o pedido pode exclui-lo.");
        }

        pedidos.remove(pedido);
        return ResultadoOperacao.sucesso("Pedido #" + idPedido + " excluido com sucesso.");
    }

    private static PedidoAquisicao buscarPorId(List<PedidoAquisicao> pedidos, int id) {
        for (PedidoAquisicao pedido : pedidos) {
            if (pedido.getId() == id) {
                return pedido;
            }
        }
        return null;
    }
}