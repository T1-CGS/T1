package servico;

import modelo.Departamento;
import modelo.ItemPedido;
import modelo.PedidoAquisicao;
import modelo.StatusPedido;
import modelo.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServicoPedidos {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // STRICT recusa datas inexistentes como 31/02 em vez de ajusta-las.
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    // Registra um novo pedido de aquisicao para o solicitante informado.
    // O departamento vem do proprio solicitante e o status inicial e ABERTO.
    // O valor total nao pode ultrapassar o teto por pedido do departamento.
    public static ResultadoOperacao registrarPedido(List<PedidoAquisicao> pedidos, Usuario solicitante,
            List<ItemPedido> itens) {
        if (solicitante == null) {
            return ResultadoOperacao.falha("E necessario informar o funcionario solicitante.");
        }

        Departamento departamento = solicitante.getDepartamento();

        if (departamento == null) {
            return ResultadoOperacao
                    .falha("O solicitante " + solicitante.getNome() + " nao esta alocado a nenhum departamento.");
        }

        if (itens == null || itens.isEmpty()) {
            return ResultadoOperacao.falha("O pedido precisa ter pelo menos um item.");
        }

        // Sem esta checagem um item com quantidade ou valor negativo reduziria o
        // total e permitiria furar o teto do departamento.
        for (ItemPedido item : itens) {
            if (item == null) {
                return ResultadoOperacao.falha("O pedido contem um item invalido.");
            }

            if (item.getQuantidade() < 1) {
                return ResultadoOperacao
                        .falha("O item \"" + item.getDescricao() + "\" precisa ter quantidade de pelo menos 1.");
            }

            if (item.getValorUnitario() == null || item.getValorUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                return ResultadoOperacao
                        .falha("O item \"" + item.getDescricao() + "\" precisa ter valor unitario maior que zero.");
            }
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }

        BigDecimal teto = departamento.getLimitePorPedido();

        if (total.compareTo(teto) > 0) {
            return ResultadoOperacao.falha("Pedido rejeitado: o valor total de R$ " + total
                    + " ultrapassa o teto de R$ " + teto + " por pedido do departamento "
                    + departamento.getNome() + " (" + departamento.getSigla() + ").");
        }

        PedidoAquisicao pedido = new PedidoAquisicao(proximoId(pedidos), solicitante);

        // Os itens sao imutaveis, entao a numeracao e feita criando novos itens.
        for (int i = 0; i < itens.size(); i++) {
            ItemPedido item = itens.get(i);
            pedido.adicionarItem(new ItemPedido(i + 1, item.getDescricao(), item.getQuantidade(),
                    item.getUnidade(), item.getValorUnitario()));
        }

        pedidos.add(pedido);

        return ResultadoOperacao.sucesso("Pedido #" + pedido.getId() + " registrado com sucesso. Valor total: R$ "
                + pedido.getValorTotal() + ".");
    }

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

        if (novoStatus == StatusPedido.APROVADO) {
            pedido.aprovar();
        } else {
            pedido.reprovar();
        }
        return ResultadoOperacao
                .sucesso("Pedido #" + pedido.getId() + " marcado como " + novoStatus.getDescricao() + ".");
    }

    // Conclui um pedido aprovado apos a entrega (Issue #2). A data e hora da
    // conclusao sao registradas automaticamente. Nao ha regra no enunciado sobre
    // quem conclui, entao adotamos a mesma regra da avaliacao: somente administrador.
    public static ResultadoOperacao concluirPedido(Usuario administrador, PedidoAquisicao pedido) {
        if (administrador == null || !administrador.isAdministrador()) {
            return ResultadoOperacao.falha("Somente um administrador pode concluir pedidos.");
        }

        if (pedido == null) {
            return ResultadoOperacao.falha("Pedido nao encontrado.");
        }

        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            return ResultadoOperacao.falha("Pedido #" + pedido.getId() + " ja esta concluido.");
        }

        if (pedido.getStatus() != StatusPedido.APROVADO) {
            return ResultadoOperacao.falha("Somente pedidos com status APROVADO podem ser concluidos. Pedido #"
                    + pedido.getId() + " esta " + pedido.getStatus().getDescricao() + ".");
        }

        pedido.concluir();
        return ResultadoOperacao.sucesso("Pedido #" + pedido.getId() + " marcado como Concluido em "
                + pedido.getDataConclusao().format(FORMATO_DATA) + ".");
    }

    // Lista os pedidos aprovados, que sao os unicos que podem ser concluidos.
    public static List<PedidoAquisicao> listarPedidosAprovados(List<PedidoAquisicao> pedidos) {
        List<PedidoAquisicao> aprovados = new ArrayList<>();

        for (PedidoAquisicao pedido : pedidos) {
            if (pedido.getStatus() == StatusPedido.APROVADO) {
                aprovados.add(pedido);
            }
        }

        return aprovados;
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

    // ---------- Buscas do administrador (Issue 7) ----------
    // As buscas consideram pedidos de todos os status e devolvem uma lista nova,
    // sem alterar os pedidos nem a lista original.

    // Converte um texto no formato dd/MM/yyyy em data. Retorna null se o texto
    // nao for uma data real nesse formato.
    public static LocalDate interpretarData(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DIA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Pedidos criados entre as duas datas, incluindo os dois dias inteiros,
    // qualquer que seja o horario de criacao.
    public static ResultadoBusca buscarPorPeriodo(List<PedidoAquisicao> pedidos, LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            return ResultadoBusca.falha("Informe a data inicial e a data final no formato dd/MM/aaaa.");
        }

        if (inicio.isAfter(fim)) {
            return ResultadoBusca.falha("A data inicial (" + inicio.format(FORMATO_DIA)
                    + ") e posterior a data final (" + fim.format(FORMATO_DIA) + ").");
        }

        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime antesDe = fim.plusDays(1).atStartOfDay();
        List<PedidoAquisicao> encontrados = new ArrayList<>();

        for (PedidoAquisicao pedido : pedidos) {
            LocalDateTime criacao = pedido.getDataCriacao();
            if (!criacao.isBefore(desde) && criacao.isBefore(antesDe)) {
                encontrados.add(pedido);
            }
        }

        return ResultadoBusca.sucesso(encontrados);
    }

    // Pedidos do solicitante com o id informado. A busca e pelo id, e nao pelo
    // nome, para nao misturar pessoas com o mesmo nome.
    public static ResultadoBusca buscarPorSolicitante(List<PedidoAquisicao> pedidos, List<Usuario> usuarios,
            int idSolicitante) {
        boolean usuarioExiste = false;
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == idSolicitante) {
                usuarioExiste = true;
                break;
            }
        }

        if (!usuarioExiste) {
            return ResultadoBusca.falha("Usuario #" + idSolicitante + " nao encontrado.");
        }

        List<PedidoAquisicao> encontrados = new ArrayList<>();

        for (PedidoAquisicao pedido : pedidos) {
            if (pedido.getSolicitante() != null && pedido.getSolicitante().getId() == idSolicitante) {
                encontrados.add(pedido);
            }
        }

        return ResultadoBusca.sucesso(encontrados);
    }

    // Pedidos com pelo menos um item cuja descricao contem o texto pesquisado,
    // sem diferenciar maiusculas e minusculas. Cada pedido aparece uma vez so.
    public static ResultadoBusca buscarPorDescricaoItem(List<PedidoAquisicao> pedidos, String pesquisa) {
        if (pesquisa == null || pesquisa.trim().isEmpty()) {
            return ResultadoBusca.falha("Informe um texto para pesquisar na descricao dos itens.");
        }

        String termo = pesquisa.trim().toLowerCase(Locale.ROOT);
        List<PedidoAquisicao> encontrados = new ArrayList<>();

        for (PedidoAquisicao pedido : pedidos) {
            for (ItemPedido item : pedido.getItens()) {
                if (item.getDescricao() != null && item.getDescricao().toLowerCase(Locale.ROOT).contains(termo)) {
                    encontrados.add(pedido);
                    break;
                }
            }
        }

        return ResultadoBusca.sucesso(encontrados);
    }

    private static PedidoAquisicao buscarPorId(List<PedidoAquisicao> pedidos, int id) {
        for (PedidoAquisicao pedido : pedidos) {
            if (pedido.getId() == id) {
                return pedido;
            }
        }
        return null;
    }

    // Proximo identificador sequencial, a partir do maior id ja existente.
    private static int proximoId(List<PedidoAquisicao> pedidos) {
        int maior = 0;

        for (PedidoAquisicao pedido : pedidos) {
            if (pedido.getId() > maior) {
                maior = pedido.getId();
            }
        }

        return maior + 1;
    }
}
