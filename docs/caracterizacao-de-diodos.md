# Caracterização de diodos

Este documento descreve o comportamento atualmente implementado em
`CaracterizacaoDiodoCtlr` e `TelaCaracterizacaoDiodo`.

## Entrada

- curva direta obrigatória;
- curva reversa/ruptura opcional, podendo ser a mesma curva completa;
- temperatura em kelvin;
- corrente de referência direta;
- intervalo de tensão do ajuste direto;
- corrente de referência de ruptura;
- intervalo de `|V_R|` para fuga;
- intervalo de corrente para estimar resistência em série.

## Região direta

O ajuste lineariza a equação de Shockley na região selecionada e exige ao menos
três pontos. O relatório apresenta tensão direta na corrente de referência,
corrente de saturação `I_s`, fator de idealidade `n`, tensão térmica, resistência
dinâmica, resistência em série estimada, retificação e `R²`.

## Região reversa

Quando há dados adequados, o relatório apresenta módulo médio e máximo da
corrente de fuga no intervalo escolhido e tensão de ruptura interpolada no
nível de corrente informado.

Na região pós-ruptura, uma regressão linear de `|I_R|` em função de `|V_R|`
fornece a inclinação, seu inverso como resistência dinâmica efetiva e `R²`.

## Persistência

A caracterização usa curvas já persistidas, mas seus parâmetros e relatório
não são armazenados em tabelas próprias nesta versão. O relatório textual pode
ser copiado pela interface.

Os detalhes históricos dos incrementos permanecem nos documentos 15, 16 e 17
da pasta `historico/`.
