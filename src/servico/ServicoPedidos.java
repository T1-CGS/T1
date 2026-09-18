package servico;

import modelo.PedidoAquisicao;
import modelo.StatusPedido;
import modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

public class ServicoPedidos {

    // Exclui um pedido da lista se (O pedido precisa estar com status aberto e
    // somente o funcionario que criou o pedido pode excluir)
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

    // Aprova um pedido aberto (Só administrador pode aprovar, e só pedido "aberto"
    // pode ser avaliado)
    public static ResultadoOperacao aprovarPedido(Usuario administrador, PedidoAquisicao pedido) {
        return avaliarPedido(administrador, pedido, StatusPedido.APROVADO);
    }

    // Reprova um pedido aberto.
    public static ResultadoOperacao reprovarPedido(Usuario administrador, PedidoAquisicao pedido) {
        return avaliarPedido(administrador, pedido, StatusPedido.REPROVADO);
    }

    // Só administrador avalia, e só pedido "aberto" pode mudar de status.
    // (pedidos ja aprovados/reprovados/concluidos nunca sao reabertos ou editados)
    private static ResultadoOperacao avaliarPedido(Usuario administrador, PedidoAquisicao pedido,
            StatusPedido novoStatus) {
        if (administrador == null || !administrador.isAdministrador()) {
            return ResultadoOperacao.falha("Somente um administrador pode avaliar pedidos.");
        }

        if (pedido == null) {
            return ResultadoOperacao.falha("Pedido nao encontrado.");
        }

        if (pedido.getStatus() != StatusPedido.ABERTO) {
            return ResultadoOperacao
                    .falha("Somente pedidos com status ABERTO podem ser avaliados (regra de imutabilidade).");
        }

        pedido.setStatus(novoStatus);
        return ResultadoOperacao
                .sucesso("Pedido #" + pedido.getId() + " marcado como " + novoStatus.getDescricao() + ".");
    }

    // Lista todos os pedidos com status "aberrto", usados na tela de avaliação do
    // administrador.
    public static List<PedidoAquisicao> listarPedidosAbertos(List<PedidoAquisicao> pedidos) {
        List<PedidoAquisicao> abertos = new ArrayList<>();

        for (PedidoAquisicao pedido : pedidos) {
            if (pedido.getStatus() == StatusPedido.ABERTO) {
                abertos.add(pedido);
            }
        }

        return abertos;
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