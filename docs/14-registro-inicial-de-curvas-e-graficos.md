# 14 — Registro inicial de curvas e gráficos

## 1. Identificação

- **Projeto:** GACS-TCC — Geração de Aplicativo de Caracterização de Componentes Semicondutores
- **Aplicativo:** GACS — Gerenciador para Análise e Caracterização de Componentes Semicondutores
- **Data:** 13 de julho de 2026

## 2. Objetivo do incremento

Este incremento inicia o fluxo seguinte ao registro das medições:

```text
Experimento salvo
→ seleção das colunas X e Y
→ criação da curva
→ criação do gráfico
→ associação da curva ao gráfico
```

O comando `Novo Gráfico`, antes provisório, foi ligado ao processo real de
persistência. Ele permanece disponível apenas quando existe um experimento
aberto.

## 3. Fluxo implementado

Ao acionar `Novo Gráfico`, o operador informa:

- o nome do gráfico;
- o nome da primeira curva;
- a coluna utilizada como eixo X;
- a coluna utilizada como eixo Y.

As colunas são apresentadas por rótulo e nome, por exemplo `A — Tensão` e
`B — Corrente`. O sistema sugere inicialmente a primeira coluna como X e a
segunda como Y, permitindo que o operador altere ambas.

O experimento e suas colunas precisam estar salvos antes da criação do gráfico.
Essa exigência é necessária porque `Curva` referencia os identificadores
persistidos de `Coluna`.

## 4. Persistência transacional

Foi criado `GraficoCtlr`, responsável por coordenar `CurvaDAO`, `GraficoDAO` e
`CurvaGraficoDAO`. O registro completo utiliza uma única conexão e uma única
transação:

1. insere a entidade `Curva` com as colunas X e Y;
2. insere a entidade `Grafico`, vinculada ao experimento aberto;
3. insere `CurvaGrafico` com `numeroCurva = 1`.

O `commit` ocorre somente depois do sucesso das três operações. Qualquer falha
provoca `rollback`, evitando curva, gráfico ou associação incompletos.

## 5. Validações

O controller verifica:

- experimento previamente salvo;
- existência de pelo menos duas colunas persistidas;
- nomes obrigatórios e limitados a 200 caracteres;
- seleção das duas colunas;
- colunas X e Y diferentes;
- pertencimento das duas colunas ao experimento aberto;
- existência efetiva das colunas no banco no momento da transação.

## 6. Classes incluídas e alteradas

Foram incluídas:

```text
controller/GraficoCtlr.java
application/TelaNovoGrafico.java
```

Foram integradas ao novo fluxo:

```text
application/MenuPrincipal.java
application/TelaPrincipal.java
application/PainelExperimento.java
```

## 7. Limite consciente deste incremento

Este sprint implementa o registro inicial de um gráfico com sua primeira curva.
A representação visual dos pontos, a inclusão posterior de outras curvas no
mesmo gráfico, a lista de gráficos salvos e a reabertura para edição ficam para
os próximos incrementos. O modelo persistido já suporta essas evoluções sem
criação de novas tabelas.
