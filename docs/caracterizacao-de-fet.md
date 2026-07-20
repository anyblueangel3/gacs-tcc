# Caracterização de FET — primeira versão

## Escopo

A caracterização é genérica e não pressupõe MOSFET, JFET ou um modelo físico
específico. O pesquisador seleciona os intervalos; a aplicação não detecta
automaticamente as regiões.

## Resultados

| Origem | Resultado |
|---|---|
| Região ôhmica da saída | `G_DS,ômica` e `R_DS,ômica` |
| Saturação da saída | `g_ds` e `r_o` |
| Interseção dos ajustes | tensão de joelho operacional |
| Curva de transferência | `g_m` local ou por intervalo |
| Condições compatíveis | ganho intrínseco `g_m r_o` |

Cada resultado informa intervalo, quantidade de pontos, equação, `R²`, método
e advertências aplicáveis.

## Métodos aprovados

Os ajustes usam regressão linear com intercepto livre:

```text
y = a x + b
```

- mínimo de três pontos;
- alerta, sem rejeição, quando `R² < 0,95`;
- limites dos intervalos inclusivos;
- `R_DS,ômica = 1/G_DS,ômica`;
- `r_o = 1/g_ds`;
- joelho pela interseção das retas ôhmica e de saturação;
- `g_m` por regressão no intervalo ou janela móvel de três pontos;
- a janela local não calcula os dois extremos;
- `g_m r_o` somente combina condições coincidentes de `V_GS` e `V_DS`.

Inclinação zero produz resistência teoricamente infinita. Inclinações negativas
são preservadas e advertidas. Corrente constante torna `R²` indefinido. Não há
arredondamento intermediário.

## Procedimento

Para curva de saída, informar `V_GS`, selecionar os intervalos ôhmico e de
saturação e executar os ajustes. Para curva de transferência, informar `V_DS`
e selecionar o cálculo de `g_m`. A análise ordena uma cópia dos pares pelo eixo
X e nunca modifica a planilha.

## Fora da primeira versão

- corrente de fuga ou baixa condução;
- histerese;
- dependência automática com a temperatura;
- mapas bidimensionais;
- detecção automática das regiões;
- parâmetros dependentes de modelo, como `V_th`, `K` ou mobilidade.

A temperatura poderá ser informada como condição experimental, em kelvin, sem
análise térmica automática.

## Estado de implementação em 20/07/2026

Estão implementados:

- configuração persistente de curvas de saída e transferência em `CurvaFet`;
- análise individual da região ôhmica e da saturação;
- tensão de joelho operacional;
- transcondutância por intervalo e por janela local de três pontos;
- relatório textual e cópia para a área de transferência.

O núcleo matemático de `g_m r_o` está implementado, mas sua seleção automática
entre curvas compatíveis e sua inclusão na interface e no relatório permanecem
pendentes.
