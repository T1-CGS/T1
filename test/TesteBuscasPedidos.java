import dados.CargaInicial;
import modelo.Departamento;
import modelo.ItemPedido;
import modelo.Papel;
import modelo.PedidoAquisicao;
import modelo.StatusPedido;
import modelo.Usuario;
import servico.ResultadoBusca;
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

/**
 * Testes das buscas do administrador (Issue #7). Java puro, sem dependencias:
 * termina com codigo 1 se alguma verificacao falhar. Veja o README.
 */
public class TesteBuscasPedidos {

    private static int verificacoes = 0;
    private static final List<String> falhas = new ArrayList<>();

    private static final LocalDate DIA = LocalDate.now().minusDays(20);

    private static Usuario admin;
    private static Usuario bruno;
    private static Usuario semPedidos;
    private static Usuario joaoTi;
    private static Usuario joaoRh;
    private static List<Usuario> usuarios;
    private static List<PedidoAquisicao> pedidos;

    private static PedidoAquisicao inicioDoPrimeiroDia;
    private static PedidoAquisicao fimDoUltimoDia;
    private static PedidoAquisicao antesDoIntervalo;
    private static PedidoAquisicao depoisDoIntervalo;
    private static PedidoAquisicao doAdmin;

    public static void main(String[] args) {
        prepararDados();
        String antes = retrato(pedidos);

        testarPeriodo();
        testarDatasInvalidas();
        testarSolicitante();
        testarDescricao();
        testarPreservacao(antes);
        testarMenu();

        System.out.println();
        System.out.println(verificacoes + " verificacoes, " + falhas.size() + " falha(s).");
        if (!falhas.isEmpty()) {
            for (String falha : falhas) {
                System.out.println("  FALHOU: " + falha);
            }
            System.exit(1);
        }
        System.out.println("Todos os testes passaram.");
    }

    // Dados montados pela API vigente do modelo: construtor com data de criacao
    // e transicoes aprovar/reprovar/concluir.
    private static void prepararDados() {
        Departamento ti = new Departamento(1, "Tecnologia da Informacao", "TI", new BigDecimal("50000.00"));
        Departamento rh = new Departamento(2, "Recursos Humanos", "RH", new BigDecimal("50000.00"));
        admin = new Usuario(1, "Ana Ribeiro", Papel.ADMINISTRADOR, ti);
        bruno = new Usuario(2, "Bruno Alves", Papel.FUNCIONARIO, ti);
        semPedidos = new Usuario(3, "Carla Nunes", Papel.FUNCIONARIO, ti);
        joaoTi = new Usuario(4, "Joao Silva", Papel.FUNCIONARIO, ti);
        joaoRh = new Usuario(5, "Joao Silva", Papel.FUNCIONARIO, rh);
        usuarios = List.of(admin, bruno, semPedidos, joaoTi, joaoRh);

        inicioDoPrimeiroDia = pedido(1, bruno, DIA.atStartOfDay(), "Cadeira ergonomica");
        fimDoUltimoDia = pedido(2, bruno, DIA.plusDays(2).atTime(23, 59, 59, 999_999_999), "Monitor 24 polegadas");
        fimDoUltimoDia.aprovar();
        fimDoUltimoDia.concluir();
        antesDoIntervalo = pedido(3, joaoTi, DIA.minusDays(1).atTime(23, 59, 59), "Mesa de reuniao");
        antesDoIntervalo.reprovar();
        depoisDoIntervalo = pedido(4, joaoTi, DIA.plusDays(3).atStartOfDay(), "Teclado");
        depoisDoIntervalo.aprovar();

        // Dois itens combinam com "cadeira": o pedido deve aparecer uma vez so.
        doAdmin = new PedidoAquisicao(5, admin, DIA.plusDays(1).atTime(12, 0));
        doAdmin.adicionarItem(item(1, "CADEIRA de escritorio"));
        doAdmin.adicionarItem(item(2, "Almofada para cadeira"));

        pedidos = new ArrayList<>(List.of(inicioDoPrimeiroDia, fimDoUltimoDia, antesDoIntervalo,
                depoisDoIntervalo, doAdmin));
    }

