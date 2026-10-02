import dados.CargaInicial;
import modelo.Departamento;
import modelo.ItemPedido;
import modelo.Papel;
import modelo.PedidoAquisicao;
import modelo.Usuario;
import servico.EstatisticasPedidos;
import servico.ServicoPedidos;
import sessao.Sessao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Testes das estatisticas, dos limites de datas e do menu real (Issue #8). */
public class TesteEstatisticasPedidos {
    private static int verificacoes;
    private static final LocalDateTime REFERENCIA = LocalDate.now().minusDays(1).atTime(12, 0);
    private static final Departamento DEPARTAMENTO = new Departamento(1, "Teste", "TST",
            new BigDecimal("100000.00"));
    private static final Usuario ADMIN = new Usuario(1, "Admin Teste", Papel.ADMINISTRADOR, DEPARTAMENTO);
    private static final Usuario FUNCIONARIO = new Usuario(2, "Funcionario Teste", Papel.FUNCIONARIO, DEPARTAMENTO);

    public static void main(String[] args) {
        testarSemPedidos();
        testarStatusEMaiorPedido();
        testarLimitesDoPeriodo();
        testarArredondamento();
        testarSemAbertosEEmpate();
        testarPreservacao();
        testarMenu();
        System.out.println(verificacoes + " verificacoes, 0 falhas. Todos os testes passaram.");
    }

    private static void testarSemPedidos() {
        EstatisticasPedidos r = ServicoPedidos.calcularEstatisticas(List.of(), REFERENCIA);
        verificar(r.getTotal() == 0 && r.getPedidosRecentes() == 0, "lista vazia tem contagens zero");
        verificar(r.getPercentualAprovados().equals(new BigDecimal("0.00"))
                && r.getPercentualReprovados().equals(new BigDecimal("0.00")), "lista vazia nao divide por zero");
        verificar(r.getValorMedioRecente().equals(new BigDecimal("0.00")), "lista vazia tem media zero");
        verificar(r.getMaiorPedidoAberto() == null, "lista vazia nao tem maior pedido aberto");
        verificar(r.getInicioPeriodo().equals(REFERENCIA.toLocalDate().minusDays(29))
                && r.getFimPeriodo().equals(REFERENCIA.toLocalDate()), "periodo tem 30 datas incluindo hoje");
        recusar(() -> ServicoPedidos.calcularEstatisticas(null, REFERENCIA), "lista ausente recusada");
        recusar(() -> ServicoPedidos.calcularEstatisticas(List.of(), null), "referencia ausente recusada");
    }

    private static void testarStatusEMaiorPedido() {
        PedidoAquisicao aberto = pedido(1, REFERENCIA, "10.00");
        PedidoAquisicao aprovado = pedido(2, REFERENCIA, "20.00");
        aprovado.aprovar();
        PedidoAquisicao reprovado = pedido(3, REFERENCIA, "30.00");
        reprovado.reprovar();
        PedidoAquisicao concluido = pedido(4, REFERENCIA, "900.00");
        concluido.aprovar();
        concluido.concluir();
        EstatisticasPedidos r = ServicoPedidos.calcularEstatisticas(List.of(aberto, aprovado, reprovado, concluido),
                REFERENCIA);
        verificar(r.getTotal() == 4, "total inclui todos os status");
        verificar(r.getAbertos() == 1 && r.getAprovados() == 1 && r.getReprovados() == 1
                && r.getConcluidos() == 1, "contagens usam o status atual, sem duplicar concluidos");
        verificar(r.getPercentualAbertos().equals(new BigDecimal("25.00"))
                && r.getPercentualAprovados().equals(new BigDecimal("25.00"))
                && r.getPercentualReprovados().equals(new BigDecimal("25.00"))
                && r.getPercentualConcluidos().equals(new BigDecimal("25.00")), "percentuais usam total como base");
        verificar(r.getPedidosRecentes() == 4 && r.getValorMedioRecente().equals(new BigDecimal("240.00")),
                "media inclui pedidos de todos os status");
        verificar(r.getMaiorPedidoAberto() == aberto, "pedido fechado de maior valor nao entra no destaque");
    }

    private static void testarLimitesDoPeriodo() {
        LocalDateTime inicio = REFERENCIA.toLocalDate().minusDays(29).atStartOfDay();
        PedidoAquisicao primeiro = pedido(1, inicio, "10.00");
        PedidoAquisicao anterior = pedido(2, inicio.minusNanos(1), "900.00");
        PedidoAquisicao ultimo = pedido(3, REFERENCIA, "20.00");
        PedidoAquisicao posterior = pedido(4, REFERENCIA.plusNanos(1), "700.00");
        PedidoAquisicao meio = pedido(5, REFERENCIA.minusDays(1), "30.00");
        EstatisticasPedidos r = ServicoPedidos.calcularEstatisticas(List.of(primeiro, anterior, ultimo, posterior, meio),
                REFERENCIA);
        verificar(r.getPedidosRecentes() == 3, "inclui inicio e referencia exatos, exclui antes e depois");
        verificar(r.getValorMedioRecente().equals(new BigDecimal("20.00")), "media usa somente pedidos do periodo");
        verificar(r.getTotal() == 5, "total geral continua incluindo pedidos fora do periodo");
        EstatisticasPedidos antigo = ServicoPedidos.calcularEstatisticas(List.of(anterior), REFERENCIA);
        verificar(antigo.getPedidosRecentes() == 0 && antigo.getValorMedioRecente().equals(new BigDecimal("0.00")),
                "sem pedidos recentes a media e zero");
    }

    private static void testarArredondamento() {
        PedidoAquisicao p1 = pedido(1, REFERENCIA, "1.00");
        PedidoAquisicao p2 = pedido(2, REFERENCIA, "1.01");
        EstatisticasPedidos r = ServicoPedidos.calcularEstatisticas(List.of(p1, p2), REFERENCIA);
        verificar(r.getValorMedioRecente().equals(new BigDecimal("1.01")), "media arredonda meio centavo para cima");
        p1.aprovar();
        PedidoAquisicao p3 = pedido(3, REFERENCIA, "2.00");
        r = ServicoPedidos.calcularEstatisticas(List.of(p1, p2, p3), REFERENCIA);
        verificar(r.getPercentualAprovados().equals(new BigDecimal("33.33")), "percentual fracionario tem duas casas");

        PedidoAquisicao variosItens = pedido(4, REFERENCIA, "10.00");
        variosItens.adicionarItem(new ItemPedido(2, "Segundo item", 3, "un", new BigDecimal("5.00")));
        r = ServicoPedidos.calcularEstatisticas(List.of(variosItens, pedido(5, REFERENCIA, "5.00")), REFERENCIA);
        verificar(r.getValorMedioRecente().equals(new BigDecimal("15.00")), "media e por pedido e soma quantidades e itens");
    }

    private static void testarSemAbertosEEmpate() {
        PedidoAquisicao fechado = pedido(1, REFERENCIA, "1000.00");
        fechado.aprovar();
        verificar(ServicoPedidos.calcularEstatisticas(List.of(fechado), REFERENCIA).getMaiorPedidoAberto() == null,
                "lista sem abertos nao destaca aprovado");
        PedidoAquisicao maiorId = pedido(12, REFERENCIA, "30.00");
        PedidoAquisicao menorId = pedido(3, REFERENCIA, "30.00");
        PedidoAquisicao menorValor = pedido(2, REFERENCIA, "20.00");
        verificar(ServicoPedidos.calcularEstatisticas(List.of(maiorId, menorValor, menorId, fechado), REFERENCIA)
                .getMaiorPedidoAberto() == menorId, "empate escolhe menor numero independentemente da ordem");
        PedidoAquisicao semItens = new PedidoAquisicao(8, FUNCIONARIO, REFERENCIA);
        verificar(ServicoPedidos.calcularEstatisticas(List.of(semItens), REFERENCIA).getMaiorPedidoAberto() == semItens,
                "pedido aberto de valor zero tambem e encontrado");
    }

    private static void testarPreservacao() {
        List<PedidoAquisicao> pedidos = new ArrayList<>(List.of(pedido(5, REFERENCIA, "10.00"),
                pedido(2, REFERENCIA.minusDays(40), "20.00")));
        String antes = retrato(pedidos);
        ServicoPedidos.calcularEstatisticas(pedidos, REFERENCIA);
        verificar(antes.equals(retrato(pedidos)), "consulta preserva ordem, itens, status e datas");
    }

    private static void testarMenu() {
        CargaInicial.carregar();
        String antes = retrato(CargaInicial.pedidos);
        String saida = executarMenu(ADMIN, "7\n0\n");
        verificar(saida.contains("--- Estatisticas gerais ---") && saida.contains("Total de pedidos: 6"),
                "opcao 7 abre estatisticas reais");
        verificar(saida.contains("Aprovados: 2 (33,33%)") && saida.contains("Reprovados: 1 (16,67%)"),
                "menu mostra contagens e percentuais da carga");
        verificar(saida.contains("Pedidos criados no periodo: 6") && saida.contains("6.528,33"),
                "menu mostra quantidade e media em reais");
        verificar(saida.contains("Detalhes do pedido #4") && saida.contains("Kit de ferramentas industriais")
                && saida.contains("Quantidade: 5"), "menu exibe detalhes do maior pedido aberto");
        verificar(!saida.contains("em desenvolvimento") && saida.contains("Fechando o sistema"),
                "menu retorna e encerra normalmente sem placeholder");
        verificar(antes.equals(retrato(CargaInicial.pedidos)), "menu nao modifica a carga");
        String funcionario = executarMenu(FUNCIONARIO, "7\n0\n");
        verificar(!funcionario.contains("--- Estatisticas gerais ---") && funcionario.contains("Opcao invalida"),
                "funcionario nao acessa estatisticas");

        List<PedidoAquisicao> salvos = new ArrayList<>(CargaInicial.pedidos);
        try {
            CargaInicial.pedidos.clear();
            String vazio = executarMenu(ADMIN, "7\n0\n");
            verificar(vazio.contains("Total de pedidos: 0") && vazio.contains("Nao ha pedidos abertos."),
                    "menu trata lista vazia e ausencia de pedido aberto");
        } finally {
            CargaInicial.pedidos.addAll(salvos);
        }
    }

    private static PedidoAquisicao pedido(int id, LocalDateTime criacao, String valor) {
        PedidoAquisicao p = new PedidoAquisicao(id, FUNCIONARIO, criacao);
        p.adicionarItem(new ItemPedido(1, "Item " + id, 1, "un", new BigDecimal(valor)));
        return p;
    }

    private static String retrato(List<PedidoAquisicao> pedidos) {
        StringBuilder texto = new StringBuilder();
        for (PedidoAquisicao pedido : pedidos) {
            texto.append(pedido).append(pedido.getDataCriacao()).append(pedido.getDataConclusao())
                    .append(pedido.getItens()).append('\n');
        }
        return texto.toString();
    }

    private static String executarMenu(Usuario usuario, String linhas) {
        InputStream entrada = System.in;
        PrintStream saida = System.out;
        ByteArrayOutputStream capturada = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(linhas.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(capturada, true, StandardCharsets.UTF_8));
            new MenuPrincipal(new Sessao(usuario)).executar();
        } finally {
            System.setIn(entrada);
            System.setOut(saida);
        }
        return new String(capturada.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void recusar(Runnable acao, String descricao) {
        try {
            acao.run();
        } catch (IllegalArgumentException esperado) {
            verificar(true, descricao);
            return;
        }
        verificar(false, descricao);
    }

    private static void verificar(boolean condicao, String descricao) {
        verificacoes++;
        if (!condicao) {
            throw new AssertionError(descricao);
        }
        System.out.println("OK: " + descricao);
    }
}
