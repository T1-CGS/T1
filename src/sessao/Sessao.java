package sessao;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import modelo.Usuario;
import repositorio.UsuarioRepositorio;

/**
 * Mantem o usuario atual da aplicacao e permite alterna-lo a qualquer momento.
 * Nao ha autenticacao: a troca e apenas a selecao de um usuario existente.
 */
public class Sessao {

    private final UsuarioRepositorio usuarios;
    private final List<Consumer<Usuario>> ouvintes = new ArrayList<>();
    private Usuario usuarioAtual;

    public Sessao(UsuarioRepositorio usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios, "Repositorio de usuarios e obrigatorio.");
    }

    public boolean temUsuario() {
        return usuarioAtual != null;
    }

    /** @throws IllegalStateException se nenhum usuario foi selecionado ainda. */
    public Usuario getUsuarioAtual() {
        if (usuarioAtual == null) {
            throw new IllegalStateException("Nenhum usuario selecionado.");
        }
        return usuarioAtual;
    }

    /** Seleciona o usuario pelo ID. */
    public Usuario selecionarPorId(int id) {
        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario #%d nao encontrado.".formatted(id)));
        return selecionar(usuario);
    }

    public Usuario selecionar(Usuario usuario) {
        this.usuarioAtual = Objects.requireNonNull(usuario, "Usuario e obrigatorio.");
        ouvintes.forEach(ouvinte -> ouvinte.accept(usuario));
        return usuario;
    }

    public void encerrar() {
        this.usuarioAtual = null;
        ouvintes.forEach(ouvinte -> ouvinte.accept(null));
    }

    public boolean isAdministrador() {
        return usuarioAtual != null && usuarioAtual.isAdministrador();
    }

    /** Impede a acao quando o usuario atual nao e administrador. */
    public void exigirAdministrador() {
        if (!isAdministrador()) {
            throw new IllegalStateException("Acao permitida somente para administradores.");
        }
    }

    /** Registra um observador notificado a cada troca de usuario (util para a UI). */
    public void aoTrocarUsuario(Consumer<Usuario> ouvinte) {
        ouvintes.add(Objects.requireNonNull(ouvinte));
    }

    /** Descricao curta do usuario atual: ID, nome, iniciais e papel. */
    public String descricaoUsuarioAtual() {
        return usuarioAtual == null ? "Nenhum usuario selecionado" : usuarioAtual.getResumo();
    }
}
