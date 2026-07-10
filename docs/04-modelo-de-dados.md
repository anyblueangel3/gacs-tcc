# 04 -- Modelo de Dados do GACS

## Objetivo

Este documento descreve o modelo lógico inicial do GACS, que servirá de
base para:

-   entrada de dados;
-   geração de gráficos;
-   análise/caracterização.

O modelo foi projetado para ser simples, normalizado e evolutivo.

------------------------------------------------------------------------

# Banco de Dados

**Nome:** `DadosGACS`

------------------------------------------------------------------------

# Entidades iniciais

## Experimento

Representa a unidade central do sistema.

  Campo             Tipo lógico   Observação
  ----------------- ------------- ----------------------------
  idExperimento     Long          Identificador único
  nomeExperimento   Texto         Nome amigável
  dataExperimento   Data/Hora     Data da criação ou medição
  observacoes       Texto         Informações livres

### Cardinalidade

Um experimento possui uma ou mais colunas.

    Experimento
          |
          | 1:N
          |
       Coluna

------------------------------------------------------------------------

## Coluna

Representa uma grandeza experimental.

Campos:

  Campo           Observação
  --------------- -------------------------------------------
  idColuna        Identificador
  idExperimento   FK para Experimento
  nomeColuna      Cabeçalho apresentado ao usuário
  tipoEixo        X ou Y
  idColunaX       Referência à coluna X quando tipoEixo = Y

### Regras

Se:

    tipoEixo = X

então:

    idColunaX = NULL

Se:

    tipoEixo = Y

então:

    idColunaX deve existir

Além disso:

-   a coluna X deve pertencer ao mesmo experimento;
-   um X pode servir para vários Y;
-   cada Y possui exatamente um X principal.

------------------------------------------------------------------------

## DadoColuna

Representa um único valor medido.

  Campo            Observação
  ---------------- ------------------------
  idDado           Identificador
  idColuna         FK para Coluna
  numeroDaMedida   Índice lógico da linha
  valorMedida      Valor numérico

### Regra

A combinação

    idColuna + numeroDaMedida

deverá ser única.

------------------------------------------------------------------------

# Pareamento X--Y

O gráfico será reconstruído utilizando:

-   a referência `idColunaX`;
-   o campo `numeroDaMedida`.

Exemplo:

    VDS  (X)

    1 -> 0,0
    2 -> 0,5
    3 -> 1,0

    IDS (Y)

    1 -> 1,2E-9
    2 -> 3,7E-7
    3 -> 2,15E-5

Os pares utilizados na análise serão:

    (0,0 ; 1,2E-9)
    (0,5 ; 3,7E-7)
    (1,0 ; 2,15E-5)

------------------------------------------------------------------------

# Casos suportados

## Caso 1

Uma coluna X compartilhada.

    VDS
    IDS(VGS=1V)
    IDS(VGS=2V)
    IDS(VGS=3V)

Cada coluna Y referencia a mesma coluna X.

------------------------------------------------------------------------

## Caso 2

Cada curva possui seu próprio X.

    VDS1
    IDS1

    VDS2
    IDS2

    VDS3
    IDS3

Cada Y referencia seu respectivo X.

------------------------------------------------------------------------

# Fluxo dos dados

    Experimento
          │
          ├── Colunas
          │       │
          │       └── DadosColuna
          │
          ├── Entrada
          ├── Gráfico
          └── Caracterização

------------------------------------------------------------------------

# Decisões deliberadamente adiadas

Nesta fase **não** existirão as entidades:

-   Curva;
-   Grafico;
-   GraficoCurva;
-   Componente.

Caso o projeto exija múltiplas interpretações das mesmas colunas no
futuro, a criação da entidade Curva será reavaliada.

------------------------------------------------------------------------

# Evolução prevista

Após estabilização do núcleo serão incorporados:

1.  cadastro de componentes;
2.  cadastro de equipamentos;
3.  múltiplos gráficos;
4.  caracterização automática;
5.  relatórios;
6.  aquisição automática de dados.

Essas funcionalidades deverão reutilizar este modelo sem alterar sua
essência.
