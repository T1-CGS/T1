package dados;

import modelo.Departamento;
import modelo.ItemPedido;
import modelo.Papel;
import modelo.PedidoAquisicao;
import modelo.StatusPedido;
import modelo.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga inicial de dados mockados em memoria (Issue #3 - Membro 2).
 * Popula departamentos, usuarios e pedidos de exemplo para permitir
 * testar o sistema sem necessidade de cadastro manual previo.
 */
public class CargaInicial {

    public static final List<Departamento> departamentos = new ArrayList<>();
    public static final List<Usuario> usuarios = new ArrayList<>();
    public static final List<PedidoAquisicao> pedidos = new ArrayList<>();

    /**
     * Popula as listas em memoria com dados de exemplo.
     * Deve ser chamado no inicio da aplicacao. Chamadas repetidas sao
     * ignoradas, para nao duplicar os dados ja carregados.
     */
    public static void carregar() {
        if (!departamentos.isEmpty()) {
            return;
        }
        carregarDepartamentos();
        carregarUsuarios();
        carregarPedidos();
    }

    private static void carregarDepartamentos() {
        departamentos.add(new Departamento(1, "Tecnologia da Informacao", "TI", new BigDecimal("30000.00")));
        departamentos.add(new Departamento(2, "Financeiro", "FIN", new BigDecimal("50000.00")));
        departamentos.add(new Departamento(3, "Recursos Humanos", "RH", new BigDecimal("15000.00")));
        departamentos.add(new Departamento(4, "Engenharia", "ENG", new BigDecimal("80000.00")));
        departamentos.add(new Departamento(5, "Manutencao", "MAN", new BigDecimal("20000.00")));
    }

    /**
     * Cada departamento recebe 1 administrador e 3 funcionarios,
     * totalizando 15 funcionarios e 5 administradores.
     */
    private static void carregarUsuarios() {
        Departamento ti = departamentos.get(0);
        Departamento fin = departamentos.get(1);
        Departamento rh = departamentos.get(2);
        Departamento eng = departamentos.get(3);
        Departamento man = departamentos.get(4);

        usuarios.add(new Usuario(1, "Ana Ribeiro", Papel.ADMINISTRADOR, ti));
        usuarios.add(new Usuario(2, "Bruno Alves", Papel.FUNCIONARIO, ti));
        usuarios.add(new Usuario(3, "Carla Nunes", Papel.FUNCIONARIO, ti));
        usuarios.add(new Usuario(4, "Daniel Moreira", Papel.FUNCIONARIO, ti));

        usuarios.add(new Usuario(5, "Diego Souza", Papel.ADMINISTRADOR, fin));
        usuarios.add(new Usuario(6, "Elisa Moraes", Papel.FUNCIONARIO, fin));
        usuarios.add(new Usuario(7, "Fabio Lima", Papel.FUNCIONARIO, fin));
        usuarios.add(new Usuario(8, "Giovana Prado", Papel.FUNCIONARIO, fin));

        usuarios.add(new Usuario(9, "Helena Rocha", Papel.ADMINISTRADOR, rh));
        usuarios.add(new Usuario(10, "Hugo Martins", Papel.FUNCIONARIO, rh));
        usuarios.add(new Usuario(11, "Isabela Costa", Papel.FUNCIONARIO, rh));
        usuarios.add(new Usuario(12, "Jonas Ferreira", Papel.FUNCIONARIO, rh));

        usuarios.add(new Usuario(13, "Joao Pereira", Papel.ADMINISTRADOR, eng));
        usuarios.add(new Usuario(14, "Karina Dias", Papel.FUNCIONARIO, eng));
        usuarios.add(new Usuario(15, "Lucas Fernandes", Papel.FUNCIONARIO, eng));
        usuarios.add(new Usuario(16, "Mariana Aguiar", Papel.FUNCIONARIO, eng));

        usuarios.add(new Usuario(17, "Renata Barbosa", Papel.ADMINISTRADOR, man));
        usuarios.add(new Usuario(18, "Nicolas Barros", Papel.FUNCIONARIO, man));
        usuarios.add(new Usuario(19, "Olivia Cardoso", Papel.FUNCIONARIO, man));
        usuarios.add(new Usuario(20, "Paulo Henrique Silva", Papel.FUNCIONARIO, man));
    }

    private static void carregarPedidos() {
        Usuario bruno = buscarUsuarioPorId(2);
        Usuario elisa = buscarUsuarioPorId(6);
        Usuario hugo = buscarUsuarioPorId(10);
        Usuario karina = buscarUsuarioPorId(14);
        Usuario lucas = buscarUsuarioPorId(15);
        Usuario nicolas = buscarUsuarioPorId(18);

        // Pedido 1 - Aberto (TI)
        PedidoAquisicao pedido1 = new PedidoAquisicao(1, bruno);
        pedido1.adicionarItem(new ItemPedido(1, "Notebook Dell Inspiron", 1, "un", new BigDecimal("4200.00")));
        pedido1.adicionarItem(new ItemPedido(2, "Mouse sem fio", 2, "un", new BigDecimal("85.00")));
        pedidos.add(pedido1);

        // Pedido 2 - Aprovado, com data de conclusao (Financeiro)
        PedidoAquisicao pedido2 = new PedidoAquisicao(2, elisa);
        pedido2.adicionarItem(new ItemPedido(1, "Licenca de software de planilhas", 10, "un", new BigDecimal("450.00")));
        pedido2.setStatus(StatusPedido.APROVADO);
        pedido2.setDataConclusao(LocalDateTime.now().minusDays(2));
        pedidos.add(pedido2);

        // Pedido 3 - Reprovado (RH)
        PedidoAquisicao pedido3 = new PedidoAquisicao(3, hugo);
        pedido3.adicionarItem(new ItemPedido(1, "Cadeira ergonomica", 12, "un", new BigDecimal("1200.00")));
        pedido3.setStatus(StatusPedido.REPROVADO);
        pedidos.add(pedido3);

        // Pedido 4 - Aberto (Engenharia)
        PedidoAquisicao pedido4 = new PedidoAquisicao(4, lucas);
        pedido4.adicionarItem(new ItemPedido(1, "Kit de ferramentas industriais", 5, "un", new BigDecimal("980.00")));
        pedido4.adicionarItem(new ItemPedido(2, "Capacete de seguranca", 8, "un", new BigDecimal("75.00")));
        pedidos.add(pedido4);

        // Pedido 5 - Aprovado (Manutencao)
        PedidoAquisicao pedido5 = new PedidoAquisicao(5, nicolas);
        pedido5.adicionarItem(new ItemPedido(1, "Compressor de ar", 1, "un", new BigDecimal("3800.00")));
        pedido5.setStatus(StatusPedido.APROVADO);
        pedidos.add(pedido5);

        // Pedido 6 - Concluido, com data de conclusao (Engenharia)
        PedidoAquisicao pedido6 = new PedidoAquisicao(6, karina);
        pedido6.adicionarItem(new ItemPedido(1, "Licenca de software CAD", 3, "un", new BigDecimal("2200.00")));
        pedido6.setStatus(StatusPedido.CONCLUIDO);
        pedido6.setDataConclusao(LocalDateTime.now().minusDays(5));
        pedidos.add(pedido6);
    }

    private static Usuario buscarUsuarioPorId(int id) {
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }
        return null;
    }
}
