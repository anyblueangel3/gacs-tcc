# 05 -- Regras de Negócio

## Princípios

-   O experimento é a unidade central do sistema.
-   O operador pode utilizar um fluxo rápido.
-   Cadastros completos são opcionais no protótipo.

## Colunas

Se tipoEixo = X: - idColunaX deve ser nulo.

Se tipoEixo = Y: - idColunaX deve referenciar uma coluna X do mesmo
experimento.

## Dados

-   Os pares X--Y são formados por numeroDaMedida.
-   Não substituir automaticamente valores inválidos.
-   Aceitar entrada manual, colagem e CSV delimitado por ';'.

## Caracterização

Os algoritmos devem receber apenas pares (x,y), independentemente da
origem dos dados.
