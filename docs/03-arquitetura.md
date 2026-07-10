# 03 -- Arquitetura do GACS

## Arquitetura em camadas

O GACS adota inicialmente uma arquitetura em camadas:

-   application
-   controller
-   service
-   dao
-   model
-   util

Cada camada possui responsabilidade única.

## Fluxo principal

Interface → Controller → Service → DAO → Banco

## Núcleo funcional

Entrada de dados → Gráfico → Caracterização

As três funcionalidades devem utilizar exatamente o mesmo modelo de
dados.

## Tecnologias

-   Java 21
-   JavaFX
-   Maven
-   MySQL 8
-   JDBC

Não utilizar frameworks adicionais nesta primeira versão.
