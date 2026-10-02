# T1 - Sistema de Controle de Aquisicoes

Sistema em Java puro, sem dependencias externas, para registrar, avaliar e
acompanhar pedidos de aquisicao de departamentos. Os dados ficam em memoria e
sao carregados por `dados.CargaInicial` ao iniciar.

## Requisitos

- JDK 11 ou superior (`javac` e `java` no PATH).

## Compilar e executar

Na raiz do repositorio:

```sh
javac --release 11 -d out $(find src -name "*.java")
java -cp out MenuPrincipal
```

`MenuPrincipal` e a entrada do sistema interativo. O sistema inicia com a
administradora Ana Ribeiro (TI); use a opcao "Trocar usuario" para operar como
outro funcionario ou administrador.

`Main` executa apenas uma demonstracao dos modelos:

```sh
java -cp out Main
```

## Testes

Os testes ficam em `test/` e usam apenas Java puro. O programa termina com
codigo 1 se alguma verificacao falhar.

```sh
javac --release 11 -d out $(find src test -name "*.java")
java -cp out TesteRegrasPedido
java -cp out TesteBuscasPedidos
java -cp out TesteEstatisticasPedidos
```

## Ciclo de vida dos pedidos

```
ABERTO --aprovar--> APROVADO --concluir--> CONCLUIDO
   \
    --reprovar--> REPROVADO
```

- Somente pedidos abertos podem ter dados ou itens alterados e somente eles
  podem ser excluidos (apenas pelo funcionario que os criou).
- Aprovar, reprovar e concluir sao acoes exclusivas do administrador.
- A conclusao registra automaticamente a data e hora em que o pedido foi
  marcado como entregue.
- Pedidos aprovados, reprovados ou concluidos nunca voltam a ser abertos. O
  bloqueio e garantido no proprio modelo: `getItens()` devolve uma lista
  somente leitura e `ItemPedido` e imutavel.

## Buscas do administrador

No menu do administrador, a opcao "Buscar pedidos" abre um submenu com tres
modalidades. Todas consideram pedidos de qualquer status e nao alteram nada:

1. **Por intervalo de datas**: informe a data inicial e a final no formato
   `dd/MM/aaaa`. Os dois dias entram inteiros no resultado, pela data de
   criacao do pedido.
2. **Por funcionario solicitante**: escolha o solicitante pelo id na lista
   exibida (funcionarios e administradores).
3. **Por descricao de item**: digite parte da descricao, sem diferenciar
   maiusculas e minusculas (ex.: `cadeira` encontra "Cadeira ergonomica").

O resultado mostra numero, solicitante, departamento, data de criacao, status e
valor total de cada pedido. Digite o numero de um pedido da lista para ver os
itens e as datas, ou `0` para voltar.

## Estatisticas do administrador

A opcao **7 - Ver estatisticas gerais** mostra:

- Total de pedidos, quantidade e percentual em cada status atual. Todos os
  percentuais usam o total como denominador; concluidos aparecem separados
  dos aprovados que ainda aguardam entrega.
- Quantidade e valor medio dos pedidos criados nos ultimos 30 dias, incluindo
  hoje e os 29 dias anteriores, ate o momento da consulta. A media considera
  o total de cada pedido, de qualquer status, e e arredondada para centavos.
- Detalhes do pedido aberto de maior valor. Em caso de empate, aparece o
  pedido com o menor numero.

Quando nao ha pedidos, as contagens, percentuais e media sao zero. Quando nao
ha pedidos abertos, o sistema informa isso. As consultas nao alteram os dados.

## Relatorio e entrega

- [Relatorio final em PDF](output/pdf/relatorio-final-t1.pdf)
- [Fonte editavel do relatorio](docs/relatorio-final.md)
- [Evidencias do Git e dos testes](docs/evidencias/)

O PDF pode ser regenerado com `python3 docs/gerar_relatorio.py`, instalando
`reportlab` apenas para essa tarefa de documentacao. Essa dependencia nao e
necessaria para compilar, testar ou executar o sistema Java.

A equipe deve enviar o relatorio na atividade do Moodle quando o acesso
estiver disponivel. A presenca dos arquivos no repositorio nao representa
uma submissao ao Moodle.
