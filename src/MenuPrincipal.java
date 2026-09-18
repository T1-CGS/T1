import dados.CargaInicial;
import modelo.PedidoAquisicao;
import modelo.Usuario;
import servico.ResultadoOperacao;
import servico.ServicoPedidos;
import sessao.Sessao;

import java.util.List;
import java.util.Scanner;

//As opcoes "Registrar novo pedido", "Buscar pedidos" e "Ver estatisticas" dependem de outras issues (do Membro 2 e do 4) que ainda nao foram mergeadas, então aparecem no menu mas com aviso de "em desenvolvimento".
public class MenuPrincipal {

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
                avisarFuncionalidadePendente();
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
        System.out.println("5 - Buscar pedidos");
        System.out.println("6 - Ver estatisticas gerais");
        System.out.println("7 - Trocar usuario");
        System.out.println("0 - Sair");
        System.out.print("Escolha uma opcao: ");

        int opcao = lerOpcao();

        switch (opcao) {
            case 1:
                verMeusPedidos();
                break;

            case 2:
                avisarFuncionalidadePendente();
                break;

            case 3:
                excluirPedidoAberto();
                break;

            case 4:
                avaliarPedidosAbertos();
                break;

            case 5:
                avisarFuncionalidadePendente();
                break;

            case 6:
                avisarFuncionalidadePendente();
                break;

            case 7:
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

        PedidoAquisicao pedidoEscolhido = buscarPedidoAberto(abertos, idPedido);
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

    //EM DESENVOLVIMENTO!!! EM "MenuPrincipal" ESTÁ COMPLETO! MAS FALTA A ISSUE 4, 7 E 8 SEREM FINALIZADAS (registrar pedido, buscar pedidos, ver estatísticas). DEPOIS DE FEITOS TEM QUE TROCAR O NOME DO MÉTODO PARA A CHAMADA REAL.
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

    private PedidoAquisicao buscarPedidoAberto(List<PedidoAquisicao> abertos, int id) {
        for (PedidoAquisicao pedido : abertos) {
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
}