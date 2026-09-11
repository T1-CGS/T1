package repositorio;

import java.util.function.Function;
import modelo.Papel;
import modelo.Usuario;

import java.util.List;

public class UsuarioRepositorio extends RepositorioEmMemoria<Usuario> {

    @Override
    protected Function<Usuario, Integer> extratorDeId() {
        return Usuario::getId;
    }

    public List<Usuario> listarPorPapel(Papel papel) {
        return listarTodos().stream().filter(usuario -> usuario.getPapel() == papel).toList();
    }
}
