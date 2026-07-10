# 04 — Modelo de Dados do GACS

## Objetivo

Este documento descreve o modelo lógico adotado para o banco de dados do GACS.

O modelo organiza:

- usuários;
- experimentos;
- colunas de dados;
- valores medidos;
- curvas;
- gráficos;
- associação ordenada entre curvas e gráficos.

O banco de dados desta versão é composto por exatamente sete tabelas.

---

# Banco de Dados

**Nome:** `DadosGACS`

---

# Padrão de nomenclatura

As tabelas e colunas devem preservar exatamente os nomes definidos neste documento.

Regras gerais:

- nomes de tabelas no singular;
- nomes do domínio em português;
- atributos e colunas em `camelCase`;
- identificadores representados por `Long`;
- toda tabela que possui chave primária própria utiliza o nome `id`;
- chaves estrangeiras utilizam o padrão `id` + nome da entidade referenciada;
- enums Java não são tabelas do banco de dados nesta versão.

Exemplos:

```text
id
idExperimento
idColuna
idGrafico
idCurva
```

---

# Tabelas

## Usuario

Representa uma pessoa autorizada a utilizar o sistema.

| Campo | Tipo lógico | Observação |
|---|---|---|
| `id` | `Long` | Identificador único do usuário |
| `nome` | `String` | Nome do usuário |
| `email` | `String` | Endereço de e-mail |
| `senhaHash` | `String` | Hash da senha; a senha não deve ser armazenada em texto puro |
| `perfil` | `PerfilUsuario` | Perfil de acesso representado por enum |
| `ativo` | `Boolean` | Indica se o usuário está ativo |
| `dataCriacao` | `LocalDateTime` | Data e hora de criação |
| `dataUltimaAlteracao` | `LocalDateTime` | Data e hora da última alteração |

### Observação sobre `PerfilUsuario`

`PerfilUsuario` é um enum da aplicação e não uma tabela do banco de dados nesta versão.

---

## Experimento

Representa a unidade central do trabalho experimental.

| Campo | Tipo lógico | Observação |
|---|---|---|
| `id` | `Long` | Identificador único do experimento |
| `nomeExperimento` | `String` | Nome do experimento |
| `dataExperimento` | `LocalDateTime` | Data e hora do experimento |
| `observacoes` | `String` | Texto livre armazenado em campo de texto longo |
| `idUsuario` | `Long` | é usado para dizer de quem é um experimento |

### Cardinalidade

Um experimento pode possuir várias colunas.

```text
Experimento 1:N Coluna
```

---

## Coluna

Representa uma coluna de dados pertencente a um experimento.

| Campo | Tipo lógico | Observação |
|---|---|---|
| `id` | `Long` | Identificador único da coluna |
| `idExperimento` | `Long` | Chave estrangeira para `Experimento` |
| `rotulo` | `Short` | Código interno correspondente ao rótulo visual da coluna |
| `nomeColuna` | `String` | Nome ou cabeçalho apresentado ao usuário |

### Rótulo da coluna

A interface apresentará as colunas segundo o padrão de planilha:

```text
A, B, C, ..., Z, AA, AB, ..., AX
```

A primeira versão aceitará no máximo 50 colunas por experimento.

O campo `rotulo` utilizará um valor numérico curto, representado por `Short`, que será convertido pela aplicação para o rótulo visual correspondente.

Exemplo conceitual:

```text
1  -> A
2  -> B
26 -> Z
27 -> AA
50 -> AX
```

O rótulo:

- somente será atribuído quando a coluna passar a existir com dados;
- deverá ser único dentro do experimento;
- permanecerá associado à mesma coluna durante toda a sua existência;
- não deverá ser reutilizado por outra coluna do mesmo experimento enquanto a coluna original existir.

A conversão entre o valor numérico e o rótulo visual pertence à aplicação. Não será criada uma tabela específica para os rótulos.

---

## DadoColuna

Representa um único valor medido em determinada linha de uma coluna.

| Campo | Tipo lógico | Observação |
|---|---|---|
| `idColuna` | `Long` | Chave estrangeira para `Coluna` |
| `numeroDaMedida` | `Integer` | Número sequencial da linha ou medida |
| `valorMedida` | `Double` | Valor numérico armazenado |

### Identificação composta

`DadoColuna` não possui identificador próprio.

Cada registro é identificado pela combinação:

```text
idColuna + numeroDaMedida
```

Essa combinação deverá ser única.

### Limites

Cada coluna poderá possuir até 10.000 medidas.

A numeração das medidas deverá permanecer dentro do intervalo:

```text
1 a 10.000
```

### Ordenação

Os dados deverão ser recuperados preferencialmente em ordem crescente de `numeroDaMedida`.

---

## Curva

Representa a associação explícita entre uma coluna X e uma coluna Y.

| Campo | Tipo lógico | Observação |
|---|---|---|
| `id` | `Long` | Identificador único da curva |
| `nome` | `String` | Nome da curva |
| `idColunaX` | `Long` | Chave estrangeira para a coluna utilizada como eixo X |
| `idColunaY` | `Long` | Chave estrangeira para a coluna utilizada como eixo Y |

### Regras

