import dados.CargaInicial;
import modelo.ItemPedido;
import modelo.PedidoAquisicao;
import modelo.Usuario;
import servico.ResultadoBusca;
import servico.ResultadoOperacao;
import servico.ServicoPedidos;
import sessao.Sessao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//A opcao "Ver estatisticas" depende da issue 8 (do Membro 4), que ainda nao foi mergeada, então aparece no menu mas com aviso de "em desenvolvimento".
public class MenuPrincipal {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner entrada;
    private final Sessao sessao;

    public MenuPrincipal(Sessao sessao) {
        this.entrada = new Scanner(System.in);
        this.sessao = sessao;
    }

    public static void main(String[] args) {
        CargaInicial.carregar();

        //Usuário inicial (Só pra não ter que ficar criando um a cada teste)
        Usuario usuarioInicial = CargaInicial.usuarios.get(0);
        Sessao sessao = new Sessao(usuarioInicial);

        MenuPrincipal menu = new MenuPrincipal(sessao);
        menu.executar();
    }

    public void executar() {
        boolean continuar = true;

        while (continuar) {
            exibirCabecalho();

            if (sessao.isAdministrador()) {
                continuar = exibirMenuAdministrador();
            } else {
                continuar = exibirMenuFuncionario();
            }
        }

        System.out.println("\nFechando o sistema. Ate mais! :P");
        entrada.close();
    }

    private void exibirCabecalho() {
        System.out.println("\n==================================================");
        System.out.println("Operador atual: " + sessao.descricaoUsuarioAtual());
        System.out.println("==================================================");
    }

    //Menu (Funcionário)
    private boolean exibirMenuFuncionario() {
        System.out.println("1 - Ver meus pedidos");
        System.out.println("2 - Registrar novo pedido");
        System.out.println("3 - Excluir pedido aberto");
        System.out.println("4 - Trocar usuario");
        System.out.println("0 - Sair");
        System.out.print("Escolha uma opcao: ");

        int opcao = lerOpcao();

        switch (opcao) {
            case 1:
                verMeusPedidos();
                break;

            case 2:
                registrarNovoPedido();
                break;

            case 3:
                excluirPedidoAberto();
                break;

            case 4:
                trocarUsuario();
                break;

            case 0:
                return false;

            default:
                System.out.println("Opcao invalida.");
        }

        return true;
    }

    //Menu (Administrador)
    private boolean exibirMenuAdministrador() {
        System.out.println("1 - Ver meus pedidos");
        System.out.println("2 - Registrar novo pedido");
        System.out.println("3 - Excluir pedido aberto");
        System.out.println("4 - Avaliar pedidos abertos (aprovar/reprovar)");
        System.out.println("5 - Concluir pedido entregue");
        System.out.println("6 - Buscar pedidos");
        System.out.println("7 - Ver estatisticas gerais");
        System.out.println("8 - Trocar usuario");
        System.out.println("0 - Sair");
        System.out.print("Escolha uma opcao: ");

        int opcao = lerOpcao();

        switch (opcao) {
            case 1:
                verMeusPedidos();
                break;

            case 2:
                registrarNovoPedido();
                break;

            case 3:
                excluirPedidoAberto();
                break;

            case 4:
                avaliarPedidosAbertos();
                break;

            case 5:
                concluirPedidoEntregue();
                break;

            case 6:
                buscarPedidos();
                break;

            case 7:
                avisarFuncionalidadePendente();
                break;

            case 8:
                trocarUsuario();
                break;

            case 0:
                return false;

            default:
                System.out.println("Opcao invalida.");
        }

        return true;
    }

    private void verMeusPedidos() {
        Usuario usuarioAtual = sessao.getUsuarioAtual();
        System.out.println("\nPedidos de " + usuarioAtual.getNome() + ":");

        boolean encontrouAlgum = false;
        for (PedidoAquisicao pedido : CargaInicial.pedidos) {
            if (pedido.getSolicitante().getId() == usuarioAtual.getId()) {
                System.out.println("  " + pedido);
                encontrouAlgum = true;
            }
        }

        if (!encontrouAlgum) {
            System.out.println("  Nenhum pedido encontrado.");
        }
    }

