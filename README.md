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
