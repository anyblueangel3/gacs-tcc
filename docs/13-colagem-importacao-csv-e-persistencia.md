# 13 — Entrada de dados, persistência e estágio funcional da planilha

## 1. Identificação do registro

- **Projeto acadêmico:** GACS-TCC — Geração de Aplicativo de Caracterização de Componentes Semicondutores
- **Aplicativo:** GACS — Gerenciador para Análise e Caracterização de Componentes Semicondutores
- **Data deste registro:** 12 de julho de 2026
- **Tecnologias:** Java 21, JavaFX 21, Maven, JDBC e MySQL 8.0.28

Este documento registra o incremento dedicado à planilha experimental do GACS,
consolidando as decisões adotadas, as funcionalidades implementadas, os testes
realizados e os pontos deixados para refinamento posterior.

## 2. Resultado geral do incremento

O GACS passou a oferecer um fluxo funcional integrado para:

```text
Criar experimento
→ digitar, colar ou importar dados
→ editar nomes e valores
→ salvar no banco
→ fechar o experimento
→ reabrir colunas e medidas persistidas
```

As três formas de entrada utilizam a mesma classe `PlanilhaExperimento` e a
mesma grade editável em JavaFX. A origem dos dados não altera sua representação
em memória nem sua forma de persistência.

## 3. Modelo da planilha em memória

`PlanilhaExperimento` mantém separadamente:

- os nomes textuais das colunas;
- as linhas de medidas;
- os valores numéricos de cada célula.

Os nomes pertencem conceitualmente às entidades `Coluna`. As células de dados
aceitam exclusivamente valores `Double`; uma célula `null` representa uma
posição ainda vazia e não gera registro em `DadoColuna`.

Continuam válidos os limites de:

- 50 colunas por experimento;
- 10.000 medidas por coluna;
- 500.000 valores no caso máximo.

Os rótulos posicionais são produzidos automaticamente no padrão `A`, `B`, ...,
`Z`, `AA`, ..., `AX`. Eles não são obtidos dos nomes importados.

## 4. Digitação direta

A grade permanece editável e virtualizada. A digitação implementada neste
incremento permite iniciar a edição diretamente pela tecla digitada e mantém a
navegação principal:

- `Enter`: próxima linha da mesma coluna;
- `Shift + Enter`: linha anterior;
- `Tab`: próxima coluna;
- `Shift + Tab`: coluna anterior;
- `Delete`: limpa a célula selecionada.

Ao avançar além da última linha por `Enter`, uma nova medida vazia é criada. Os
valores são exibidos em notação científica sem modificar o `Double` armazenado.
Ponto ou vírgula decimal e notação científica podem ser informados na edição.

Quando já existe um experimento aberto, o atalho `Digitar Dados` acrescenta uma
nova coluna à planilha atual, em vez de criar outro experimento e descartar
visualmente os dados existentes.

## 5. Colagem proveniente do Excel

O atalho `Colar de Planilha` lê imediatamente o texto da área de transferência.
O formato tabulado produzido pelo Excel é convertido em uma matriz de células.

A primeira linha é interpretada como cabeçalho quando contém ao menos uma
célula não numérica. Nesse caso, seus textos tornam-se os nomes das colunas. As
linhas restantes devem:

- possuir a mesma quantidade de células;
- respeitar os limites da planilha;
- conter somente números finitos;
- não possuir células numéricas obrigatórias vazias.

A matriz somente é aceita depois da validação integral, evitando colagem
parcial de dados inválidos.

## 6. Importação de arquivo CSV

Foi acrescentado o atalho `Importar CSV` e o item `Importar arquivo CSV` no menu
`Arquivo`. Ambos utilizam o `FileChooser` do JavaFX para apresentar a janela de
seleção de arquivo do sistema operacional.

O utilitário `LeitorCsv` foi criado no pacote `br.uel.gacs.util`. Ele lê arquivos
UTF-8 e contempla:

- separador por vírgula;
- separador por ponto e vírgula, usual no Excel em configuração brasileira;
- vírgula decimal quando o delimitador é ponto e vírgula;
- campos delimitados por aspas;
- aspas literais representadas por duas aspas consecutivas;
- marcador BOM no início do arquivo;
- terminações de linha de diferentes sistemas operacionais.

Depois da leitura, o conteúdo passa pelas mesmas regras de cabeçalho, dimensão
e validação numérica utilizadas na colagem.

