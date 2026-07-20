# 16 — Tensão de ruptura, curva completa e resistência em série

## 1. Identificação

- **Projeto:** GACS-TCC — Geração de Aplicativo de Caracterização de Componentes Semicondutores
- **Aplicativo:** GACS — Gerenciador para Análise e Caracterização de Componentes Semicondutores
- **Período do incremento:** 18 e 19 de julho de 2026
- **Tecnologias:** Java 21, JavaFX 21, Maven, JDBC e MySQL 8.0.28

## 2. Objetivo do incremento

Este incremento amplia a caracterização de diodo iniciada no documento 15. O
GACS passa a reconhecer e calcular parâmetros de regiões que não estavam
presentes nas primeiras curvas sintéticas:

```text
polarização reversa anterior à ruptura
→ joelho de ruptura
→ região de ruptura
→ passagem pela origem
→ região exponencial direta
→ região direta influenciada pela resistência em série
```

Foram implementados e validados:

- tensão de ruptura por corrente reversa de referência;
- uso de uma curva completa como fonte das regiões direta e reversa;
- correção da contagem de pontos da polarização direta;
- descrição mais precisa das estatísticas da região reversa;
- estimativa diferencial da resistência em série `Rs`;
- novos parâmetros e resultados no relatório de caracterização.

As alterações preservam as sete tabelas oficiais. Nenhuma entidade ou tabela
foi criada, pois os cálculos continuam sendo realizados em memória a partir das
curvas persistidas.

## 3. Manutenção da mesma tela de caracterização

A tensão de ruptura e a resistência em série foram integradas à tela de
caracterização de diodo existente. Não foi criada uma tela isolada para cada
parâmetro, porque todos pertencem à caracterização elétrica do mesmo componente.

A seleção anteriormente apresentada como curva reversa passou a indicar de
forma explícita:

```text
Curva reversa/ruptura
```

Essa curva continua opcional. Quando informada, pode fornecer:

- corrente em polarização reversa;
- razão de retificação;
- tensão de ruptura.

Uma única curva completa também pode ser utilizada sem alteração estrutural. O
operador cadastra uma curva com tensões negativas e positivas e seleciona essa
mesma curva nos campos de curva direta e curva reversa/ruptura. O controller
separa os pontos pelo sinal de tensão e corrente.

O programa permanece igualmente compatível com duas curvas distintas, uma
direta e outra reversa, quando as medições tiverem sido realizadas em
varreduras separadas.

## 4. Critério para a tensão de ruptura

A tensão de ruptura não é obtida apenas pela existência de tensões negativas.
A curva precisa alcançar a região em que o módulo da corrente reversa cresce
acentuadamente.

Nesta versão, o operador informa uma corrente reversa de referência positiva:

```text
Corrente de referência da ruptura (A)
```

O valor inicial é:

```text
1,0 × 10⁻³ A
```

O GACS procura dois pontos consecutivos da varredura reversa que envolvam essa
corrente em módulo. A tensão é calculada por interpolação linear:

```text
V = V1 + (Iref - I1)(V2 - V1) / (I2 - I1)
```

O resultado apresentado é o módulo da tensão:

```text
VBR = |V em |IR| = Iref|
```

Se a curva não alcançar a corrente escolhida, o sistema não extrapola nem
inventa um resultado. O relatório informa:

```text
não disponível: a curva não alcança essa corrente de referência
```

Esse comportamento foi verificado inicialmente com a antiga curva reversa,
que permanecia próxima de `10⁻¹² A` e não continha ruptura.

## 5. Curva reversa sintética com ruptura

Para validar o novo cálculo, foi produzida uma curva reversa com 100 pontos. A
distribuição dos pontos foi concentrada nas proximidades do joelho e adotou uma
ruptura simulada próxima de:

```text
VBR = 50 V
```

Com corrente de referência de `1 mA`, o GACS calculou:

```text
VBR = 4,999659 × 10¹ V
```

O primeiro teste utilizou por engano a curva reversa antiga. O relatório
corretamente declarou a ruptura indisponível, pois aquela série não alcançava a
corrente de referência. Depois da seleção da curva `Ruptura`, o valor esperado
foi obtido. O episódio também confirmou a validação de ausência de dados.

## 6. Correções nas contagens e nos rótulos reversos

