# 001 — Especialização `CurvaFet`

**Estado:** aceita em 20/07/2026.

## Contexto

Curvas de saída e transferência de FET precisam registrar seu tipo e a tensão
mantida constante. Repetir esse valor em cada medida seria redundante, enquanto
adicionar campos de FET a `Curva` reduziria sua generalidade.

## Decisão

Criar `CurvaFet`, em relação opcional um-para-um com `Curva`, contendo
`idCurva`, `tipoCurvaFet` e `valorTensaoConstante`.

## Consequências

- `Curva` continua reutilizável por outros componentes;
- a condição experimental é armazenada uma única vez;
- o modelo passa de sete para oito tabelas;
- banco, model, DAO, instruções e documentação devem evoluir em conjunto.

`CurvaFet` integra a criação inicial do banco. Não há migração nem criação
complementar quando `DadosGACS` já existe; durante o desenvolvimento, o banco
anterior deve ser excluído manualmente pelo responsável antes do teste.