## 7. Alternância e combinação das entradas

Foi alcançada uma alternância funcional entre digitação, colagem e importação.
Os comandos utilizam a mesma tela de experimento e a mesma grade.

A implementação também foi preparada para acrescentar novos blocos à direita
da planilha aberta. Quando os blocos possuem quantidades diferentes de linhas,
a grade conserva a maior quantidade e completa as posições inexistentes com
células vazias. Antes da incorporação é verificado se o resultado respeita os
limites de 50 colunas e 10.000 medidas.

Os testes manuais confirmaram o funcionamento das formas individuais de entrada
e da alternância geral. Permaneceram pequenos comportamentos de interface na
repetição de colagens e importações que deverão ser reproduzidos e refinados em
um sprint posterior. Em particular, foi observado um caso no qual uma nova
importação CSV substituiu visualmente a planilha em vez de acumulá-la. Esse
ponto fica registrado como pendência e não invalida a importação individual,
que está funcional.

## 8. Persistência transacional

O botão `Salvar Experimento` passou a coordenar, por meio de `ExperimentoCtlr`,
os DAOs necessários para persistir:

```text
Experimento
→ Coluna
→ DadoColuna
```

O salvamento completo utiliza uma única conexão e uma única transação. O
`commit` somente ocorre depois do sucesso integral. Uma falha em qualquer etapa
provoca tentativa de `rollback`, evitando que o experimento seja apresentado ao
operador como salvo parcialmente.

No primeiro salvamento:

1. o registro de `Experimento` é inserido e recebe o identificador gerado;
2. cada nome e rótulo posicional é persistido em `Coluna`;
3. cada célula preenchida é persistida em `DadoColuna`, associada à coluna e ao
   número da medida.

Ao salvar alterações, as entidades existentes são atualizadas dentro de nova
transação completa. Se a inserção de um experimento novo for revertida, seu
identificador em memória volta a `null`, permitindo uma tentativa posterior
segura.

## 9. Reabertura do experimento

Ao abrir um experimento persistido, `ExperimentoCtlr` consulta as colunas por
meio de `ColunaDAO` e as medidas por meio de `DadoColunaDAO`.

Com esses resultados, o sistema reconstrói uma `PlanilhaExperimento`, restaura:

- a ordem das colunas;
- os nomes das colunas;
- os números das medidas;
- os valores numéricos;
- as posições sem registro como células vazias.

A planilha reconstruída volta a ser apresentada na mesma grade editável. O
teste manual de salvar, fechar e reabrir confirmou a persistência e a carga dos
dados nas tabelas corretas.

## 10. Principais classes envolvidas

As contribuições deste incremento ficaram concentradas em:

```text
application/PlanilhaExperimento.java
application/PainelExperimento.java
application/TelaPrincipal.java
application/MenuPrincipal.java
controller/ExperimentoCtlr.java
util/FormatadorNumero.java
util/LeitorCsv.java
dao/ExperimentoDAO.java
dao/ColunaDAO.java
dao/DadoColunaDAO.java
```

As responsabilidades permanecem separadas: JavaFX compõe a interface,
`PlanilhaExperimento` mantém o estado tabular, o controller valida e coordena a
transação e os DAOs executam o SQL de suas respectivas entidades.

## 11. Pendências para o próximo sprint

Ficam deliberadamente adiados:

1. reproduzir e corrigir os pequenos comportamentos observados ao repetir e
   alternar colagens e importações em um experimento já aberto;
2. revisar mensagens de erro para oferecer mais contexto sem expor detalhes
   técnicos ao operador;
3. ampliar os testes com arquivos CSV produzidos por diferentes versões e
   configurações regionais do Excel;
4. verificar a experiência com blocos de quantidades muito diferentes de
   linhas e com planilhas próximas aos limites máximos;
5. prosseguir posteriormente para a associação entre colunas, curvas e
   gráficos, sem alterar o núcleo de entrada já funcional.

## 12. Encerramento do dia

Ao final de 12 de julho de 2026, o GACS possui uma planilha experimental
editável com três formas de entrada, validação numérica, persistência
transacional e reconstrução dos dados salvos. O núcleo implementado já permite
registrar e recuperar medições experimentais de modo coerente com o modelo de
dados aprovado.

Os comportamentos menores ainda não ideais foram conscientemente registrados
para tratamento posterior. O estágio atual é funcional e constitui uma base
sólida para a futura formação de curvas e geração de gráficos.