    private static void testarPeriodo() {
        secao("Busca por intervalo de datas");
        ResultadoBusca r = ServicoPedidos.buscarPorPeriodo(pedidos, DIA, DIA.plusDays(2));
        verificar(r.isSucesso(), "intervalo valido aceito");
        verificar(r.getPedidos().contains(inicioDoPrimeiroDia), "inclui pedido criado as 00:00 do primeiro dia");
        verificar(r.getPedidos().contains(fimDoUltimoDia), "inclui pedido criado as 23:59:59.999 do ultimo dia");
        verificar(!r.getPedidos().contains(antesDoIntervalo), "exclui pedido do dia anterior (23:59:59)");
        verificar(!r.getPedidos().contains(depoisDoIntervalo), "exclui pedido do dia seguinte (00:00)");
        verificar(ids(r).equals(List.of(1, 2, 5)), "resultado na ordem original: 1, 2, 5");

        ResultadoBusca umDia = ServicoPedidos.buscarPorPeriodo(pedidos, DIA.plusDays(2), DIA.plusDays(2));
        verificar(ids(umDia).equals(List.of(2)), "intervalo de um dia so pega o dia inteiro");

        ResultadoBusca todos = ServicoPedidos.buscarPorPeriodo(pedidos, DIA.minusDays(5), DIA.plusDays(5));
        boolean[] status = new boolean[StatusPedido.values().length];
        for (PedidoAquisicao pedido : todos.getPedidos()) {
            status[pedido.getStatus().ordinal()] = true;
        }
        verificar(todos.getPedidos().size() == 5 && status[0] && status[1] && status[2] && status[3],
                "busca considera pedidos de todos os status");

        ResultadoBusca vazio = ServicoPedidos.buscarPorPeriodo(pedidos, DIA.minusDays(100), DIA.minusDays(90));
        verificar(vazio.isSucesso() && vazio.getPedidos().isEmpty(), "intervalo sem pedidos retorna lista vazia");
    }

    private static void testarDatasInvalidas() {
        secao("Datas invalidas e intervalo invertido");
        verificar(LocalDate.of(2024, 2, 29).equals(ServicoPedidos.interpretarData("29/02/2024")),
                "29/02/2024 (bissexto) e valida");
        verificar(LocalDate.of(2026, 1, 5).equals(ServicoPedidos.interpretarData("  05/01/2026 ")),
                "espacos em volta sao ignorados");
        String[] invalidas = { "31/02/2026", "29/02/2025", "32/01/2026", "10/13/2026", "2026-01-10", "5/1/2026",
                "abc", "", "   " };
        for (String texto : invalidas) {
            verificar(ServicoPedidos.interpretarData(texto) == null, "\"" + texto + "\" e recusada");
        }
        verificar(ServicoPedidos.interpretarData(null) == null, "null e recusado");

        ResultadoBusca invertido = ServicoPedidos.buscarPorPeriodo(pedidos, DIA.plusDays(1), DIA);
        verificar(!invertido.isSucesso() && invertido.getMensagem().contains("posterior"),
                "intervalo invertido recusado com mensagem");
        verificar(!ServicoPedidos.buscarPorPeriodo(pedidos, null, DIA).isSucesso(), "data inicial ausente recusada");
        verificar(!ServicoPedidos.buscarPorPeriodo(pedidos, DIA, null).isSucesso(), "data final ausente recusada");
    }

    private static void testarSolicitante() {
        secao("Busca por funcionario solicitante");
        verificar(ids(ServicoPedidos.buscarPorSolicitante(pedidos, usuarios, bruno.getId())).equals(List.of(1, 2)),
                "funcionario com pedidos");
        ResultadoBusca sem = ServicoPedidos.buscarPorSolicitante(pedidos, usuarios, semPedidos.getId());
        verificar(sem.isSucesso() && sem.getPedidos().isEmpty(), "funcionario sem pedidos retorna lista vazia");
        verificar(ids(ServicoPedidos.buscarPorSolicitante(pedidos, usuarios, admin.getId())).equals(List.of(5)),
                "administrador solicitante tambem e encontrado");

        verificar(ids(ServicoPedidos.buscarPorSolicitante(pedidos, usuarios, joaoTi.getId())).equals(List.of(3, 4)),
                "homonimo: busca pelo id do Joao da TI");
        verificar(ServicoPedidos.buscarPorSolicitante(pedidos, usuarios, joaoRh.getId()).getPedidos().isEmpty(),
                "homonimo: Joao do RH nao recebe os pedidos do Joao da TI");

        ResultadoBusca inexistente = ServicoPedidos.buscarPorSolicitante(pedidos, usuarios, 999);
        verificar(!inexistente.isSucesso() && inexistente.getMensagem().contains("#999"), "id inexistente recusado");
    }

