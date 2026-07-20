# Arquitetura

## Pacotes

```text
br.uel.gacs
├── application
├── controller
├── dao
├── model
└── util
```

- `application`: telas JavaFX, composição da aplicação e sessão autenticada;
- `controller`: regras do processo, validações e coordenação de DAOs;
- `dao`: JDBC, SQL e conversão entre registros e entidades;
- `model`: entidades persistentes e enums do domínio;
- `util`: funções genéricas reutilizáveis.

O fluxo vigente é:

```text
JavaFX → Controller → um ou mais DAOs → MySQL
```

Não há camada `service` nesta versão. Operações compostas são coordenadas pelos
controllers, e todo SQL permanece nos DAOs. Uma camada adicional exige
necessidade concreta e nova decisão arquitetural.

## Persistência

Operações compostas que alterem várias estruturas devem usar uma única
transação: `commit` no sucesso e `rollback` em qualquer falha.

O banco e todas as suas tabelas são criados somente quando `DadosGACS` ainda
não existe. Esta versão não possui mecanismo de migração nem criação
complementar de tabelas. Durante o desenvolvimento, uma mudança estrutural
exige recriar deliberadamente o banco; essa exclusão nunca é feita pela
aplicação.
