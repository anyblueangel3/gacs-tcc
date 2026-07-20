# 17 — Caracterização detalhada da região reversa do diodo

## 1. Identificação

- **Projeto:** GACS-TCC — Geração de Aplicativo de Caracterização de Componentes Semicondutores
- **Aplicativo:** GACS — Gerenciador para Análise e Caracterização de Componentes Semicondutores
- **Data:** 19 de julho de 2026
- **Tecnologias:** Java 21, JavaFX 21, Maven, JDBC e MySQL 8.0.28

## 2. Objetivo do incremento

Este incremento continua a caracterização de diodo documentada nos arquivos 15
e 16. A região reversa deixou de ser apresentada apenas por estatísticas globais
e por uma tensão de ruptura isolada. O relatório passou a separar:

```text
corrente de fuga
→ transição para o joelho
→ início operacional do joelho
→ tensão de ruptura
→ região pós-ruptura
```

Também foram aprimoradas a nomenclatura e as explicações físicas do relatório.
Nenhuma tabela, entidade ou operação de persistência foi criada.

## 3. Critérios operacionais

A corrente reversa de referência informada pelo operador continua definindo a
tensão de ruptura `V_BR`. A partir dela, foi estabelecido um nível auxiliar
explícito para o início operacional do joelho:

```text
início operacional do joelho = 10% de |I_R de referência|
tensão de ruptura V_BR = 100% de |I_R de referência|
```

Essas porcentagens são critérios operacionais de análise. Elas não afirmam que
exista uma fronteira física descontínua entre as regiões da curva.

## 4. Corrente de fuga separada

A média e o máximo da corrente de fuga são calculados no intervalo de módulo da
tensão reversa informado pelo operador. A tela apresenta dois campos:

```text
|V_R| mínimo para fuga (V)
|V_R| máximo para fuga (V)
```

Os valores iniciais são `1 V` e `35 V`, adequados à curva sintética completa,
cuja fuga permanece próxima de `10^-12 A` nessa faixa. O operador deve ajustar
o intervalo de acordo com a curva medida e excluir a transição para o joelho.
Isso permite caracterizar a fuga mesmo quando os dados não alcançam a ruptura.

O relatório registra o intervalo empregado, a quantidade de pontos, a média e
o máximo do módulo da corrente. Também mantém separadamente as estatísticas de
toda a região reversa para que o operador possa distinguir as duas grandezas.

## 5. Identificação do joelho

O início operacional do joelho, `V_K`, é obtido por interpolação linear no nível
de corrente equivalente a 10% da referência de ruptura. A largura operacional
do joelho é:

```text
Delta V_joelho = V_BR - V_K
```

Quando a curva não alcança algum dos níveis necessários, o resultado
correspondente é apresentado como indisponível. O sistema não extrapola dados.

## 6. Região pós-ruptura

São considerados pós-ruptura os pontos que satisfazem simultaneamente:

```text
|I_R| >= corrente de referência
|V_R| >= V_BR
```

Com pelo menos três pontos, é realizada uma regressão linear de `|I_R|` em
função de `|V_R|`. O relatório apresenta:

```text
inclinação pós-ruptura = d|I_R| / d|V_R|
resistência dinâmica efetiva pós-ruptura = d|V_R| / d|I_R|
R² do ajuste pós-ruptura
quantidade de pontos utilizada
```

A resistência dinâmica efetiva é calculada como o inverso da inclinação média
positiva obtida pela regressão em toda a faixa pós-ruptura selecionada. Ela não
representa uma derivada local em um único ponto. Se a regressão não puder ser
realizada ou não apresentar inclinação positiva finita, os resultados ficam
indisponíveis.

## 7. Histerese

O GACS não declara a existência ou ausência de histerese a partir de uma única
sequência sem identificação do sentido da varredura. Uma análise de histerese
exige dados de ida e volta distinguíveis e comparação das correntes em tensões
equivalentes. Essa limitação passou a constar expressamente no relatório.

## 8. Nomenclatura e explicação física

O relatório passou a empregar as notações:

```text
I_s   corrente de saturação
V_T   tensão térmica, V_T = kT/q
R_s   resistência em série equivalente
V_K   início operacional do joelho
V_BR  tensão de ruptura pelo critério de corrente
```

Também distingue a resistência dinâmica direta, a resistência em série
equivalente e a resistência dinâmica pós-ruptura. O método explica que `R_s`
pode reunir contribuições internas do material, contatos e terminais e que sua
estimativa diferencial usa:

```text
R_s = dV/dI - n V_T/I
```

## 9. Arquivos alterados

```text
src/main/java/br/uel/gacs/application/TelaCaracterizacaoDiodo.java
src/main/java/br/uel/gacs/controller/CaracterizacaoDiodoCtlr.java
docs/17-caracterizacao-detalhada-da-regiao-reversa.md
```

## 10. Validação com a curva completa

Para a curva sintética completa de 1.000 pontos e corrente de referência de
`1 mA`, os critérios recuperam aproximadamente:

```text
intervalo de fuga em |V_R|:          1,00000 V a 35,00000 V
pontos utilizados na fuga:                         236
corrente de fuga média:               1,02157E-12 A
corrente de fuga máxima:              1,91823E-12 A
V_K:                                48,33331 V
V_BR:                               49,99987 V
largura operacional do joelho:       1,66656 V
pontos no ajuste pós-ruptura:               53
inclinação pós-ruptura:               4,20326E-3 S
resistência dinâmica efetiva pós-ruptura: 2,37911E2 ohm
R² pós-ruptura:                       9,38061E-1
```

O novo intervalo recupera corretamente o patamar de fuga de aproximadamente
`10^-12 A` utilizado na geração da curva sintética e não incorpora a corrente
crescente da aproximação do joelho.

O `R²` inferior ao ajuste direto é coerente com a região pós-ruptura sintética
não ser perfeitamente linear em toda a faixa selecionada.

## 11. Estado ao final do incremento

O relatório de diodo passa a oferecer uma leitura mais completa e fisicamente
organizada da polarização reversa:

```text
estatísticas reversas globais
→ corrente de fuga isolada
→ início e largura do joelho
→ V_BR
→ inclinação e resistência dinâmica efetiva pós-ruptura
```

Os cálculos permanecem em memória, usam as curvas persistidas e respeitam o
modelo oficial de sete tabelas do GACS.