    private static void testarDescricao() {
        secao("Busca por descricao de item");
        verificar(ids(ServicoPedidos.buscarPorDescricaoItem(pedidos, "cadeira")).equals(List.of(1, 5)),
                "\"cadeira\" encontra \"Cadeira ergonomica\" e \"CADEIRA de escritorio\"");
        verificar(ids(ServicoPedidos.buscarPorDescricaoItem(pedidos, "CADEIRA")).equals(List.of(1, 5)),
                "maiusculas nao fazem diferenca");
        verificar(ids(ServicoPedidos.buscarPorDescricaoItem(pedidos, "ErGoNo")).equals(List.of(1)),
                "correspondencia parcial no meio da descricao");
        verificar(ids(ServicoPedidos.buscarPorDescricaoItem(pedidos, "  monitor  ")).equals(List.of(2)),
                "espacos em volta da pesquisa sao ignorados");

        List<Integer> doisItens = ids(ServicoPedidos.buscarPorDescricaoItem(pedidos, "cadeira"));
        verificar(doisItens.indexOf(5) == doisItens.lastIndexOf(5), "pedido com dois itens aparece uma vez so");

        verificar(!ServicoPedidos.buscarPorDescricaoItem(pedidos, "").isSucesso(), "pesquisa vazia recusada");
        verificar(!ServicoPedidos.buscarPorDescricaoItem(pedidos, "   ").isSucesso(), "pesquisa so com espacos recusada");
        verificar(!ServicoPedidos.buscarPorDescricaoItem(pedidos, null).isSucesso(), "pesquisa nula recusada");

        ResultadoBusca nada = ServicoPedidos.buscarPorDescricaoItem(pedidos, "impressora");
        verificar(nada.isSucesso() && nada.getPedidos().isEmpty(), "pesquisa sem resultado retorna lista vazia");
    }

    private static void testarPreservacao(String antes) {
        secao("Preservacao dos dados");
        verificar(retrato(pedidos).equals(antes), "pedidos, itens, status e datas iguais apos as buscas");

        ResultadoBusca r = ServicoPedidos.buscarPorPeriodo(pedidos, DIA.minusDays(5), DIA.plusDays(5));
        try {
            r.getPedidos().clear();
            verificar(false, "lista do resultado e somente leitura");
        } catch (UnsupportedOperationException e) {
            verificar(true, "lista do resultado e somente leitura");
        }
        verificar(pedidos.size() == 5, "lista original mantem os 5 pedidos");
        verificar(r.getPedidos().get(0) == pedidos.get(0), "resultado aponta para os mesmos pedidos (sem copias)");
        try {
            r.getPedidos().get(1).adicionarItem(item(9, "Extra"));
            verificar(false, "pedido concluido encontrado na busca continua bloqueado");
        } catch (IllegalStateException e) {
            verificar(true, "pedido concluido encontrado na busca continua bloqueado");
        }
    }

