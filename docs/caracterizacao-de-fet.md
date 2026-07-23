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
- na integração, o operador informa um `V_GS` de referência; deve existir uma
  curva de saída configurada nesse valor e o `V_DS` da transferência deve estar
  no intervalo de saturação usado em `r_o`;
- o ganho é classificado como local quando usa a janela de três pontos e como
  efetivo quando `g_m` representa a regressão de um intervalo.

Inclinação zero produz resistência teoricamente infinita. Inclinações negativas
são preservadas e advertidas. Corrente constante torna `R²` indefinido. Não há
arredondamento intermediário.

## Procedimento

Para as curvas de saída, informar `V_GS` na configuração persistente e, na
tabela de análise, selecionar as curvas desejadas e os intervalos ôhmico e de
saturação próprios de cada uma. A tabela apresenta cinco linhas visíveis e
rolagem vertical para qualquer quantidade de curvas. O relatório é ordenado por
`V_GS` e preserva os resultados e advertências de cada curva selecionada.

O `CheckBox` **Analisar** inclui ou ignora cada curva de saída somente na
execução corrente. Os intervalos e a seleção permanecem em memória enquanto a
tela estiver aberta e não são persistidos.

Para a curva de transferência, informar `V_DS`, selecionar a curva em um
formulário independente e escolher o cálculo de `g_m`. A transferência pode ser
analisada sozinha, sem exigir curva de saída. Curvas de saída são exigidas
somente nos cálculos que dependem delas, como o ganho intrínseco. A análise
ordena uma cópia dos pares pelo eixo X e nunca modifica a planilha.

## Fora da primeira versão

- corrente de fuga ou baixa condução;
- histerese;
- dependência automática com a temperatura;
- mapas bidimensionais;
- detecção automática das regiões;
- parâmetros dependentes de modelo, como `V_th`, `K` ou mobilidade.

A temperatura poderá ser informada como condição experimental, em kelvin, sem
análise térmica automática.

## Estado de implementação em 23/07/2026

Estão implementados:

- configuração persistente de curvas de saída e transferência em `CurvaFet`;
- análise individual da região ôhmica e da saturação;
- tensão de joelho operacional;
- transcondutância por intervalo e por janela local de três pontos;
- relatório textual e cópia para a área de transferência.
- tabela transitória com seleção e intervalos individuais por curva de saída;
- análise conjunta de uma ou mais curvas de saída selecionadas;
- formulário e análise independente da curva de transferência.

O ganho intrínseco está integrado à interface: a curva de transferência
selecionada é combinada automaticamente com a curva de saída configurada no
`V_GS` de referência, respeitando a compatibilidade do intervalo de saturação.
