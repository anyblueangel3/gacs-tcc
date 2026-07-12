# 03 -- Arquitetura do GACS

## Arquitetura em camadas

O GACS adota inicialmente uma arquitetura em camadas:

-   application
-   controller
-   dao
-   model
-   util

Cada camada possui responsabilidade única.

## Fluxo principal

Interface JavaFX → Controller → um ou mais DAOs → MySQL

Na fase atual do protótipo, não será utilizada uma camada `service`. Os
controllers recebem os dados da interface, aplicam as validações e regras do
processo e coordenam os DAOs necessários. Todo SQL permanece exclusivamente
nos DAOs.

Uma camada `service` somente poderá ser introduzida futuramente diante de uma
necessidade concreta, como complexidade excessiva dos controllers ou
reutilização significativa de regras, mediante nova decisão arquitetônica
documentada.

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
