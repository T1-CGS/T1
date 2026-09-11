package repositorio;

import java.util.function.Function;
import modelo.Departamento;

public class DepartamentoRepositorio extends RepositorioEmMemoria<Departamento> {

    @Override
    protected Function<Departamento, Integer> extratorDeId() {
        return Departamento::getId;
    }
}