O campo `Pontos da curva direta` anteriormente utilizava a quantidade total de
pontos carregados da curva selecionada. Isso não era perceptível com uma curva
exclusivamente direta, mas produziria uma contagem incorreta ao utilizar uma
curva completa.

A contagem passou a considerar somente pontos que satisfazem:

```text
V > 0 e I > 0
```

O ponto na origem e os pontos reversos não são contabilizados como pontos da
polarização direta.

Também foram corrigidos os rótulos das estatísticas reversas. Quando uma curva
alcança a ruptura, a média e o máximo incluem as correntes dessa região e não
devem ser interpretados automaticamente como corrente de fuga. O relatório
passou a utilizar:

```text
Módulo médio da corrente em toda a região reversa
Módulo máximo da corrente em toda a região reversa
```

A caracterização detalhada da corrente de fuga e da região pós-ruptura fica
reservada para um incremento posterior.

## 7. Curva completa com mil pontos

Foi produzida uma única curva sintética `I × V` com 1.000 pontos, organizada da
seguinte forma:

```text
599 pontos com tensão negativa
1 ponto na origem
400 pontos com tensão positiva
```

A série contém:

- corrente de fuga reversa próxima de `10⁻¹² A`;
- joelho e região de ruptura próximos de `-50 V`;
- passagem pela origem;
- região exponencial direta;
- região direta de corrente elevada;
- resistência em série simulada de `10 ohm`.

Os parâmetros fundamentais empregados foram:

```text
Is = 1,0 × 10⁻¹² A
n  = 1,8
T  = 300 K
Rs = 10 ohm
VBR = 50 V
```

A curva foi cadastrada uma única vez com uma coluna de tensão e uma coluna de
corrente. Na caracterização, a mesma curva foi selecionada nos dois campos. O
teste confirmou que o GACS separa corretamente as regiões sem exigir um novo
modo de interface.

## 8. Significado da resistência em série

A resistência `Rs` representa a resistência em série equivalente do modelo do
diodo. Ela pode reunir contribuições do material semicondutor, contatos,
terminais e conexões internas. Não significa necessariamente um resistor
externo acrescentado intencionalmente ao circuito de medição.

Com `Rs`, a tensão aplicada ao diodo real é representada por:

```text
V = VD + I Rs
```

e o modelo assume a forma implícita:

```text
I = Is [exp((V - I Rs) / (n Vt)) - 1]
```

Um resistor externo pode ser utilizado no laboratório para limitar ou medir a
corrente, mas deve ser distinguido da resistência interna equivalente. Se a
coluna de tensão representar a tensão total do circuito, a estimativa poderá
incorporar resistores, cabos e contatos externos. Para caracterizar o diodo, é
preferível registrar a tensão efetivamente medida sobre o componente.

O GACS continua utilizável quando `Rs` é desprezível ou quando a curva não
alcança a faixa de corrente necessária. Nesse caso, a estimativa pode ficar
próxima de zero ou ser apresentada como indisponível, sem impedir os demais
cálculos.

## 9. Estimativa diferencial de Rs

Derivando o modelo direto, obtém-se aproximadamente:

```text
dV/dI = n Vt / I + Rs
```

Portanto:

```text
Rs = dV/dI - n Vt / I
```

O programa calcula `dV/dI` por diferença central usando o ponto anterior e o
ponto posterior da curva. Para reduzir a dependência de um único ponto, `Rs` é
calculada em todos os pontos pertencentes ao intervalo de corrente escolhido e
o resultado final é a média das estimativas finitas e não negativas.

A tela recebeu os campos:

```text
Corrente mínima para Rs (A): 1E-3
Corrente máxima para Rs (A): 1E-2
```

As validações exigem valores finitos, positivos e uma corrente mínima menor que
a máxima. Se a curva não fornecer pontos suficientes, o relatório declara que
o resultado não está disponível no intervalo dos dados.

## 10. Validação numérica da curva completa

Com a mesma curva selecionada como direta e reversa/ruptura, o relatório final
apresentou:

```text
Pontos da curva direta:                    400
Pontos utilizados no ajuste:              217
Pontos reversos utilizados:               599
Is:                                       9,990709 × 10⁻¹³ A
n:                                        1,800219
Vf em 1 mA:                               0,9743167 V
Resistência dinâmica em 1 mA:             55,97447 ohm
Rs:                                       9,997580 ohm
Pontos utilizados na estimativa de Rs:    66
R²:                                       0,9999992
VBR em 1 mA:                              49,99987 V
Razão de retificação em ±0,8 V:           2,909506 × 10⁷
```

