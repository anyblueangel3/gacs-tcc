# Curvas e gráficos

Uma `Curva` associa uma coluna X e uma coluna Y. Seus pontos são reconstruídos
por igualdade de `numeroDaMedida`; pares incompletos não formam ponto.

Um `Grafico` pertence a um experimento e reúne curvas por `CurvaGrafico`.
`numeroCurva` determina ordem de desenho e legenda. O registro conjunto de
gráfico, curvas e associações é transacional.

Na caracterização de FET, a curva pode receber a especialização `CurvaFet`:

| Tipo | X | Y | Condição armazenada |
|---|---|---|---|
| Saída | `V_DS` em V | `I_D` em A | `V_GS` em V |
| Transferência | `V_GS` em V | `I_D` em A | `V_DS` em V |

`Curva` permanece genérica; apenas curvas efetivamente classificadas para FET
possuem registro em `CurvaFet`.

A plotagem é exibida em janela independente e não modal. Ela permanece aberta
até ser fechada pelo usuário e pode ser movida, redimensionada, minimizada,
maximizada ou restaurada sem bloquear as demais telas do GACS.