    private static void testarMenu() {
        secao("Menu: submenu, detalhes e retorno");
        CargaInicial.carregar();
        String antes = retrato(CargaInicial.pedidos);

        // Admin: busca por item, abre detalhes, tenta pedido fora do resultado,
        // volta ao submenu, busca por solicitante (pedido concluido), testa
        // entradas invalidas e sai pelo menu principal.
        String saida = executarMenu(CargaInicial.usuarios.get(0),
                "6", "3", "CADEIRA", "3", "99", "1", "0",
                "2", "14", "6", "0",
                "2", "999",
                "1", "31/02/2026",
                "1", "10/01/2026", "01/01/2026",
                "3", "   ",
                "3", "impressora",
                "1", "01/01/2000", "31/12/2000",
                "0", "0");

        verificar(saida.contains("Buscar pedidos:") && saida.contains("0 - Voltar"), "submenu exibido");
        verificar(saida.contains("1 pedido(s) encontrado(s)")
                && saida.contains("Pedido #3 | Hugo Martins | RH | Criado em "),
                "lista resumida com numero, solicitante, departamento e data");
        verificar(saida.contains("| Reprovado | R$ 14400.00"), "lista resumida com status e total");
        verificar(saida.contains("--- Detalhes do pedido #3 ---")
                && saida.contains("  - Cadeira ergonomica | Quantidade: 12 un | Unitario: R$ 1200.00 | Subtotal: R$ 14400.00"),
                "detalhes mostram descricao, quantidade, unidade, unitario e subtotal");
        verificar(ocorrencias(saida, "o pedido informado nao esta entre os resultados") == 2,
                "pedido inexistente e pedido fora do resultado sao recusados");
        verificar(!saida.contains("--- Detalhes do pedido #1 ---"), "detalhes de pedido fora do resultado nao aparecem");
        verificar(saida.contains("--- Detalhes do pedido #6 ---") && saida.contains("Concluido em : "),
                "detalhes mostram a data de conclusao quando existe");
        verificar(saida.contains("Erro: Usuario #999 nao encontrado."), "id de solicitante inexistente");
        verificar(saida.contains("\"31/02/2026\" nao e uma data valida"), "data invalida no menu");
        verificar(saida.contains("e posterior a data final"), "intervalo invertido no menu");
        verificar(saida.contains("Erro: Informe um texto para pesquisar"), "pesquisa vazia no menu");
        verificar(ocorrencias(saida, "Nenhum pedido encontrado.") == 2, "ausencia de resultados informada");
        verificar(saida.contains("Fechando o sistema"), "volta ao menu principal e sai normalmente");
        verificar(retrato(CargaInicial.pedidos).equals(antes), "dados da carga inicial preservados apos as buscas");

        String funcionario = executarMenu(CargaInicial.usuarios.get(1), "6", "0");
        verificar(!funcionario.contains("Buscar pedidos:") && funcionario.contains("Opcao invalida."),
                "funcionario nao acessa as buscas");
    }

    // ---------- Auxiliares ----------

    private static String executarMenu(Usuario usuario, String... linhas) {
        InputStream entradaOriginal = System.in;
        PrintStream saidaOriginal = System.out;
        ByteArrayOutputStream capturada = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream((String.join("\n", linhas) + "\n").getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(capturada, true, StandardCharsets.UTF_8));
            new MenuPrincipal(new Sessao(usuario)).executar();
        } catch (RuntimeException e) {
            System.setOut(saidaOriginal);
            verificar(false, "menu terminou com excecao: " + e);
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(saidaOriginal);
        }
        return new String(capturada.toByteArray(), StandardCharsets.UTF_8);
    }

    private static PedidoAquisicao pedido(int id, Usuario solicitante, LocalDateTime criacao, String descricao) {
        PedidoAquisicao pedido = new PedidoAquisicao(id, solicitante, criacao);
        pedido.adicionarItem(item(1, descricao));
        return pedido;
    }

    private static ItemPedido item(int id, String descricao) {
        return new ItemPedido(id, descricao, 2, "un", new BigDecimal("100.00"));
    }

    private static List<Integer> ids(ResultadoBusca resultado) {
        List<Integer> ids = new ArrayList<>();
        for (PedidoAquisicao pedido : resultado.getPedidos()) {
            ids.add(pedido.getId());
        }
        return ids;
    }

    // Texto com tudo o que importa de cada pedido, para comparar antes e depois.
    private static String retrato(List<PedidoAquisicao> lista) {
        StringBuilder texto = new StringBuilder();
        for (PedidoAquisicao pedido : lista) {
            texto.append(pedido).append('|').append(pedido.getDataCriacao()).append('|')
                    .append(pedido.getDataConclusao()).append('\n');
            for (ItemPedido item : pedido.getItens()) {
                texto.append("  ").append(item.getId()).append(' ').append(item).append('\n');
            }
        }
        return texto.toString();
    }

    private static int ocorrencias(String texto, String trecho) {
        int total = 0;
        int posicao = texto.indexOf(trecho);
        while (posicao >= 0) {
            total++;
            posicao = texto.indexOf(trecho, posicao + trecho.length());
        }
        return total;
    }

    private static void secao(String nome) {
        System.out.println("\n[" + nome + "]");
    }

    private static void verificar(boolean condicao, String descricao) {
        verificacoes++;
        System.out.println((condicao ? "  ok    " : "  FALHA ") + descricao);
        if (!condicao) {
            falhas.add(descricao);
        }
    }
}
