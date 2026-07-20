# Modelo de dados

O banco de dados chama-se `DadosGACS` e possui oito tabelas vigentes.

## Convenções

- tabelas no singular e nomes do domínio em português;
- chave primária própria denominada `id`;
- chave estrangeira denominada `id` + entidade referenciada;
- identificadores Java representados por `Long`;
- valores experimentais representados por `Double`;
- enums Java não geram tabelas.

## Tabelas

### `Usuario`

```text
id, nome, email, senhaHash, perfil, ativo,
dataCriacao, dataUltimaAlteracao
```

### `Experimento`

```text
id, nomeExperimento, dataExperimento, observacoes, idUsuario
```

Cada experimento pertence obrigatoriamente a um usuário.

### `Coluna`

```text
id, idExperimento, rotulo, nomeColuna
```

`rotulo` é um `Short` único no experimento, de 1 a 50, exibido como A até AX.
O rótulo permanece associado à coluna; `nomeColuna` é seu cabeçalho editável.

### `DadoColuna`

```text
idColuna, numeroDaMedida, valorMedida
```

Possui chave composta `(idColuna, numeroDaMedida)`. A medida varia de 1 a
10.000 e o valor é numérico.

### `Curva`

```text
id, nome, idColunaX, idColunaY
```

Associa duas colunas distintas do mesmo experimento. Os pontos X e Y são
pareados por `numeroDaMedida`.

### `CurvaFet`

```text
idCurva, tipoCurvaFet, valorTensaoConstante
```

É uma especialização opcional, em relação 1:0..1 com `Curva`. `idCurva` é ao
mesmo tempo chave primária e estrangeira.

`tipoCurvaFet` é representado pelo enum `TipoCurvaFet`:

- `SAIDA`: curva `I_D × V_DS`; `valorTensaoConstante` representa `V_GS`;
- `TRANSFERENCIA`: curva `I_D × V_GS`; `valorTensaoConstante` representa
  `V_DS`.

Todas as tensões e correntes são informadas em unidades SI: volt e ampere.

### `Grafico`

```text
id, idExperimento, nome
```

Cada gráfico pertence a um experimento.

### `CurvaGrafico`

```text
idGrafico, numeroCurva, idCurva
```

Possui chave composta `(idGrafico, numeroCurva)` e unicidade de
`(idGrafico, idCurva)`. `numeroCurva` começa em 1 e define a ordem.

## Relacionamentos

```text
Usuario 1:N Experimento
Experimento 1:N Coluna
Experimento 1:N Grafico
Coluna 1:N DadoColuna
Coluna 1:N Curva, como X ou Y
Curva 1:0..1 CurvaFet
Grafico N:N Curva, por CurvaGrafico
```

Não existem nesta versão tabelas para unidade, resultado de caracterização,
relatório, componente, equipamento ou laboratório.
