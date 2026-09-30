package servico;

import modelo.PedidoAquisicao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Resultado de uma busca de pedidos (Issue 7): ou a lista encontrada (que pode
// estar vazia), ou uma mensagem explicando por que a busca foi recusada.
public class ResultadoBusca {
    private final boolean sucesso;
    private final String mensagem;
    private final List<PedidoAquisicao> pedidos;

    private ResultadoBusca(boolean sucesso, String mensagem, List<PedidoAquisicao> pedidos) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.pedidos = pedidos;
    }

    public static ResultadoBusca sucesso(List<PedidoAquisicao> pedidos) {
        return new ResultadoBusca(true, "", Collections.unmodifiableList(new ArrayList<>(pedidos)));
    }

    public static ResultadoBusca falha(String mensagem) {
        return new ResultadoBusca(false, mensagem, Collections.emptyList());
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    // Lista somente leitura com os pedidos encontrados.
    public List<PedidoAquisicao> getPedidos() {
        return pedidos;
    }
}
