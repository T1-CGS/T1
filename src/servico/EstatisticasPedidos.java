package servico;

import modelo.PedidoAquisicao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Resumo das estatisticas gerais do administrador (Issue #8). */
public final class EstatisticasPedidos {
    private final int total;
    private final int abertos;
    private final int aprovados;
    private final int reprovados;
    private final int concluidos;
    private final LocalDate inicioPeriodo;
    private final LocalDate fimPeriodo;
    private final int pedidosRecentes;
    private final BigDecimal valorMedioRecente;
    private final PedidoAquisicao maiorPedidoAberto;

    EstatisticasPedidos(int total, int abertos, int aprovados, int reprovados, int concluidos,
            LocalDate inicioPeriodo, LocalDate fimPeriodo, int pedidosRecentes,
            BigDecimal valorMedioRecente, PedidoAquisicao maiorPedidoAberto) {
        this.total = total;
        this.abertos = abertos;
        this.aprovados = aprovados;
        this.reprovados = reprovados;
        this.concluidos = concluidos;
        this.inicioPeriodo = inicioPeriodo;
        this.fimPeriodo = fimPeriodo;
        this.pedidosRecentes = pedidosRecentes;
        this.valorMedioRecente = valorMedioRecente;
        this.maiorPedidoAberto = maiorPedidoAberto;
    }

    public int getTotal() { return total; }
    public int getAbertos() { return abertos; }
    public int getAprovados() { return aprovados; }
    public int getReprovados() { return reprovados; }
    public int getConcluidos() { return concluidos; }
    public LocalDate getInicioPeriodo() { return inicioPeriodo; }
    public LocalDate getFimPeriodo() { return fimPeriodo; }
    public int getPedidosRecentes() { return pedidosRecentes; }
    public BigDecimal getValorMedioRecente() { return valorMedioRecente; }
    public PedidoAquisicao getMaiorPedidoAberto() { return maiorPedidoAberto; }

    public BigDecimal getPercentualAbertos() { return percentual(abertos); }
    public BigDecimal getPercentualAprovados() { return percentual(aprovados); }
    public BigDecimal getPercentualReprovados() { return percentual(reprovados); }
    public BigDecimal getPercentualConcluidos() { return percentual(concluidos); }

    private BigDecimal percentual(int quantidade) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(quantidade).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
