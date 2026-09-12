package modelo;

/**
 * Papel de um usuario no sistema (Requisito 1: Funcionario ou Administrador).
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
}
