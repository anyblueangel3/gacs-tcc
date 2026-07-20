# Instruções para agentes — GACS

## Identidade

- Projeto: **GACS-TCC — Geração de Aplicativo de Caracterização de Componentes
  Semicondutores**.
- Software: **GACS — Gerenciador para Análise e Caracterização de Componentes
  Semicondutores**.
- Autor: Ronaldo Rodrigues Godoi.

## Antes de alterar o projeto

1. Leia `docs/README.md` e os documentos canônicos relacionados à tarefa.
2. Ignore arquivos cujo nome começa com `velho`.
3. Consulte `docs/historico/` apenas para compreender a evolução, nunca como
   especificação vigente.
4. Preserve alterações preexistentes no repositório.

Ordem de autoridade:

1. decisão humana mais recente;
2. documento canônico específico;
3. este arquivo;
4. registros históricos.

## Tecnologias e arquitetura

- Java 21, JavaFX, Maven, MySQL 8 e JDBC;
- pacotes `application`, `controller`, `dao`, `model` e `util`;
- JavaFX chama controllers; controllers validam e coordenam DAOs; DAOs contêm
  todo SQL; model representa entidades;
- não criar camada `service`, ORM, Spring, Lombok ou outro framework sem decisão
  explícita;
- operações compostas usam uma conexão, `commit` no sucesso e `rollback` na
  falha.

## Modelo vigente

As oito tabelas são:

```text
Usuario
Experimento
Coluna
DadoColuna
Curva
CurvaFet
Grafico
CurvaGrafico
```

O modelo completo e os nomes oficiais estão em `docs/modelo-de-dados.md`.
Não criar outras tabelas ou entidades persistentes sem decisão arquitetural.

`PerfilUsuario` e `TipoCurvaFet` são enums. Não existe `TipoEixo`.
`SessaoUsuario` pertence a `application` e não é persistente.

## Regras técnicas

- classes e nomes do domínio em português;
- classes em PascalCase; atributos e métodos em camelCase;
- IDs Java são `Long`; medições são `Double`; datas usam `LocalDateTime`;
- entidades persistentes são classes mutáveis com getters e setters;
- SQL somente em DAO;
- não usar recursos preview;
- Javadoc curto em classes públicas e regras não óbvias;
- máximo de 50 colunas e 10.000 medidas por coluna;
- ponto ou vírgula decimal na entrada; notação científica aceita;
- caracterização de FET usa exclusivamente unidades SI.

## Validação

Ao final de alterações Java, execute:

```text
mvn clean compile
```

Adicione testes proporcionais ao risco quando houver infraestrutura disponível.
Corrija somente problemas relacionados à tarefa e informe os arquivos alterados.

## Documentação

Cada regra normativa possui uma única fonte canônica. Atualize somente os
documentos afetados. Decisões arquiteturais relevantes recebem registro em
`docs/decisoes/`; relatos cronológicos pertencem a `docs/historico/`.
