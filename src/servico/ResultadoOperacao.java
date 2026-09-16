package servico;

public class ResultadoOperacao {
    private final boolean sucesso;
    private final String mensagem;

    private ResultadoOperacao(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public static ResultadoOperacao sucesso(String mensagem) {
        return new ResultadoOperacao(true, mensagem);
    }

    public static ResultadoOperacao falha(String mensagem) {
        return new ResultadoOperacao(false, mensagem);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }
}