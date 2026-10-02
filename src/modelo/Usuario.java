package modelo;

/**
 * Representa um usuario do sistema (Funcionario ou Administrador).
 * Requisitos 1 e 4: Identificador, nome, iniciais e tipo.
 */
public class Usuario {
    private int id;
    private String nome;
    private Papel papel;
    private Departamento departamento;

    public Usuario(int id, String nome, Papel papel, Departamento departamento) {
        this.id = id;
        this.nome = nome;
        this.papel = papel;
        this.departamento = departamento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public boolean isAdministrador() {
        return papel != null && papel.isAdministrador();
    }

    /**
     * Algoritmo para obter as iniciais a partir do nome completo.
     * Pega a primeira letra do primeiro nome e a primeira letra do ultimo nome.
     */
    public String getIniciais() {
        if (nome == null || nome.trim().isEmpty()) {
            return "";
        }
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }
        String primeiraLetra = partes[0].substring(0, 1).toUpperCase();
        String ultimaLetra = partes[partes.length - 1].substring(0, 1).toUpperCase();
        return primeiraLetra + ultimaLetra;
    }

    /**
     * Resumo formatado para identificar o operador atual.
     */
    public String getResumo() {
        String siglaDepto = (departamento != null) ? departamento.getSigla() : "S/D";
        String nomePapel = (papel != null) ? papel.getDescricao() : "Sem Papel";
        return "#" + id + " " + nome + " (" + getIniciais() + ") - " + nomePapel + " - " + siglaDepto;
    }

    @Override
    public String toString() {
        return getResumo();
    }
}
