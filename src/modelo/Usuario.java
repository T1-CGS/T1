package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Usuario do sistema. Pode ser um funcionario (abre pedidos) ou um
 * administrador (tambem analisa e decide pedidos).
 */
public class Usuario {

    /** Particulas ignoradas no calculo das iniciais (ex.: Joao da Silva -> JS). */
    private static final Set<String> PARTICULAS = Set.of("de", "da", "do", "das", "dos", "e", "di", "du", "del", "van");

    private final int id;
    private String nome;
    private Papel papel;
    private Departamento departamento;

    public Usuario(int id, String nome, Papel papel, Departamento departamento) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do usuario e obrigatorio.");
        }
        this.id = id;
        this.nome = nome.trim();
        this.papel = Objects.requireNonNull(papel, "Papel do usuario e obrigatorio.");
        this.departamento = Objects.requireNonNull(departamento, "Departamento do usuario e obrigatorio.");
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do usuario e obrigatorio.");
        }
        this.nome = nome.trim();
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = Objects.requireNonNull(papel, "Papel do usuario e obrigatorio.");
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = Objects.requireNonNull(departamento, "Departamento do usuario e obrigatorio.");
    }

    public boolean isAdministrador() {
        return papel.isAdministrador();
    }

    /**
     * Iniciais derivadas do nome: primeira letra do primeiro e do ultimo nome
     * significativo. Nome unico com duas ou mais letras usa as duas primeiras.
     */
    public String getIniciais() {
        List<String> partes = new ArrayList<>();
        for (String parte : nome.split("\\s+")) {
            if (!parte.isBlank() && !PARTICULAS.contains(parte.toLowerCase(Locale.ROOT))) {
                partes.add(parte);
            }
        }
        if (partes.isEmpty()) {
            partes.add(nome.trim());
        }
        if (partes.size() == 1) {
            String unico = partes.get(0);
            String iniciais = unico.length() >= 2 ? unico.substring(0, 2) : unico.substring(0, 1);
            return iniciais.toUpperCase(Locale.ROOT);
        }
        String primeira = partes.get(0).substring(0, 1);
        String ultima = partes.get(partes.size() - 1).substring(0, 1);
        return (primeira + ultima).toUpperCase(Locale.ROOT);
    }

    /** Linha de identificacao usada no cabecalho da aplicacao. */
    public String getResumo() {
        return "#%d %s (%s) - %s - %s".formatted(id, nome, getIniciais(), papel.getDescricao(), departamento.getSigla());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Usuario outro && outro.id == this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getResumo();
    }
}
