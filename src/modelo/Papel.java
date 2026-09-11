package modelo;

/**
 * Papel do usuario dentro do sistema de aquisicoes.
 */
public enum Papel {
    FUNCIONARIO("Funcionario"),
    ADMINISTRADOR("Administrador");

    private final String descricao;

    Papel(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAdministrador() {
        return this == ADMINISTRADOR;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