    //Exclusao de pedido aberto (Issue 5)
    private void excluirPedidoAberto() {
        System.out.print("\nDigite o numero do pedido que deseja excluir: ");
        int idPedido = lerOpcao();

        ResultadoOperacao resultado = ServicoPedidos.excluirPedido(CargaInicial.pedidos, sessao.getUsuarioAtual(), idPedido);
        exibirResultado(resultado);
    }

    //Avaliacao do administrador (Issue 6)
    private void avaliarPedidosAbertos() {
        List<PedidoAquisicao> abertos = ServicoPedidos.listarPedidosAbertos(CargaInicial.pedidos);

        if (abertos.isEmpty()) {
            System.out.println("\nNao ha pedidos abertos para avaliar.");
            return;
        }

        System.out.println("\nPedidos abertos:");
        for (PedidoAquisicao pedido : abertos) {
            System.out.println("  " + pedido);
            for (var item : pedido.getItens()) {
                System.out.println("      - " + item);
            }
        }

        System.out.print("\nDigite o numero do pedido para avaliar (0 para cancelar): ");
        int idPedido = lerOpcao();

        if (idPedido == 0) {
            return;
        }

        PedidoAquisicao pedidoEscolhido = buscarPedidoNaLista(abertos, idPedido);
        if (pedidoEscolhido == null) {
            System.out.println("Pedido aberto nao encontrado.");
            return;
        }

        System.out.print("Aprovar (A) ou Reprovar (R)? ");
        String decisao = entrada.nextLine().trim().toUpperCase();

        ResultadoOperacao resultado;
        if (decisao.equals("A")) {
            resultado = ServicoPedidos.aprovarPedido(sessao.getUsuarioAtual(), pedidoEscolhido);
        } else if (decisao.equals("R")) {
            resultado = ServicoPedidos.reprovarPedido(sessao.getUsuarioAtual(), pedidoEscolhido);
        } else {
            System.out.println("Opcao invalida. Avaliacao cancelada.");
            return;
        }

        exibirResultado(resultado);
    }

    //Conclusao de pedido entregue (Issue 2). Somente pedidos aprovados aparecem na lista.
    private void concluirPedidoEntregue() {
        List<PedidoAquisicao> aprovados = ServicoPedidos.listarPedidosAprovados(CargaInicial.pedidos);

        if (aprovados.isEmpty()) {
            System.out.println("\nNao ha pedidos aprovados aguardando conclusao.");
            return;
        }

        System.out.println("\nPedidos aprovados aguardando entrega:");
        for (PedidoAquisicao pedido : aprovados) {
            System.out.println("  " + pedido);
            for (ItemPedido item : pedido.getItens()) {
                System.out.println("      - " + item);
            }
        }

        System.out.print("\nDigite o numero do pedido entregue (0 para cancelar): ");
        int idPedido = lerOpcao();

        if (idPedido == 0) {
            System.out.println("Conclusao cancelada.");
            return;
        }

        if (idPedido < 0) {
            System.out.println("\nErro: numero de pedido invalido.");
            return;
        }

        PedidoAquisicao pedidoEscolhido = buscarPedidoNaLista(aprovados, idPedido);
        if (pedidoEscolhido == null) {
            System.out.println("\nErro: Pedido #" + idPedido + " nao esta na lista de pedidos aprovados.");
            return;
        }

        ResultadoOperacao resultado = ServicoPedidos.concluirPedido(sessao.getUsuarioAtual(), pedidoEscolhido);
        exibirResultado(resultado);
    }

