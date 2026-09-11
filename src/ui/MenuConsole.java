package ui;

import app.Contexto;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import modelo.ItemPedido;
import modelo.PedidoAquisicao;
import modelo.Usuario;
import sessao.Sessao;

/**
 * Interface de console da aplicacao. O cabecalho sempre mostra o usuario atual
 * e a opcao de troca fica disponivel em todas as telas.
 */
public class MenuConsole {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String LINHA = "=".repeat(62);

    private final Contexto contexto;
    private final Sessao sessao;
    private final Scanner entrada = new Scanner(System.in);

    public MenuConsole(Contexto contexto) {
        this.contexto = contexto;
        this.sessao = contexto.getSessao();
    }

    public void executar() {
        System.out.println(LINHA);
        System.out.println(" Sistema de Gerenciamento de Pedidos de Aquisicao");
        System.out.println(LINHA);
        try {
            if (!sessao.temUsuario()) {
                trocarUsuario();
            }
            laco();
        } catch (NoSuchElementException fimDaEntrada) {
            System.out.println("\nEntrada encerrada. Ate logo.");
        }
    }

    private void laco() {
        while (true) {
            exibirCabecalho();
            System.out.println("1 - Trocar usuario atual");
            System.out.println("2 - Detalhes do usuario atual");
            System.out.println("3 - Listar usuarios");
            System.out.println("4 - Listar departamentos");
            System.out.println("5 - Listar pedidos de aquisicao");
            System.out.println("0 - Sair");
            switch (lerTexto("Opcao: ")) {
                case "1" -> trocarUsuario();
                case "2" -> exibirUsuarioAtual();
                case "3" -> listarUsuarios();
                case "4" -> listarDepartamentos();
                case "5" -> listarPedidos();
                case "0" -> {
                    System.out.println("Ate logo.");
                    return;
                }
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void exibirCabecalho() {
        System.out.println();
        System.out.println(LINHA);
        System.out.println(" Usuario atual: " + sessao.descricaoUsuarioAtual());
        System.out.println(LINHA);
    }

    /** Selecao/alternancia do usuario atual, disponivel a qualquer momento. */
    private void trocarUsuario() {
        List<Usuario> usuarios = contexto.getUsuarios().listarTodos();
        System.out.println("\n-- Usuarios disponiveis --");
        usuarios.forEach(usuario -> System.out.println("  " + usuario.getResumo()));

        while (true) {
            String digitado = lerTexto("ID do usuario (vazio para manter o atual): ");
            if (digitado.isBlank()) {
                if (sessao.temUsuario()) {
                    return;
                }
                System.out.println("Nenhum usuario selecionado ainda; informe um ID.");
                continue;
            }
            try {
                Usuario selecionado = sessao.selecionarPorId(Integer.parseInt(digitado));
                System.out.println("Usuario atual agora e " + selecionado.getResumo());
                return;
            } catch (NumberFormatException erro) {
                System.out.println("Informe um numero de ID valido.");
            } catch (java.util.NoSuchElementException erro) {
                System.out.println(erro.getMessage());
            }
        }
    }

    private void exibirUsuarioAtual() {
        Usuario usuario = sessao.getUsuarioAtual();
        System.out.println("\n-- Usuario atual --");
        System.out.println("  ID          : " + usuario.getId());
        System.out.println("  Nome        : " + usuario.getNome());
        System.out.println("  Iniciais    : " + usuario.getIniciais());
        System.out.println("  Papel       : " + usuario.getPapel().getDescricao());
        System.out.println("  Departamento: " + usuario.getDepartamento().getNome()
                + " (" + usuario.getDepartamento().getSigla() + ")");
    }

    private void listarUsuarios() {
        System.out.println("\n-- Usuarios cadastrados --");
        for (Usuario usuario : contexto.getUsuarios().listarTodos()) {
            String marcador = sessao.temUsuario() && usuario.equals(sessao.getUsuarioAtual()) ? "* " : "  ";
            System.out.println(marcador + usuario.getResumo());
        }
    }

    private void listarDepartamentos() {
        System.out.println("\n-- Departamentos --");
        contexto.getDepartamentos().listarTodos()
                .forEach(departamento -> System.out.println("  " + departamento));
    }

    private void listarPedidos() {
        List<PedidoAquisicao> pedidos = contexto.getPedidos().listarTodos();
        System.out.println("\n-- Pedidos de aquisicao --");
        if (pedidos.isEmpty()) {
            System.out.println("  Nenhum pedido cadastrado.");
            return;
        }
        for (PedidoAquisicao pedido : pedidos) {
            System.out.printf("  Pedido #%d | %s | %s | aberto em %s%n",
                    pedido.getId(),
                    pedido.getDepartamento().getSigla(),
                    pedido.getStatus().getDescricao(),
                    pedido.getDataCriacao().format(DATA_HORA));
            System.out.println("    Solicitante : " + pedido.getSolicitante().getResumo());
            System.out.println("    Justificativa: " + pedido.getJustificativa());
            for (ItemPedido item : pedido.getItens()) {
                System.out.printf("      - %dx %s (%s) | unit. %s | subtotal %s%n",
                        item.getQuantidade(),
                        item.getDescricao(),
                        item.getUnidade(),
                        MOEDA.format(item.getValorUnitario()),
                        MOEDA.format(item.getSubtotal()));
            }
            System.out.println("    Total: " + MOEDA.format(pedido.getValorTotal()));
            if (pedido.getResponsavelDecisao() != null) {
                System.out.printf("    Decisao por %s em %s%s%n",
                        pedido.getResponsavelDecisao().getNome(),
                        pedido.getDataDecisao().format(DATA_HORA),
                        pedido.getObservacaoDecisao() == null ? "" : " - " + pedido.getObservacaoDecisao());
            }
        }
    }

    private String lerTexto(String rotulo) {
        System.out.print(rotulo);
        return entrada.nextLine().trim();
    }
}
