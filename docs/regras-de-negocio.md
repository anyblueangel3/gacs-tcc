# Regras de negócio

## Usuários e experimentos

- o e-mail do usuário é único e a senha é armazenada somente como hash;
- usuários inativos não autenticam;
- somente `ADMINISTRADOR` gerencia usuários;
- todos podem consultar experimentos;
- somente o proprietário ou um administrador pode alterar ou excluir um
  experimento;
- `PESQUISADOR` e `OPERADOR` possuem as mesmas permissões nesta versão;
- `CONSULTA` não altera dados.

## Colunas e dados

- cada experimento possui no máximo 50 colunas;
- cada coluna possui no máximo 10.000 medidas;
- somente o nome da coluna é textual; células de dados contêm `Double`;
- rótulos A a AX são automáticos, únicos e permanentes;
- valores podem ser inseridos, alterados e excluídos pelo fluxo normal da
  planilha;
- a caracterização usa os dados existentes no momento do cálculo e não cria
  restrições especiais à edição.

## Curvas e gráficos

- uma curva associa exatamente uma coluna X e uma Y do mesmo experimento;
- X e Y não podem ser a mesma coluna;
- uma curva pode integrar vários gráficos;
- uma curva não pode aparecer duas vezes no mesmo gráfico;
- as curvas de um gráfico pertencem ao experimento do gráfico.

## FET

- uma `CurvaFet` especializa uma única `Curva`;
- o tipo é `SAIDA` ou `TRANSFERENCIA`;
- a tensão constante é armazenada uma única vez na especialização;
- todos os dados físicos usados pela caracterização de FET estão no SI;
- regiões de ajuste são escolhidas pelo pesquisador, com limites inclusivos;
- cada regressão exige ao menos três pontos;
- o cálculo trabalha sobre cópia ordenada e não altera os dados persistidos.
