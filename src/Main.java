import app.Contexto;
import ui.MenuConsole;

/**
 * Ponto de entrada do sistema de gerenciamento de pedidos de aquisicao.
 */
public class Main {

    public static void main(String[] args) {
        new MenuConsole(new Contexto()).executar();
    }
}
