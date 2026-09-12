package sessao;

import modelo.Usuario;

/**
 * Controla a sessao do sistema e o usuario ativo no momento.
 * Permite alternar o operador atual a qualquer momento (Requisito Detalhamento 1).
 */
public class Sessao {
    private Usuario usuarioAtual;

    public Sessao() {
        this.usuarioAtual = null;
    }

    public Sessao(Usuario usuarioInicial) {
        this.usuarioAtual = usuarioInicial;
    }

    public Usuario getUsuarioAtual() {
        return usuarioAtual;
    }

    public void setUsuarioAtual(Usuario usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
    }

    public void trocarUsuario(Usuario novoUsuario) {
        this.usuarioAtual = novoUsuario;
    }

    public boolean temUsuario() {
        return usuarioAtual != null;
    }

    public boolean isAdministrador() {
        return usuarioAtual != null && usuarioAtual.isAdministrador();
    }

    /**
     * Retorna a descricao completa do operador ativo para exibir no cabecalho.
     */
    public String descricaoUsuarioAtual() {
        if (usuarioAtual == null) {
            return "Nenhum usuario selecionado";
        }
        return usuarioAtual.getResumo();
    }
}