    //Buscas do administrador (Issue 7). A filtragem fica no ServicoPedidos;
    //aqui so a leitura dos criterios e a exibicao dos resultados.
    private void buscarPedidos() {
        if (!sessao.isAdministrador()) {
            System.out.println("\nErro: somente administradores podem buscar pedidos.");
            return;
        }

        boolean continuar = true;
        while (continuar) {
            System.out.println("\nBuscar pedidos:");
            System.out.println("1 - Por intervalo de datas");
            System.out.println("2 - Por funcionario solicitante");
            System.out.println("3 - Por descricao de item");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opcao: ");

            int opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    buscarPorPeriodo();
                    break;

                case 2:
                    buscarPorSolicitante();
                    break;

                case 3:
                    buscarPorDescricaoItem();
                    break;

                case 0:
                    continuar = false;
                    break;

                default:
                    System.out.println("Opcao invalida.");
            }
        }
    }

    private void buscarPorPeriodo() {
        System.out.print("\nData inicial (dd/MM/aaaa): ");
        String textoInicio = entrada.nextLine();
        LocalDate inicio = ServicoPedidos.interpretarData(textoInicio);
        if (inicio == null) {
            System.out.println("\nErro: \"" + textoInicio.trim() + "\" nao e uma data valida no formato dd/MM/aaaa.");
            return;
        }

        System.out.print("Data final (dd/MM/aaaa): ");
        String textoFim = entrada.nextLine();
        LocalDate fim = ServicoPedidos.interpretarData(textoFim);
        if (fim == null) {
            System.out.println("\nErro: \"" + textoFim.trim() + "\" nao e uma data valida no formato dd/MM/aaaa.");
            return;
        }

        exibirResultadoBusca(ServicoPedidos.buscarPorPeriodo(CargaInicial.pedidos, inicio, fim));
    }

    private void buscarPorSolicitante() {
        System.out.println("\nSolicitantes:");
        for (Usuario usuario : CargaInicial.usuarios) {
            System.out.println("  " + usuario.getId() + " - " + usuario.getNome() + " - "
                    + usuario.getDepartamento().getSigla() + " (" + usuario.getPapel().getDescricao() + ")");
        }

        System.out.print("Digite o id do solicitante (0 para cancelar): ");
        int idSolicitante = lerOpcao();

        if (idSolicitante == 0) {
            return;
        }

        exibirResultadoBusca(ServicoPedidos.buscarPorSolicitante(CargaInicial.pedidos, CargaInicial.usuarios,
                idSolicitante));
    }

    private void buscarPorDescricaoItem() {
        System.out.print("\nTexto a pesquisar na descricao dos itens: ");
        String pesquisa = entrada.nextLine();

        exibirResultadoBusca(ServicoPedidos.buscarPorDescricaoItem(CargaInicial.pedidos, pesquisa));
    }

    //Mostra a lista resumida e deixa o administrador abrir os detalhes de
    //qualquer pedido do resultado, ate digitar 0.
    private void exibirResultadoBusca(ResultadoBusca resultado) {
        if (!resultado.isSucesso()) {
            System.out.println("\nErro: " + resultado.getMensagem());
            return;
        }

        List<PedidoAquisicao> encontrados = resultado.getPedidos();
        if (encontrados.isEmpty()) {
            System.out.println("\nNenhum pedido encontrado.");
            return;
        }

        boolean continuar = true;
        while (continuar) {
            System.out.println("\n" + encontrados.size() + " pedido(s) encontrado(s):");
            for (PedidoAquisicao pedido : encontrados) {
                System.out.println("  " + resumirPedido(pedido));
            }

            System.out.print("\nDigite o numero do pedido para ver os detalhes (0 para voltar): ");
            int idPedido = lerOpcao();

            if (idPedido == 0) {
                continuar = false;
                continue;
            }

            PedidoAquisicao pedido = buscarPedidoNaLista(encontrados, idPedido);
            if (pedido == null) {
                System.out.println("\nErro: o pedido informado nao esta entre os resultados da busca.");
            } else {
                exibirDetalhesPedido(pedido);
            }
        }
    }

    private String resumirPedido(PedidoAquisicao pedido) {
        return "Pedido #" + pedido.getId()
                + " | " + pedido.getSolicitante().getNome()
                + " | " + pedido.getDepartamento().getSigla()
                + " | Criado em " + pedido.getDataCriacao().format(FORMATO_DATA)
                + " | " + pedido.getStatus().getDescricao()
                + " | R$ " + pedido.getValorTotal();
    }

    private void exibirDetalhesPedido(PedidoAquisicao pedido) {
        System.out.println("\n--- Detalhes do pedido #" + pedido.getId() + " ---");
        System.out.println("Solicitante  : " + pedido.getSolicitante().getResumo());
        System.out.println("Departamento : " + pedido.getDepartamento().getNome()
                + " (" + pedido.getDepartamento().getSigla() + ")");
        System.out.println("Status       : " + pedido.getStatus().getDescricao());
        System.out.println("Criado em    : " + pedido.getDataCriacao().format(FORMATO_DATA));
        if (pedido.getDataConclusao() != null) {
            System.out.println("Concluido em : " + pedido.getDataConclusao().format(FORMATO_DATA));
        }
        System.out.println("Itens:");
        for (ItemPedido item : pedido.getItens()) {
            System.out.println("  - " + item.getDescricao()
                    + " | Quantidade: " + item.getQuantidade() + " " + item.getUnidade()
                    + " | Unitario: R$ " + item.getValorUnitario()
                    + " | Subtotal: R$ " + item.getSubtotal());
        }
        System.out.println("Valor total  : R$ " + pedido.getValorTotal());
    }

    //Permite trocar o usuario ativo na sessão, listando todos os usuarios cadastrados.
    private void trocarUsuario() {
        System.out.println("\nUsuarios disponiveis:");
        for (Usuario usuario : CargaInicial.usuarios) {
            System.out.println("  " + usuario.getId() + " - " + usuario.getResumo());
        }

        System.out.print("Digite o id do usuario: ");
        int idUsuario = lerOpcao();

        Usuario novoUsuario = buscarUsuarioPorId(idUsuario);
        if (novoUsuario == null) {
            System.out.println("Usuario nao encontrado.");
            return;
        }

        sessao.trocarUsuario(novoUsuario);
        System.out.println("Usuario alterado para: " + novoUsuario.getResumo());
    }

    //Registro de novo pedido (Issue 4). Coleta os itens um a um e delega a
    //validacao do teto do departamento para o ServicoPedidos.
    private void registrarNovoPedido() {
        Usuario solicitante = sessao.getUsuarioAtual();

        System.out.println("\nNovo pedido para " + solicitante.getNome()
                + " - departamento " + solicitante.getDepartamento().getSigla());
        System.out.println("Teto por pedido deste departamento: R$ "
                + solicitante.getDepartamento().getLimitePorPedido());

        List<ItemPedido> itens = new ArrayList<>();
        boolean continuarAdicionando = true;

        while (continuarAdicionando) {
            System.out.print("\nDescricao do item (ENTER em branco para encerrar): ");
            String descricao = entrada.nextLine().trim();

            if (descricao.isEmpty()) {
                continuarAdicionando = false;
                continue;
            }

            System.out.print("Quantidade: ");
            int quantidade = lerOpcao();

            if (quantidade < 1) {
                System.out.println("Quantidade invalida. Item descartado.");
                continue;
            }

            System.out.print("Valor unitario: ");
            BigDecimal valorUnitario = lerValor();

            if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Valor invalido. Item descartado.");
                continue;
            }

            itens.add(new ItemPedido(0, descricao, quantidade, "un", valorUnitario));
            System.out.println("Item adicionado.");
        }

        if (itens.isEmpty()) {
            System.out.println("\nNenhum item informado. Pedido cancelado.");
            return;
        }

        ResultadoOperacao resultado = ServicoPedidos.registrarPedido(CargaInicial.pedidos, solicitante, itens);
        exibirResultado(resultado);
    }

    //Ainda usado pela opcao de ver estatisticas (issue 8, do Membro 4).
    private void avisarFuncionalidadePendente() {
        System.out.println("\nFuncionalidade ainda em desenvolvimento por outro membro do time.");
    }

    private void exibirResultado(ResultadoOperacao resultado) {
        if (resultado.isSucesso()) {
            System.out.println("\n" + resultado.getMensagem());
        } else {
            System.out.println("\nErro: " + resultado.getMensagem());
        }
    }

    private PedidoAquisicao buscarPedidoNaLista(List<PedidoAquisicao> lista, int id) {
        for (PedidoAquisicao pedido : lista) {
            if (pedido.getId() == id) {
                return pedido;
            }
        }
        return null;
    }

    private Usuario buscarUsuarioPorId(int id) {
        for (Usuario usuario : CargaInicial.usuarios) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }
        return null;
    }

    //Lê uma opcao numerica do usuário.
    private int lerOpcao() {
        String linha = entrada.nextLine().trim();
        try {
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    //Lê um valor monetario do usuário, aceitando virgula ou ponto. Retorna null se for invalido.
    private BigDecimal lerValor() {
        String linha = entrada.nextLine().trim().replace(",", ".");
        try {
            return new BigDecimal(linha);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}