# 12 — Fluxo de experimentos e entrada de dados

## 1. Objetivo

Este documento define o fluxo inicial de trabalho científico do GACS, desde a
escolha ou criação de um experimento até a entrada de dados, a composição de
curvas e gráficos e o salvamento transacional.

## 2. Abertura e atalhos

Depois da autenticação, a tela principal deverá apresentar os experimentos
acessíveis ao usuário e atalhos para:

- criar um novo experimento;
- abrir um experimento existente;
- digitar dados;
- colar dados provenientes de uma planilha;
- importar dados de arquivo CSV.

Os três modos de entrada utilizam a mesma tela de experimento e diferem apenas
na ação inicial oferecida ao operador.

A aplicação utilizará uma única janela principal. A lista inicial será
substituída pelo painel do experimento quando um trabalho for aberto ou criado;
`Fechar Experimento` devolverá o usuário à lista.

Usuários comuns verão seus próprios experimentos. Administradores verão todos
os experimentos, sempre com o nome do proprietário indicado na listagem. Um
experimento poderá ser aberto por botão ou por duplo clique.

`Novo Gráfico` permanecerá visível, porém desabilitado enquanto nenhum
experimento estiver aberto.

## 3. Experimento em memória

Ao iniciar um novo trabalho, será criado um objeto `Experimento` em memória. O
usuário autenticado e a data e hora atuais serão preenchidos automaticamente.
O banco somente será alterado quando o operador escolher salvar.

Os campos de identificação permanecerão visíveis durante a digitação, colagem,
importação e preparação de gráficos:

- nome do experimento, inicialmente `Experimento sem título`;
- data e hora do experimento;
- responsável obtido da sessão autenticada;
- observações editáveis.

As observações serão editadas em um `TextArea`. O campo SQL atual é `TEXT` e
comporta relatórios da ordem de mil palavras. Ele não deve receber limite
artificial menor na interface.

## 4. Estado e descarte

A tela distinguirá os estados `NOVO`, `SALVO`, `ALTERADO` e `SALVANDO`.

- Para um experimento novo, a ação será denominada `Descartar Experimento`.
- Para um experimento já persistido, será denominada `Descartar Alterações`.
- Toda ação de descarte de conteúdo não salvo exigirá confirmação explícita.
- Ao fechar uma tela alterada, o usuário deverá poder salvar, descartar ou
  cancelar o fechamento.

## 5. Grade de dados

A digitação ocorrerá diretamente nas células editáveis da tabela. Não será
criado um `TextField` permanente para cada célula.

A navegação seguirá inicialmente estas regras:

- `Enter`: próxima linha da mesma coluna;
- `Shift + Enter`: linha anterior da mesma coluna;
- `Tab`: próxima coluna;
- `Shift + Tab`: coluna anterior;
- setas: navegação livre;
- `Delete`: limpa o valor selecionado.

A grade deverá ser virtualizada e separar os dados numéricos dos componentes
visuais. O modelo admite até 50 colunas e 10.000 medidas por coluna, totalizando
até 500.000 valores. Não deverão ser criados componentes JavaFX individuais
para células que não estejam visíveis.

Colagens e importações grandes deverão ser validadas antes da inclusão,
processadas fora da thread gráfica e persistidas em lotes.

Ao acionar `Colar de Planilha`, o GACS lerá imediatamente o texto presente na
área de transferência. O bloco somente será colocado na grade se possuir linhas
retangulares, respeitar os limites de 50 colunas e 10.000 medidas e contiver
valores numéricos válidos, admitindo uma primeira linha de cabeçalhos.

## 6. Curvas e gráficos

Na experiência do operador, a curva será criada durante a montagem de um
gráfico, sem necessidade de um cadastro isolado de curvas.

Internamente, a curva continuará sendo uma associação reutilizável entre uma
coluna X e uma coluna Y. `CurvaGrafico` determinará em quais gráficos a curva é
apresentada e em qual ordem.

O GACS não armazenará imagens de gráficos. Serão persistidos somente dados
numéricos, nomes, colunas, curvas, gráficos e associações. Ao abrir um
experimento, o gráfico será reconstruído a partir dos dados do banco.

Os gráficos pertencentes ao experimento serão apresentados em uma lista quando
o experimento estiver aberto. A possibilidade de abrir o desenho em uma janela
própria e exportá-lo como arquivo de imagem será decidida em etapa futura e não
fará parte do modelo SQL.

## 7. Salvamento transacional

O salvamento completo utilizará uma única conexão e uma única transação para
persistir, conforme existirem:

```text
Experimento
→ Colunas
→ DadosColuna
→ Curvas
→ Gráficos
→ CurvaGrafico
```

O `commit` ocorrerá somente depois do sucesso integral. Qualquer falha provocará
tentativa de `rollback`, evitando que o operador receba um experimento salvo
parcialmente.

## 8. Implementação incremental

1. ampliar e tornar redimensionável a tela principal;
2. adicionar os atalhos de digitação e colagem;
3. implementar a tela de identificação e o estado do experimento;
4. apresentar a lista de experimentos do usuário;
5. implementar a grade editável e virtualizada;
6. implementar digitação e navegação pelo teclado;
7. implementar colagem de planilha;
8. implementar importação CSV;
9. integrar curvas e gráficos ao salvamento completo.
