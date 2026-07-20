# 18 — Estrutura e primeira caracterização de FET

**Data:** 20/07/2026

## Objetivo do sprint

Estudar a caracterização de um FET genérico, consolidar os métodos da primeira
versão e iniciar sua implementação no GACS.

## Decisões científicas

A primeira versão utiliza curvas de saída `I_D × V_DS` e de transferência
`I_D × V_GS`, sem pressupor MOSFET, JFET ou outro modelo específico.

Foram aprovados:

- `G_DS,ômica` e `R_DS,ômica`;
- `g_ds` e `r_o`;
- tensão de joelho pela interseção dos ajustes;
- `g_m` por intervalo ou janela local de três pontos;
- ganho intrínseco `g_m r_o` para condições compatíveis;
- regressão linear com intercepto livre e no mínimo três pontos;
- advertência quando `R² < 0,95`, sem rejeição automática;
- seleção manual de intervalos inclusivos;
- uso exclusivo de unidades SI.

Corrente de baixa condução, histerese, análise térmica, detecção automática de
regiões e parâmetros dependentes de modelo ficaram fora da primeira versão.

## Modelo e persistência

Foi aprovada e implementada a oitava tabela `CurvaFet`:

```text
idCurva
tipoCurvaFet
valorTensaoConstante
```

`CurvaFet` especializa opcionalmente uma `Curva`. O tipo `SAIDA` armazena o
`V_GS` constante; `TRANSFERENCIA` armazena o `V_DS` constante.

A tabela integra somente a criação inicial de `DadosGACS`. Não foi criada
migração nem verificação complementar. O banco anterior foi excluído
manualmente e recriado com as oito tabelas.

## Documentação

A pasta `docs` foi reorganizada:

- documentos canônicos por assunto na raiz;
- decisões arquiteturais em `docs/decisoes`;
- registros cronológicos em `docs/historico`;
- índice de autoridade e manutenção em `docs/README.md`.

Os antigos documentos numerados foram preservados como história, sem autoridade
sobre o comportamento atual.

## Implementação

Foram criados ou integrados:

- `TipoCurvaFet`;
- `CurvaFet`;
- `CurvaFetDAO`;
- criação SQL de `CurvaFet`;
- `CaracterizacaoFetCtlr`;
- `TelaCaracterizacaoFet`;
- acesso pelo botão FET da seleção de componentes.

A tela permite configurar uma curva, persistir seu tipo e tensão constante,
selecionar intervalos, calcular e copiar o relatório. O cálculo não persiste
resultados nem cria novos registros de `CurvaFet`; somente o comando de salvar
insere ou atualiza a configuração única da curva.

## Teste realizado

Foi importado um conjunto sintético em unidades SI contendo:

- saída em `V_GS = 3 V`;
- transferência em `V_DS = 5 V`.

Na saída, com região ôhmica de `0 V` a `1 V` e saturação de `2,25 V` a `5 V`,
foram obtidos:

```text
G_DS,ômica = 3,000000E-03 S
R_DS,ômica = 3,333333E+02 ohm
g_ds        = 8,000000E-05 S
r_o         = 1,250000E+04 ohm
V_joelho    = 1,272260E+00 V
```

Na transferência, entre `2 V` e `4 V`:

```text
g_m = 4,240000E-03 S
R²  = 9,808730E-01
```

Os valores são coerentes com o conjunto sintético. Para as condições
compatíveis, o ganho esperado é:

```text
g_m r_o = 53
20 log10|g_m r_o| ≈ 34,49 dB
```

## Próximo passo

Integrar automaticamente curvas compatíveis de saída e transferência para
apresentar `g_m r_o` na interface e no relatório, seguido de testes com uma
família completa de curvas de FET.
