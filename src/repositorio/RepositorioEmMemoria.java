package repositorio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Armazenamento em memoria com ID sequencial, usado enquanto o projeto nao tem
 * persistencia. Mantem a ordem de insercao.
 */
public abstract class RepositorioEmMemoria<T> {

    private final Map<Integer, T> registros = new LinkedHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    /** Extrai o ID da entidade. */
    protected abstract Function<T, Integer> extratorDeId();

    /** Proximo ID disponivel; use antes de construir a entidade. */
    public int proximoId() {
        return sequencia.getAndIncrement();
    }

    public T salvar(T entidade) {
        int id = extratorDeId().apply(entidade);
        registros.put(id, entidade);
        sequencia.updateAndGet(atual -> Math.max(atual, id + 1));
        return entidade;
    }

    public Optional<T> buscarPorId(int id) {
        return Optional.ofNullable(registros.get(id));
    }

    public List<T> listarTodos() {
        return Collections.unmodifiableList(new ArrayList<>(registros.values()));
    }

    public boolean remover(int id) {
        return registros.remove(id) != null;
    }

    public int quantidade() {
        return registros.size();
    }
}
