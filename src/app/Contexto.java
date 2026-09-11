package app;

import java.math.BigDecimal;
import modelo.Departamento;
import modelo.Papel;
import modelo.PedidoAquisicao;
import modelo.StatusPedido;
import modelo.Usuario;
import repositorio.DepartamentoRepositorio;
import repositorio.PedidoRepositorio;
import repositorio.UsuarioRepositorio;
import sessao.Sessao;

/**
 * Reune os repositorios e a sessao da aplicacao e carrega os dados iniciais
 * usados enquanto nao ha persistencia.
 */
public final class Contexto {

    private final DepartamentoRepositorio departamentos = new DepartamentoRepositorio();
    private final UsuarioRepositorio usuarios = new UsuarioRepositorio();
    private final PedidoRepositorio pedidos = new PedidoRepositorio();
    private final Sessao sessao = new Sessao(usuarios);

    public Contexto() {
        carregarDadosIniciais();
    }

    public DepartamentoRepositorio getDepartamentos() {
        return departamentos;
    }

    public UsuarioRepositorio getUsuarios() {
        return usuarios;
    }

    public PedidoRepositorio getPedidos() {
        return pedidos;
    }

    public Sessao getSessao() {
        return sessao;
    }

    public Departamento novoDepartamento(String nome, String sigla, BigDecimal limitePorPedido) {
        return departamentos.salvar(new Departamento(departamentos.proximoId(), nome, sigla, limitePorPedido));
    }

    public Departamento novoDepartamento(String nome, String sigla) {
        return novoDepartamento(nome, sigla, new BigDecimal("10000.00"));
    }

    public Usuario novoUsuario(String nome, Papel papel, Departamento departamento) {
        return usuarios.salvar(new Usuario(usuarios.proximoId(), nome, papel, departamento));
    }

    public PedidoAquisicao novoPedido(Usuario solicitante, String justificativa) {
        return pedidos.salvar(new PedidoAquisicao(pedidos.proximoId(), solicitante, justificativa));
    }

    private void carregarDadosIniciais() {
        Departamento ti = novoDepartamento("Tecnologia da Informacao", "TI", new BigDecimal("25000.00"));
        Departamento rh = novoDepartamento("Recursos Humanos", "RH", new BigDecimal("5000.00"));
        Departamento compras = novoDepartamento("Compras", "COM", new BigDecimal("50000.00"));

        Usuario ana = novoUsuario("Ana Beatriz Lima", Papel.ADMINISTRADOR, compras);
        Usuario bruno = novoUsuario("Bruno Souza", Papel.FUNCIONARIO, ti);
        Usuario carla = novoUsuario("Carla Mendes de Oliveira", Papel.FUNCIONARIO, rh);
        novoUsuario("Diego Rocha", Papel.ADMINISTRADOR, ti);

        PedidoAquisicao pedidoTi = novoPedido(bruno, "Substituicao de notebooks fora de garantia.");
        pedidoTi.adicionarItem("Notebook 16GB RAM / SSD 512GB", 3, "un", new BigDecimal("5200.00"));
        pedidoTi.adicionarItem("Monitor 24 polegadas", 3, "un", new BigDecimal("890.50"));

        PedidoAquisicao pedidoRh = novoPedido(carla, "Material para treinamento de integracao.");
        pedidoRh.adicionarItem("Caderno personalizado", 50, "un", new BigDecimal("18.90"));
        pedidoRh.adicionarItem("Caneta esferografica", 50, "un", new BigDecimal("3.40"));
        pedidoRh.registrarDecisao(StatusPedido.APROVADO, ana, "Dentro do orcamento do trimestre.");

        sessao.selecionar(ana);
    }
}