- `idColunaX` e `idColunaY` devem referenciar colunas existentes;
- uma curva não pode utilizar a mesma coluna como X e Y;
- as duas colunas da curva devem pertencer ao mesmo experimento;
- uma coluna pode participar de várias curvas;
- uma coluna pode atuar como X em uma curva e como Y em outra;
- uma mesma coluna X pode ser compartilhada por várias curvas.

### Pareamento dos pontos

Os pontos da curva são formados pelo pareamento dos valores que possuem o mesmo `numeroDaMedida`.

Exemplo:

```text
Coluna X, medida 1 <-> Coluna Y, medida 1
Coluna X, medida 2 <-> Coluna Y, medida 2
Coluna X, medida 3 <-> Coluna Y, medida 3
```

Assim, a curva é reconstruída como:

```text
(x1, y1)
(x2, y2)
(x3, y3)
```

---

## Grafico

Representa uma composição gráfica salva pelo usuário.

| Campo | Tipo lógico | Observação |
|---|---|---|
| `id` | `Long` | Identificador único do gráfico |
| `nome` | `String` | Nome do gráfico |

Um gráfico poderá conter uma ou várias curvas.

Uma mesma curva poderá ser utilizada em gráficos diferentes.

---

## CurvaGrafico

Representa a associação ordenada entre uma curva e um gráfico.

| Campo | Tipo lógico | Observação |
|---|---|---|
| `idGrafico` | `Long` | Chave estrangeira para `Grafico` |
| `numeroCurva` | `Integer` | Número sequencial da curva dentro do gráfico |
| `idCurva` | `Long` | Chave estrangeira para `Curva` |

### Identificação e ordenação

`CurvaGrafico` não possui identificador artificial próprio.

A combinação:

```text
idGrafico + numeroCurva
```

identifica a posição de uma curva dentro do gráfico e deverá ser única.

A combinação:

```text
idGrafico + idCurva
```

também deverá ser única, impedindo que a mesma curva seja adicionada duas vezes ao mesmo gráfico.

### Função de `numeroCurva`

`numeroCurva` define:

- a ordem de apresentação das curvas;
- a ordem das séries na legenda;
- a sequência de processamento na interface;
- a possibilidade de reorganização futura das curvas dentro do gráfico.

A numeração deverá começar em 1 e crescer sequencialmente dentro de cada gráfico.

---

# Relacionamentos

```text
Experimento 1:N Coluna

Coluna 1:N DadoColuna

Coluna 1:N Curva, no papel de eixo X

Coluna 1:N Curva, no papel de eixo Y

Grafico N:N Curva
```

A relação muitos-para-muitos entre `Grafico` e `Curva` é representada por `CurvaGrafico`.

---

# Visão estrutural

```text
Usuario

Experimento
    |
    +-- Coluna
           |
           +-- DadoColuna

Curva
    +-- idColunaX -> Coluna
    +-- idColunaY -> Coluna

Grafico
    |
    +-- CurvaGrafico
             |
             +-- Curva
```

---

# Regras de integridade principais

1. Um `DadoColuna` deve pertencer a uma coluna existente.
2. Não pode haver dois dados com o mesmo `idColuna` e `numeroDaMedida`.
3. Cada coluna aceita no máximo 10.000 medidas.
4. Cada experimento aceita no máximo 50 colunas.
5. O rótulo da coluna deve ser único dentro do experimento.
6. O rótulo atribuído a uma coluna não deve ser alterado.
7. As colunas X e Y de uma curva devem pertencer ao mesmo experimento.
8. Uma curva não pode associar uma coluna a ela mesma.
9. Um gráfico pode conter várias curvas.
10. Uma curva pode pertencer a vários gráficos.
11. Uma mesma curva não pode aparecer duas vezes no mesmo gráfico.
12. `numeroCurva` deve ser único e sequencial dentro de cada gráfico.

---

# Enums

Enums representam conjuntos controlados de valores na aplicação.

Nesta versão, enums não serão transformados em tabelas do banco de dados.

Exemplo confirmado:

```text
PerfilUsuario
```

Outros enums poderão ser criados quando houver um conjunto pequeno, estável e fechado de valores.

Um enum somente deverá se tornar uma tabela se, futuramente, seus valores precisarem ser:

- cadastrados pelo usuário;
- alterados sem recompilar a aplicação;
- associados a atributos adicionais;
- administrados dinamicamente.

---

# Entidades desta versão

O banco de dados trabalhará com exatamente estas sete tabelas:

1. `Usuario`
2. `Experimento`
3. `Coluna`
4. `DadoColuna`
5. `Curva`
6. `Grafico`
7. `CurvaGrafico`

Nenhuma nova tabela deverá ser incluída nesta versão sem decisão arquitetônica explícita.

---

# Decisões deliberadamente adiadas

Não serão incluídas nesta versão tabelas específicas para:

- componente semicondutor;
- equipamento;
- laboratório;
- unidade de medida;
- rótulo de coluna;
- resultado de caracterização;
- relatório;
- aquisição automática de dados.

Essas possibilidades poderão ser avaliadas em versões futuras sem alterar a responsabilidade das sete tabelas atuais.
