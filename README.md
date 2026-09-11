# Gerenciamento de Pedidos de Aquisicao

Projeto Java (JDK 26, projeto IntelliJ sem build tool). Entrada: `src/Main.java`.

## Estrutura

```
src/
  Main.java                     ponto de entrada
  modelo/                       modelos de dados
    Papel.java                  FUNCIONARIO | ADMINISTRADOR
    Usuario.java                id, nome, iniciais, papel, departamento
    Departamento.java           id, nome, sigla, limitePorPedido
    PedidoAquisicao.java        solicitante, departamento, itens, status, decisao, conclusao
    ItemPedido.java             descricao, quantidade, unidade, valor unitario, subtotal
    StatusPedido.java           estados e transicoes validas
  repositorio/                  armazenamento em memoria (sem persistencia ainda)
    RepositorioEmMemoria.java   base generica com ID sequencial
    UsuarioRepositorio.java
    DepartamentoRepositorio.java
    PedidoRepositorio.java
  sessao/
    Sessao.java                 usuario atual + troca a qualquer momento
  app/
    Contexto.java               repositorios, sessao e dados iniciais
  ui/
    MenuConsole.java            menu de console
```

## Ciclo de vida do pedido

```
ABERTO -> APROVADO -> CONCLUIDO
       -> REPROVADO
ABERTO / APROVADO -> CANCELADO
```

Transicoes invalidas sao rejeitadas por `PedidoAquisicao.alterarStatus`, que tambem
exige papel de administrador para APROVADO e REPROVADO.

## Usuario atual

Nao ha autenticacao: `Sessao` guarda o usuario selecionado e permite troca-lo a
qualquer momento (`selecionarPorId`). O cabecalho do menu mostra sempre ID, nome,
iniciais e papel, e a opcao 1 troca de usuario em qualquer ponto do fluxo.
`Sessao.aoTrocarUsuario` notifica observadores, para uma futura interface grafica.

## Compilar e executar

```bash
javac -d out $(find src -name "*.java")
java -cp out Main
```
