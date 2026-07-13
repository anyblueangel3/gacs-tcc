# 13 — Colagem, importação CSV e persistência da planilha

## 1. Resultado deste incremento

O GACS passa a oferecer três formas integradas de entrada de dados:

- digitação direta na grade JavaFX;
- colagem de um bloco copiado do Excel;
- importação de arquivo CSV por meio do seletor de arquivos do JavaFX.

As três formas produzem a mesma estrutura `PlanilhaExperimento`. A entrada por
digitação e sua navegação por teclado foram preservadas.

Quando um experimento já está aberto, o atalho `Digitar Dados` não inicia outro
experimento e não substitui a planilha existente. Ele solicita o nome de uma
nova coluna, acrescenta essa coluna vazia à grade atual e posiciona o cursor em
sua primeira célula, preservando integralmente os dados colados ou importados.

Da mesma forma, novas colagens e importações realizadas com um experimento
aberto são acumuladas na planilha atual. Cada novo bloco é acrescentado à
direita como novas colunas. A grade conserva a maior quantidade de linhas entre
os blocos e representa por células vazias as posições para as quais não existe
valor. A operação é recusada integralmente antes de qualquer alteração quando
o total ultrapassaria 50 colunas ou 10.000 medidas.

## 2. Colagem do Excel

O atalho `Colar de Planilha` lê o texto tabulado da área de transferência. A
primeira linha é reconhecida como cabeçalho quando contém texto. As demais
linhas devem ser retangulares e conter apenas números finitos.

## 3. Importação CSV

O atalho `Importar CSV` e o item correspondente no menu `Arquivo` abrem o
`FileChooser` nativo do JavaFX. O leitor trabalha com UTF-8, remove o marcador
BOM quando existente e aceita:

- separador por vírgula;
- separador por ponto e vírgula, inclusive o padrão do Excel com vírgula decimal;
- campos entre aspas;
- aspas literais representadas por duas aspas consecutivas;
- terminações de linha do Windows e de outros sistemas.

Todo o arquivo é analisado e validado antes da abertura da grade.

## 4. Salvamento transacional

O botão `Salvar Experimento` persiste, em uma única conexão e transação:

```text
Experimento
→ Coluna
→ DadoColuna
```

O experimento recebe seu identificador, as colunas recebem rótulos posicionais
de 1 a 50 e cada valor preenchido é armazenado com seu número de medida. Células
vazias da digitação manual não geram registros em `DadoColuna`.

Em qualquer falha ocorre `rollback`. Para um experimento novo, o identificador
em memória também é restaurado para `null`, permitindo nova tentativa segura.

## 5. Reabertura

Ao abrir um experimento já salvo, o GACS consulta suas colunas e seus valores,
reconstrói a `PlanilhaExperimento` e volta a apresentá-la na mesma grade
editável. Um novo salvamento atualiza nomes e valores dentro de outra transação
completa.