A resistência em série conhecida da simulação era `10 ohm`. O erro relativo da
estimativa foi:

```text
|9,997580 - 10| / 10 × 100% ≈ 0,0242%
```

A tensão de ruptura conhecida era `50 V`. A diferença absoluta foi de
aproximadamente `0,00013 V`.

Os resultados validam conjuntamente:

- separação de uma curva completa por sinais;
- regressão linear da região exponencial;
- interpolação da tensão direta;
- cálculo da resistência dinâmica;
- estimativa diferencial de `Rs`;
- interpolação da tensão de ruptura;
- razão de retificação;
- contagens corrigidas das regiões direta e reversa.

## 11. Relatório ampliado

O relatório passou a apresentar, na polarização direta:

```text
Resistência em série Rs
Faixa de corrente utilizada para Rs
Quantidade de pontos utilizados na estimativa de Rs
```

Na seção de método, passou a registrar que:

```text
Rs foi estimada pela média de dV/dI - nVt/I na faixa de corrente informada.
```

Na polarização reversa, passou a apresentar a tensão de ruptura acompanhada da
corrente de referência que define o critério.

O relatório permanece calculado em memória e pode ser copiado. A persistência
ou o histórico de relatórios não faz parte deste incremento.

## 12. Arquivos de código alterados

As alterações deste incremento ficaram concentradas em:

```text
src/main/java/br/uel/gacs/application/TelaCaracterizacaoDiodo.java
src/main/java/br/uel/gacs/controller/CaracterizacaoDiodoCtlr.java
```

`TelaCaracterizacaoDiodo` recebeu os novos campos e encaminha seus valores ao
controller. `CaracterizacaoDiodoCtlr` executa as validações, separa as regiões,
calcula `VBR` e `Rs` e compõe o relatório ampliado.

Não houve alteração em:

- entidades de `model`;
- classes DAO;
- estrutura do banco;
- regras de propriedade dos experimentos;
- persistência das curvas;
- fluxo de criação de gráficos.

## 13. Decisões consolidadas

Ao final deste incremento, ficam registradas as seguintes decisões:

1. a mesma tela concentra os parâmetros da caracterização de diodo;
2. a curva reversa pode ou não conter ruptura;
3. `VBR` é definida por corrente reversa de referência e interpolação;
4. o sistema não extrapola `VBR` quando os dados não alcançam o critério;
5. uma curva completa pode ser selecionada nos dois campos existentes;
6. curvas direta e reversa separadas continuam aceitas;
7. `Rs` é uma resistência em série equivalente, não obrigatoriamente externa;
8. `Rs` é estimada por método diferencial em uma faixa de corrente;
9. os cálculos utilizam os mesmos dados persistidos usados nos gráficos;
10. nenhum resultado calculado é persistido nesta fase.

## 14. Limitações e próximos passos

Permanecem para incrementos futuros:

- caracterização separada da corrente de fuga anterior à ruptura;
- resistência dinâmica e inclinação na região pós-ruptura;
- análise detalhada do joelho e possível histerese;
- ajuste não linear simultâneo de `Is`, `n` e `Rs`;
- opção visual para habilitar ou desabilitar a estimativa de `Rs`;
- comparação de curvas medidas em várias temperaturas;
- análise de capacitância a partir de dados `C × V`;
- caracterização de FET;
- testes com dados experimentais reais e ruído de medição;
- persistência, exportação ou impressão própria dos relatórios.

## 15. Estado ao final do incremento

O GACS passa a caracterizar, a partir de uma curva completa ou de curvas
separadas, parâmetros importantes das regiões direta e reversa de um diodo:

```text
Vf, Is, n, R²
→ resistência dinâmica
→ resistência em série Rs
→ estatísticas da região reversa
→ tensão de ruptura VBR
→ razão de retificação
```

Os testes sintéticos recuperaram com alta precisão os parâmetros conhecidos da
simulação. O incremento demonstra que a arquitetura atual suporta análises
físicas progressivamente mais completas sem modificar o modelo persistente e
sem duplicar os dados experimentais.
