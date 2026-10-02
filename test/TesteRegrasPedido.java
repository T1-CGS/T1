import dados.CargaInicial;
import modelo.Departamento;
import modelo.ItemPedido;
import modelo.Papel;
import modelo.PedidoAquisicao;
import modelo.StatusPedido;
import modelo.Usuario;
import servico.ResultadoOperacao;
import servico.ServicoPedidos;
import sessao.Sessao;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Testes das regras de ciclo de vida e bloqueio dos pedidos (Issue #2) e de
 * regressao das funcionalidades ja existentes. Java puro, sem dependencias:
 * termina com codigo 1 se alguma verificacao falhar. Veja o README.
 */
public class TesteRegrasPedido {

    private static int verificacoes = 0;
    private static final List<String> falhas = new ArrayList<>();

    private static Departamento ti;
    private static Usuario admin;
    private static Usuario funcionario;
    private static Usuario outroFuncionario;

    public static void main(String[] args) {
        ti = new Departamento(1, "Tecnologia da Informacao", "TI", new BigDecimal("1000.00"));
        admin = new Usuario(1, "Ana Ribeiro", Papel.ADMINISTRADOR, ti);
        funcionario = new Usuario(2, "Bruno Alves", Papel.FUNCIONARIO, ti);
        outroFuncionario = new Usuario(3, "Carla Nunes", Papel.FUNCIONARIO, ti);

        testarConclusaoValida();
        testarConclusaoInvalida();
        testarAutorizacaoDaConclusao();
        testarReabertura();
        testarAlteracaoDeItensPelaLista();
        testarAlteracaoPorReferenciasExternas();
        testarAlteracaoDeDadosDoPedidoBloqueado();
        testarRegistroELimite();
        testarExclusao();
        testarAprovacaoEReprovacao();
        testarTrocaDeUsuario();
        testarCargaInicial();

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

    // ---------- Conclusao ----------

    private static void testarConclusaoValida() {
        secao("Conclusao valida");
        PedidoAquisicao pedido = pedidoAprovado(10);
        LocalDateTime antes = LocalDateTime.now();

        ResultadoOperacao resultado = ServicoPedidos.concluirPedido(admin, pedido);
        LocalDateTime depois = LocalDateTime.now();

        verificar(resultado.isSucesso(), "admin conclui pedido aprovado");
        verificar(pedido.getStatus() == StatusPedido.CONCLUIDO, "status passa a CONCLUIDO");
        verificar(pedido.getDataConclusao() != null, "data de conclusao registrada");
        verificar(!pedido.getDataConclusao().isBefore(antes) && !pedido.getDataConclusao().isAfter(depois),
                "data de conclusao e o momento da operacao");
        verificar(!pedido.getDataConclusao().isBefore(pedido.getDataCriacao()),
                "data de conclusao nao e anterior a criacao");
        verificar(resultado.getMensagem().contains("Concluido"), "mensagem de sucesso clara");
    }

    private static void testarConclusaoInvalida() {
        secao("Conclusao invalida");
        PedidoAquisicao aberto = pedidoAberto(20);
        ResultadoOperacao r1 = ServicoPedidos.concluirPedido(admin, aberto);
        verificar(!r1.isSucesso(), "nao conclui pedido aberto");
        verificar(aberto.getStatus() == StatusPedido.ABERTO && aberto.getDataConclusao() == null,
                "pedido aberto continua aberto e sem data de conclusao");

        PedidoAquisicao reprovado = pedidoAberto(21);
        reprovado.reprovar();
        ResultadoOperacao r2 = ServicoPedidos.concluirPedido(admin, reprovado);
        verificar(!r2.isSucesso(), "nao conclui pedido reprovado");
        verificar(reprovado.getStatus() == StatusPedido.REPROVADO && reprovado.getDataConclusao() == null,
                "pedido reprovado continua reprovado e sem data de conclusao");

        PedidoAquisicao concluido = pedidoAprovado(22);
        ServicoPedidos.concluirPedido(admin, concluido);
        LocalDateTime dataOriginal = concluido.getDataConclusao();
        ResultadoOperacao r3 = ServicoPedidos.concluirPedido(admin, concluido);
        verificar(!r3.isSucesso(), "nao conclui pedido ja concluido");
        verificar(r3.getMensagem().contains("ja esta concluido"), "mensagem explica que ja esta concluido");
        verificar(dataOriginal.equals(concluido.getDataConclusao()), "data de conclusao original preservada");

        verificar(!ServicoPedidos.concluirPedido(admin, null).isSucesso(), "pedido inexistente e recusado");

        verificarLanca(IllegalStateException.class, () -> pedidoAberto(23).concluir(),
                "modelo recusa concluir pedido aberto");
    }

    private static void testarAutorizacaoDaConclusao() {
        secao("Autorizacao da conclusao");
        PedidoAquisicao pedido = pedidoAprovado(30);

        ResultadoOperacao r1 = ServicoPedidos.concluirPedido(funcionario, pedido);
        verificar(!r1.isSucesso(), "funcionario nao pode concluir");
        verificar(r1.getMensagem().contains("administrador"), "mensagem cita a regra de autorizacao");
        verificar(!ServicoPedidos.concluirPedido(null, pedido).isSucesso(), "sem usuario nao pode concluir");
        verificar(pedido.getStatus() == StatusPedido.APROVADO && pedido.getDataConclusao() == null,
                "pedido continua aprovado depois das tentativas negadas");

        List<PedidoAquisicao> pedidos = new ArrayList<>();
        pedidos.add(pedidoAberto(31));
        pedidos.add(pedido);
        List<PedidoAquisicao> elegiveis = ServicoPedidos.listarPedidosAprovados(pedidos);
        verificar(elegiveis.size() == 1 && elegiveis.get(0) == pedido, "lista de elegiveis contem so aprovados");
    }

    // ---------- Bloqueio ----------

    private static void testarReabertura() {
        secao("Tentativa de reabertura / reavaliacao");
        PedidoAquisicao aprovado = pedidoAprovado(40);
        PedidoAquisicao reprovado = pedidoAberto(41);
        reprovado.reprovar();
        PedidoAquisicao concluido = pedidoAprovado(42);
        concluido.concluir();

        for (PedidoAquisicao pedido : new PedidoAquisicao[] { aprovado, reprovado, concluido }) {
            StatusPedido statusAntes = pedido.getStatus();
            String nome = statusAntes.getDescricao();
            verificar(!ServicoPedidos.aprovarPedido(admin, pedido).isSucesso(), nome + ": servico recusa aprovar");
            verificar(!ServicoPedidos.reprovarPedido(admin, pedido).isSucesso(), nome + ": servico recusa reprovar");
            verificarLanca(IllegalStateException.class, pedido::aprovar, nome + ": modelo recusa aprovar");
            verificarLanca(IllegalStateException.class, pedido::reprovar, nome + ": modelo recusa reprovar");
            verificar(pedido.getStatus() == statusAntes, nome + ": status inalterado");
            verificar(!pedido.isEditavel(), nome + ": pedido nao editavel");
        }

        verificar(!possuiMetodoPublico(PedidoAquisicao.class, "setStatus"),
                "nao existe setStatus publico para reabrir o pedido");
        verificar(!possuiMetodoPublico(PedidoAquisicao.class, "setDataConclusao"),
                "nao existe setDataConclusao publico");
    }

    private static void testarAlteracaoDeItensPelaLista() {
        secao("Alteracao de itens pela lista retornada");
        PedidoAquisicao aprovado = pedidoAprovado(50);
        BigDecimal totalAntes = aprovado.getValorTotal();

        verificarLanca(UnsupportedOperationException.class,
                () -> aprovado.getItens().add(item(9, "Extra", 1, "10.00")), "getItens().add recusado");
        verificarLanca(UnsupportedOperationException.class, () -> aprovado.getItens().clear(),
                "getItens().clear recusado");
        verificarLanca(UnsupportedOperationException.class, () -> aprovado.getItens().remove(0),
                "getItens().remove recusado");
        verificarLanca(IllegalStateException.class, () -> aprovado.adicionarItem(item(9, "Extra", 1, "10.00")),
                "adicionarItem recusado em pedido aprovado");
        verificarLanca(IllegalStateException.class, () -> aprovado.removerItem(1),
                "removerItem recusado em pedido aprovado");
        verificar(aprovado.getItens().size() == 1 && aprovado.getValorTotal().equals(totalAntes),
                "itens e total inalterados");

        PedidoAquisicao aberto = pedidoAberto(51);
        aberto.adicionarItem(item(2, "Teclado", 1, "50.00"));
        verificar(aberto.getItens().size() == 2, "pedido aberto ainda aceita novos itens");
        verificar(aberto.removerItem(2) && aberto.getItens().size() == 1, "pedido aberto ainda permite remover itens");
    }

    private static void testarAlteracaoPorReferenciasExternas() {
        secao("Alteracao por referencias externas aos itens");
        verificar(!possuiSetter(ItemPedido.class), "ItemPedido nao tem setters publicos");

        List<PedidoAquisicao> pedidos = new ArrayList<>();
        List<ItemPedido> itensDoSolicitante = new ArrayList<>();
        ItemPedido itemExterno = item(0, "Monitor", 2, "300.00");
        itensDoSolicitante.add(itemExterno);

        ResultadoOperacao registro = ServicoPedidos.registrarPedido(pedidos, funcionario, itensDoSolicitante);
        verificar(registro.isSucesso(), "registro com lista do solicitante");
        PedidoAquisicao pedido = pedidos.get(0);
        ServicoPedidos.aprovarPedido(admin, pedido);

        itensDoSolicitante.add(item(0, "Monitor extra", 5, "300.00"));
        itensDoSolicitante.clear();
        verificar(pedido.getItens().size() == 1, "alterar a lista original nao altera o pedido");
        verificar(pedido.getValorTotal().compareTo(new BigDecimal("600.00")) == 0, "total do pedido preservado");

        ItemPedido itemDoPedido = pedido.getItens().get(0);
        verificar(itemDoPedido.getDescricao().equals("Monitor") && itemDoPedido.getQuantidade() == 2,
                "item do pedido mantem os dados registrados");
        verificar(itemDoPedido.getId() == 1 && itemExterno.getId() == 0,
                "numeracao dos itens nao altera o objeto do solicitante");
    }

    private static void testarAlteracaoDeDadosDoPedidoBloqueado() {
        secao("Alteracao de dados do pedido bloqueado");
        PedidoAquisicao aprovado = pedidoAprovado(60);
        verificarLanca(IllegalStateException.class, () -> aprovado.setId(999), "setId recusado");
        verificarLanca(IllegalStateException.class, () -> aprovado.setSolicitante(outroFuncionario),
                "setSolicitante recusado");
        verificar(aprovado.getId() == 60 && aprovado.getSolicitante() == funcionario, "dados inalterados");

        verificarLanca(IllegalArgumentException.class,
                () -> new PedidoAquisicao(61, funcionario, LocalDateTime.now().plusDays(1)),
                "data de criacao no futuro e recusada");
    }

    // ---------- Regressao ----------

    private static void testarRegistroELimite() {
        secao("Regressao: registro e limite do departamento");
        List<PedidoAquisicao> pedidos = new ArrayList<>();
        pedidos.add(pedidoAberto(7));

        List<ItemPedido> dentroDoTeto = new ArrayList<>();
        dentroDoTeto.add(item(0, "Cabo HDMI", 10, "100.00"));
        ResultadoOperacao r1 = ServicoPedidos.registrarPedido(pedidos, funcionario, dentroDoTeto);
        verificar(r1.isSucesso(), "pedido igual ao teto e aceito");
        PedidoAquisicao novo = pedidos.get(1);
        verificar(novo.getId() == 8, "id sequencial a partir do maior existente");
        verificar(novo.getStatus() == StatusPedido.ABERTO && novo.getDataConclusao() == null,
                "novo pedido aberto e sem data de conclusao");
        verificar(novo.getDepartamento() == ti, "departamento vem do solicitante");

        List<ItemPedido> acimaDoTeto = new ArrayList<>();
        acimaDoTeto.add(item(0, "Servidor", 1, "1000.01"));
        ResultadoOperacao r2 = ServicoPedidos.registrarPedido(pedidos, funcionario, acimaDoTeto);
        verificar(!r2.isSucesso() && r2.getMensagem().contains("ultrapassa o teto"), "pedido acima do teto recusado");

        List<ItemPedido> invalido = new ArrayList<>();
        invalido.add(item(0, "Negativo", -1, "10.00"));
        verificar(!ServicoPedidos.registrarPedido(pedidos, funcionario, invalido).isSucesso(),
                "quantidade invalida recusada");
        verificar(!ServicoPedidos.registrarPedido(pedidos, funcionario, new ArrayList<>()).isSucesso(),
                "pedido sem itens recusado");
        verificar(pedidos.size() == 2, "pedidos recusados nao entram na lista");
    }

    private static void testarExclusao() {
        secao("Regressao: exclusao restrita ao criador");
        List<PedidoAquisicao> pedidos = new ArrayList<>();
        pedidos.add(pedidoAberto(1));
        pedidos.add(pedidoAprovado(2));

        verificar(!ServicoPedidos.excluirPedido(pedidos, outroFuncionario, 1).isSucesso(),
                "outro usuario nao exclui");
        verificar(!ServicoPedidos.excluirPedido(pedidos, admin, 1).isSucesso(), "admin nao exclui pedido alheio");
        verificar(!ServicoPedidos.excluirPedido(pedidos, funcionario, 2).isSucesso(),
                "criador nao exclui pedido aprovado");
        verificar(!ServicoPedidos.excluirPedido(pedidos, funcionario, 99).isSucesso(), "pedido inexistente");
        verificar(ServicoPedidos.excluirPedido(pedidos, funcionario, 1).isSucesso(), "criador exclui pedido aberto");
        verificar(pedidos.size() == 1 && pedidos.get(0).getId() == 2, "somente o pedido aberto foi removido");
    }

    private static void testarAprovacaoEReprovacao() {
        secao("Regressao: aprovacao e reprovacao");
        PedidoAquisicao p1 = pedidoAberto(70);
        verificar(!ServicoPedidos.aprovarPedido(funcionario, p1).isSucesso(), "funcionario nao aprova");
        verificar(p1.getStatus() == StatusPedido.ABERTO, "pedido continua aberto");
        verificar(ServicoPedidos.aprovarPedido(admin, p1).isSucesso(), "admin aprova");
        verificar(p1.getStatus() == StatusPedido.APROVADO && p1.getDataConclusao() == null,
                "aprovado sem data de conclusao");

        PedidoAquisicao p2 = pedidoAberto(71);
        verificar(ServicoPedidos.reprovarPedido(admin, p2).isSucesso(), "admin reprova");
        verificar(p2.getStatus() == StatusPedido.REPROVADO && p2.getDataConclusao() == null,
                "reprovado sem data de conclusao");

        List<PedidoAquisicao> pedidos = new ArrayList<>();
        pedidos.add(p1);
        pedidos.add(p2);
        pedidos.add(pedidoAberto(72));
        verificar(ServicoPedidos.listarPedidosAbertos(pedidos).size() == 1, "lista de abertos correta");
    }

    private static void testarTrocaDeUsuario() {
        secao("Regressao: troca de usuario");
        Sessao sessao = new Sessao(admin);
        verificar(sessao.isAdministrador(), "sessao inicia com administrador");
        sessao.trocarUsuario(funcionario);
        verificar(sessao.getUsuarioAtual() == funcionario && !sessao.isAdministrador(), "troca para funcionario");
        verificar(sessao.descricaoUsuarioAtual().equals("#2 Bruno Alves (BA) - Funcionario - TI"),
                "descricao mostra id, nome, iniciais, papel e departamento");
    }

    private static void testarCargaInicial() {
        secao("Carga inicial");
        CargaInicial.carregar();
        CargaInicial.carregar();

        int funcionarios = 0;
        int administradores = 0;
        for (Usuario usuario : CargaInicial.usuarios) {
            if (usuario.isAdministrador()) {
                administradores++;
            } else {
                funcionarios++;
            }
        }
        verificar(CargaInicial.departamentos.size() == 5, "5 departamentos");
        verificar(funcionarios == 15, "15 funcionarios");
        verificar(administradores == 5, "5 administradores");
        verificar(CargaInicial.pedidos.size() == 6, "6 pedidos (carga repetida nao duplica)");

        boolean[] statusPresentes = new boolean[StatusPedido.values().length];
        boolean datasCoerentes = true;
        for (PedidoAquisicao pedido : CargaInicial.pedidos) {
            statusPresentes[pedido.getStatus().ordinal()] = true;
            boolean concluido = pedido.getStatus() == StatusPedido.CONCLUIDO;
            if (concluido != (pedido.getDataConclusao() != null)) {
                datasCoerentes = false;
            }
            if (concluido && pedido.getDataConclusao().isBefore(pedido.getDataCriacao())) {
                datasCoerentes = false;
            }
            if (pedido.getDataCriacao().isAfter(LocalDateTime.now())) {
                datasCoerentes = false;
            }
        }
        for (StatusPedido status : StatusPedido.values()) {
            verificar(statusPresentes[status.ordinal()], "existe exemplo com status " + status.getDescricao());
        }
        verificar(datasCoerentes, "so pedidos concluidos tem data de conclusao, sempre apos a criacao");
        verificar(ServicoPedidos.listarPedidosAprovados(CargaInicial.pedidos).size() == 2,
                "carga tem 2 pedidos aprovados prontos para conclusao");

        PedidoAquisicao concluido = CargaInicial.pedidos.get(5);
        verificarLanca(IllegalStateException.class, () -> concluido.adicionarItem(item(9, "X", 1, "1.00")),
                "pedido concluido da carga continua bloqueado");
    }

    // ---------- Auxiliares ----------

    private static ItemPedido item(int id, String descricao, int quantidade, String valor) {
        return new ItemPedido(id, descricao, quantidade, "un", new BigDecimal(valor));
    }

    private static PedidoAquisicao pedidoAberto(int id) {
        PedidoAquisicao pedido = new PedidoAquisicao(id, funcionario, LocalDateTime.now().minusDays(1));
        pedido.adicionarItem(item(1, "Mouse", 2, "50.00"));
        return pedido;
    }

    private static PedidoAquisicao pedidoAprovado(int id) {
        PedidoAquisicao pedido = pedidoAberto(id);
        pedido.aprovar();
        return pedido;
    }

    private static boolean possuiMetodoPublico(Class<?> classe, String nome) {
        for (Method metodo : classe.getMethods()) {
            if (metodo.getName().equals(nome)) {
                return true;
            }
        }
        return false;
    }

    private static boolean possuiSetter(Class<?> classe) {
        for (Method metodo : classe.getDeclaredMethods()) {
            if (Modifier.isPublic(metodo.getModifiers()) && metodo.getName().startsWith("set")) {
                return true;
            }
        }
        return false;
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

    private static void verificarLanca(Class<? extends Throwable> esperada, Runnable acao, String descricao) {
        try {
            acao.run();
            verificar(false, descricao + " (nenhuma excecao lancada)");
        } catch (Throwable e) {
            verificar(esperada.isInstance(e), descricao
                    + (esperada.isInstance(e) ? "" : " (lancou " + e.getClass().getSimpleName() + ")"));
        }
    }
}
