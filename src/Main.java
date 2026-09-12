import java.math.BigDecimal;
import modelo.Departamento;
import modelo.ItemPedido;
import modelo.Papel;
import modelo.PedidoAquisicao;
import modelo.Usuario;
import sessao.Sessao;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  Sistema de Controle de Aquisicoes - Teste Base  ");
        System.out.println("==================================================");

        // 1. Criando departamentos para demonstracao
        Departamento ti = new Departamento(1, "Tecnologia da Informacao", "TI", new BigDecimal("25000.00"));
        Departamento rh = new Departamento(2, "Recursos Humanos", "RH", new BigDecimal("5000.00"));

        // 2. Criando usuarios (com cálculo de iniciais e perfis)
        Usuario otavio = new Usuario(1, "Otavio Machado", Papel.ADMINISTRADOR, ti);
        Usuario joao = new Usuario(2, "Joao Silva", Papel.FUNCIONARIO, rh);

        // 3. Testando o controle de sessao e a alternancia de usuario
        Sessao sessao = new Sessao(otavio);
        System.out.println("\n[Sessao Inicial]");
        System.out.println("Operador atual : " + sessao.descricaoUsuarioAtual());
        System.out.println("Iniciais       : " + sessao.getUsuarioAtual().getIniciais());
        System.out.println("E administrador? " + (sessao.isAdministrador() ? "Sim" : "Nao"));

        // Alternando o operador ativo a qualquer momento
        sessao.trocarUsuario(joao);
        System.out.println("\n[Apos Troca de Usuario]");
        System.out.println("Operador atual : " + sessao.descricaoUsuarioAtual());
        System.out.println("Iniciais       : " + sessao.getUsuarioAtual().getIniciais());
        System.out.println("E administrador? " + (sessao.isAdministrador() ? "Sim" : "Nao"));

        // 4. Testando criacao de pedido e adicao de itens
        PedidoAquisicao pedido = new PedidoAquisicao(101, joao);
        pedido.adicionarItem(new ItemPedido(1, "Caderno para integracao", 10, "un", new BigDecimal("15.50")));
        pedido.adicionarItem(new ItemPedido(2, "Caneta esferografica", 20, "un", new BigDecimal("2.50")));

        System.out.println("\n[Pedido de Aquisicao Criado]");
        System.out.println(pedido);
        System.out.println("Itens inclusos:");
        for (ItemPedido item : pedido.getItens()) {
            System.out.println("  - " + item);
        }
        System.out.println("Valor total do pedido: R$ " + pedido.getValorTotal());

        System.out.println("\nBase de modelos e alternancia de usuario pronta com sucesso!");
    }
}
