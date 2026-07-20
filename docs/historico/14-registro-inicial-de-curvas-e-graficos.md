# 14 — Registro de curvas, gráficos e plotagem

## 1. Identificação

- **Projeto:** GACS-TCC — Geração de Aplicativo de Caracterização de Componentes Semicondutores
- **Aplicativo:** GACS — Gerenciador para Análise e Caracterização de Componentes Semicondutores
- **Data:** 13 de julho de 2026

## 2. Objetivo do incremento

Este incremento inicia o fluxo seguinte ao registro das medições:

```text
Experimento salvo
→ seleção das colunas X e Y
→ criação da curva
→ criação do gráfico
→ associação da curva ao gráfico
→ consulta e manutenção das curvas do gráfico
→ plotagem das curvas persistidas
→ exportação ou impressão do resultado
```

O comando `Novo Gráfico`, antes provisório, foi ligado ao processo real de
persistência. Ele permanece disponível apenas quando existe um experimento
aberto.

Ao longo do sprint, o escopo inicialmente previsto foi ampliado. Além do
registro da primeira curva, foram implementados a consulta e o CRUD de gráficos
e curvas, bem como a representação visual dos dados persistidos.

## 3. Fluxo implementado

Ao acionar `Novo Gráfico`, o operador informa:

- o nome do gráfico;
- o nome da primeira curva;
- a coluna utilizada como eixo X;
- a coluna utilizada como eixo Y.

As colunas são apresentadas por rótulo e nome, por exemplo `A — Tensão` e
`B — Corrente`. O sistema sugere inicialmente a primeira coluna como X e a
segunda como Y, permitindo que o operador altere ambas.

O experimento e suas colunas precisam estar salvos antes da criação do gráfico.
Essa exigência é necessária porque `Curva` referencia os identificadores
persistidos de `Coluna`.

## 4. Persistência transacional

Foi criado `GraficoCtlr`, responsável por coordenar `CurvaDAO`, `GraficoDAO` e
`CurvaGraficoDAO`. O registro completo utiliza uma única conexão e uma única
transação:

1. insere a entidade `Curva` com as colunas X e Y;
2. insere a entidade `Grafico`, vinculada ao experimento aberto;
3. insere `CurvaGrafico` com `numeroCurva = 1`.

O `commit` ocorre somente depois do sucesso das três operações. Qualquer falha
provoca `rollback`, evitando curva, gráfico ou associação incompletos.

## 5. Validações

O controller verifica:

- experimento previamente salvo;
- existência de pelo menos duas colunas persistidas;
- nomes obrigatórios e limitados a 200 caracteres;
- seleção das duas colunas;
- colunas X e Y diferentes;
- pertencimento das duas colunas ao experimento aberto;
- existência efetiva das colunas no banco no momento da transação.

## 6. Consulta e manutenção de gráficos e curvas

Foi criada a tela `Gráficos e curvas`, acessível a partir do experimento
aberto. Essa tela apresenta os gráficos pertencentes ao experimento e as curvas
associadas ao gráfico selecionado.

O fluxo permite:

- listar os gráficos salvos do experimento;
- selecionar um gráfico e consultar as curvas que o compõem;
- criar gráficos com sua primeira curva;
- incluir outras curvas em um gráfico existente;
- alterar os dados cadastrais do gráfico e das curvas;
- excluir curvas e gráficos, respeitando suas associações;
- conservar a ordem das curvas dentro de cada gráfico.

Com isso, tornou-se possível conferir visualmente se o registro foi realizado
de maneira adequada e manter um gráfico composto por diversas curvas.

## 7. Plotagem do gráfico

Foi acrescentado o comando `Plotar gráfico` na tela `Gráficos e curvas`. O
comando atua sobre o gráfico selecionado e abre uma janela própria de
pré-visualização.

A plotagem utiliza os registros persistidos de `Curva`, `Coluna` e
`DadoColuna`. Para cada curva, os valores das colunas X e Y são associados pelo
`numeroDaMedida`. Quando uma medida não possui o par correspondente no outro
eixo, ela não é incluída na série, evitando a ligação de pontos incompatíveis.

Foram implementados:

- exibição de várias curvas no mesmo gráfico;
- legenda com identificação das curvas;
- apresentação dos valores dos eixos em notação científica;
- mensagem informativa quando não existem pares X/Y disponíveis;
- pré-visualização em proporção compatível com uma folha A4 horizontal;
- gravação do resultado em arquivo PNG;
- impressão em A4 por meio do diálogo de impressão do sistema operacional.

O formato PNG foi adotado para a exportação porque preserva linhas finas,
textos, eixos e marcadores sem os artefatos de compressão característicos do
JPEG, sendo mais apropriado para gráficos técnicos.

Para permitir a conversão do gráfico JavaFX em imagem PNG, foi adicionada ao
Maven a dependência `javafx-swing`, utilizada pela classe `SwingFXUtils`.

## 8. Classes incluídas e alteradas

Foram incluídas:

```text
controller/GraficoCtlr.java
application/TelaNovoGrafico.java
application/TelaGraficos.java
application/TelaPlotagemGrafico.java
```

Foram integradas ao novo fluxo:

```text
application/MenuPrincipal.java
application/TelaPrincipal.java
application/PainelExperimento.java
dao/CurvaGraficoDAO.java
pom.xml
```

## 9. Verificação realizada

Após a inclusão da dependência necessária à exportação da imagem, o projeto foi
compilado e executado com sucesso no ambiente de desenvolvimento oficial:

```text
Java 21
JavaFX
Maven
MySQL 8.0.28
```

Foram verificados o acesso à tela de gráficos, a manutenção das curvas, a
plotagem e o funcionamento geral do sistema. Ao final do sprint, o aplicativo
estava compilando e executando corretamente.

## 10. Ajustes previstos para o próximo sprint

No próximo sprint serão revistos alguns comportamentos da interface que, embora
não impeçam o funcionamento atual, deverão operar de forma um pouco diferente
para tornar o uso mais natural e coerente.

Esses ajustes serão definidos a partir de novos testes práticos e poderão
envolver fluxo entre telas, seleção de gráficos e curvas, habilitação de botões,
mensagens, foco, atualização das listas e outros detalhes de experiência de
uso identificados durante a operação.

As alterações deverão preservar:

- os dados já persistidos;
- o relacionamento de um gráfico com várias curvas;
- o padrão transacional com `commit` integral ou `rollback` em caso de falha;
- a plotagem, a exportação em PNG e a impressão em A4 já implementadas;
- as funcionalidades anteriores do GACS.

## 11. Estado ao final do dia

O GACS encerra o dia com o ciclo básico de gráficos funcional: registra o
gráfico e suas curvas, permite consultar e manter essas associações, recupera os
dados experimentais persistidos, plota uma ou várias curvas, salva o resultado
em PNG e oferece impressão em folha A4 horizontal.

Os próximos trabalhos concentram-se no refinamento dos comportamentos da
interface e, posteriormente, na evolução dos recursos de análise e
caracterização das curvas.
