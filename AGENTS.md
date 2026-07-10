# AGENTS.md — Instruções para agentes de desenvolvimento do GACS

## 1. Identidade oficial do projeto

### 1.1 Projeto de TCC

**GACS-TCC – Geração de Aplicativo de Caracterização de Componentes Semicondutores**

### 1.2 Nome oficial do software

**GACS – Gerenciador para Análise e Caracterização de Componentes Semicondutores**

### 1.3 Autor

Ronaldo Rodrigues Godoi

### 1.4 Contexto acadêmico

O GACS é um projeto acadêmico de TCC vinculado ao Bacharelado em Física da Universidade Estadual de Londrina (UEL).

O software deve refletir simultaneamente:

- rigor científico;
- clareza de modelagem;
- simplicidade de uso;
- organização de engenharia de software;
- possibilidade de evolução futura para uso laboratorial.

---

## 2. Objetivo principal do protótipo

O protótipo inicial deve oferecer um fluxo funcional e coerente para:

1. criar e registrar um experimento;
2. inserir dados experimentais;
3. organizar os dados em colunas;
4. associar colunas X e Y;
5. gerar gráficos;
6. analisar ou caracterizar curvas;
7. salvar e recuperar os dados.

O núcleo do protótipo é:

```text
Entrada de dados
→ geração de gráfico
→ análise/caracterização
```

Essas três partes devem utilizar a mesma estrutura de dados e permanecer consistentes entre si.

---

## 3. Princípios fundamentais do sistema

### 3.1 Simplicidade para o operador

O operador não deve ser obrigado a preencher cadastros detalhados antes de utilizar o núcleo do sistema.

O fluxo mínimo deve permitir:

```text
Criar experimento
→ digitar, colar ou importar dados
→ selecionar ou definir colunas X e Y
→ gerar gráfico
→ analisar ou caracterizar
→ salvar
```

Cadastros adicionais poderão ser vinculados futuramente, mas não devem bloquear o fluxo rápido.

### 3.2 Detalhamento progressivo

O sistema deve permitir que um experimento seja criado inicialmente com poucos dados e complementado posteriormente.

O protótipo não deve exigir, desde o início:

- cadastro completo do componente;
- fabricante;
- lote;
- encapsulamento;
- equipamento;
- laboratório;
- operador;
- condições experimentais completas.

Esses dados poderão ser adicionados em módulos futuros.

### 3.3 Modularidade

O GACS deve ser estruturado em módulos ou camadas com responsabilidades claras.

Os módulos devem poder evoluir com baixo acoplamento, embora compartilhem o mesmo banco de dados.

### 3.4 Coerência tecnológica

A pilha tecnológica deve permanecer simples e coesa.

Evitar a inclusão de frameworks, bibliotecas ou ferramentas adicionais sem necessidade comprovada.

### 3.5 Evolução controlada

Não alterar decisões arquitetônicas consolidadas sem autorização explícita.

Quando uma solução futura exigir mudança estrutural, documentar:

- o problema;
- a limitação do modelo atual;
- as alternativas;
- o impacto;
- a proposta de migração.

---

## 4. Tecnologias definidas

### 4.1 Linguagem

- Java 21

### 4.2 Interface gráfica

- JavaFX

### 4.3 Gerenciamento de dependências e compilação

- Maven

### 4.4 Banco de dados

- MySQL 8

### 4.5 Persistência inicial

- JDBC

Não introduzir JPA, Hibernate, Spring, Spring Boot ou outro ORM nesta fase, salvo decisão posterior expressa.

---

## 5. Organização arquitetônica

A arquitetura inicial deve utilizar separação em camadas.

Estrutura de pacotes prevista:

```text
br.uel.gacs
├── application
├── controller
├── dao
├── model
├── service
└── util
```

### 5.1 `application`

Responsável por:

- inicialização da aplicação;
- configuração principal;
- ponto de entrada JavaFX;
- composição inicial das dependências.

### 5.2 `controller`

Responsável por:

- interação entre telas JavaFX e serviços;
- tratamento de eventos de interface;
- validações de interface;
- atualização de tabelas, campos e gráficos.

Controllers não devem conter SQL.

### 5.3 `dao`

Responsável por:

- acesso ao banco de dados;
- operações CRUD;
- consultas SQL;
- conversão entre registros do banco e objetos de modelo.

DAOs não devem conter regras de interface.

### 5.4 `model`

Responsável por:

- representar entidades e dados do domínio;
- armazenar estado;
- oferecer validações simples e coerentes com a entidade;
- refletir o modelo lógico definido.

Classes de `model` não devem executar SQL nem manipular componentes JavaFX.

### 5.5 `service`

Responsável por:

- regras de negócio;
- coordenação entre DAOs;
- validações que dependem de mais de uma entidade;
- operações transacionais;
- preparação dos dados para gráfico e análise;
- importação, normalização e tratamento de dados.

### 5.6 `util`

Responsável por utilitários genéricos e reutilizáveis, como:

- formatação numérica;
- leitura de CSV;
- validação de notação científica;
- conversão de separadores decimais;
- tratamento de datas;
- mensagens técnicas.

Evitar transformar `util` em depósito de responsabilidades indefinidas.

---

## 6. Modelo de dados inicial aceito

Banco de dados:

```text
DadosGACS
```

O núcleo inicial é composto por três entidades:

```text
Experimento
Coluna
DadoColuna
```

### 6.1 Experimento

Representa a unidade central de registro do trabalho experimental.

Campos inicialmente definidos:

```text
idExperimento
nomeExperimento
dataExperimento
observacoes
```

Observações:

- `idExperimento` identifica unicamente o experimento;
- `nomeExperimento` permite identificação amigável;
- `dataExperimento` registra data e hora;
- `observacoes` armazena texto livre;
- um experimento pode existir sem cadastro detalhado de componente;
- um experimento possui uma ou mais colunas.

### 6.2 Coluna

Representa uma coluna de grandeza ou dados experimentais.

Campos inicialmente definidos:

```text
idColuna
idExperimento
nomeColuna
tipoEixo
idColunaX
```

Significado:

- `idColuna` identifica unicamente a coluna;
- `idExperimento` indica o experimento ao qual a coluna pertence;
- `nomeColuna` é o cabeçalho apresentado ao operador;
- `tipoEixo` indica o papel inicial da coluna, X ou Y;
- `idColunaX` referencia a coluna X associada quando a coluna atual for Y.

### 6.3 DadoColuna

Representa um único valor medido pertencente a uma coluna.

Campos inicialmente definidos:

```text
idDado
idColuna
numeroDaMedida
valorMedida
```

Significado:

- `idDado` identifica unicamente o valor;
- `idColuna` informa a coluna proprietária;
- `numeroDaMedida` identifica a posição ou linha lógica da medida;
- `valorMedida` armazena o valor numérico.

---

## 7. Regras de associação entre colunas X e Y

### 7.1 Regra para coluna X

Quando:

```text
tipoEixo = X
```

então:

```text
idColunaX = NULL
```

Uma coluna X não referencia outra coluna X no modelo inicial.

### 7.2 Regra para coluna Y

Quando:

```text
tipoEixo = Y
```

então:

```text
idColunaX != NULL
```

A coluna Y deve apontar para a coluna X utilizada como base.

### 7.3 Restrição de experimento

A coluna Y e a coluna X referenciada devem pertencer ao mesmo experimento.

### 7.4 Um X compartilhado por vários Y

O modelo deve permitir:

```text
X1 → Y1
X1 → Y2
X1 → Y3
```

Exemplo:

```text
VDS → IDS para VGS = 1 V
VDS → IDS para VGS = 2 V
VDS → IDS para VGS = 3 V
```

### 7.5 Um X diferente para cada Y

O modelo deve permitir:

```text
X1 → Y1
X2 → Y2
X3 → Y3
```

### 7.6 Limitação conhecida

No modelo inicial, `tipoEixo` e `idColunaX` representam o pareamento principal de cada coluna Y.

Uma entidade formal `Curva` não será criada nesta etapa.

Caso futuramente seja necessário:

- reutilizar a mesma coluna em vários papéis;
- associar a mesma Y a diferentes X;
- salvar múltiplas composições gráficas independentes;
- compartilhar curvas em vários gráficos;

a modelagem poderá ser revisada e o relacionamento poderá ser extraído para uma entidade específica.

Não antecipar essa tabela sem autorização.

---

## 8. Regras para os dados medidos

### 8.1 Pareamento das medidas

Os valores X e Y são associados por:

```text
numeroDaMedida
```

Exemplo:

```text
X, medida 1 ↔ Y, medida 1
X, medida 2 ↔ Y, medida 2
X, medida 3 ↔ Y, medida 3
```

### 8.2 Unicidade

Deve existir no máximo um valor para cada combinação:

```text
idColuna + numeroDaMedida
```

O banco deverá futuramente aplicar restrição única para essa combinação.

### 8.3 Ordem

Os dados devem ser recuperados e apresentados preferencialmente em ordem crescente de `numeroDaMedida`.

### 8.4 Dados faltantes

O tratamento definitivo de dados ausentes será definido posteriormente.

Não inventar preenchimento automático com zero.

Não interpolar valores automaticamente sem solicitação explícita.

---

## 9. Entrada de dados

O GACS deverá aceitar:

### 9.1 Digitação manual

O usuário poderá inserir os valores diretamente em uma tabela editável.

### 9.2 Colagem direta

O usuário poderá copiar dados de uma planilha, como Excel ou software semelhante, e colá-los diretamente na tabela do GACS.

### 9.3 Importação CSV

O sistema deverá importar arquivos CSV usando:

```text
;
```

como separador padrão de colunas.

Exemplo:

```csv
VDS;IDS
0,0;1,20E-9
0,5;3,70E-7
1,0;2,15E-5
```

### 9.4 Separador decimal

O sistema deverá ser preparado para aceitar:

- vírgula decimal;
- ponto decimal.

A conversão deve ocorrer antes da persistência numérica.

### 9.5 Cabeçalhos

Os cabeçalhos importados devem ser utilizados inicialmente como `nomeColuna`.

### 9.6 Validação

Valores inválidos devem gerar mensagem clara.

Não substituir silenciosamente valores inválidos por zero.

---

## 10. Notação científica

A notação científica é uma característica obrigatória do GACS.

### 10.1 Entrada

O sistema deve aceitar valores como:

```text
1.23E-6
1,23E-6
-4.50E+3
```

### 10.2 Armazenamento

Os valores devem ser armazenados como tipos numéricos.

Não armazenar `valorMedida` apenas como texto formatado.

### 10.3 Apresentação

A interface deve ser capaz de exibir valores de forma consistente em notação científica.

Exemplo:

```text
1.2300E-06
```

### 10.4 Precisão

Não aplicar arredondamento destrutivo durante a importação ou persistência.

A quantidade de casas exibidas poderá ser configurada posteriormente.

---

## 11. Gráficos

A implementação de gráficos ainda não pertence à primeira tarefa de código, mas o modelo deve permanecer compatível com ela.

### 11.1 Fonte dos dados

Um gráfico será construído a partir de:

- uma coluna Y;
- sua coluna X associada;
- os valores pareados por `numeroDaMedida`.

### 11.2 Múltiplas curvas

Um gráfico deverá poder apresentar várias colunas Y.

Essas colunas Y poderão:

- compartilhar a mesma coluna X;
- utilizar colunas X diferentes.

### 11.3 JavaFX

A visualização gráfica deverá ser implementada prioritariamente com JavaFX, preservando a diretriz de coesão tecnológica.

### 11.4 Não criar estruturas prematuras

Não criar nesta etapa:

- tabela `Grafico`;
- tabela `Curva`;
- tabela `GraficoCurva`;
- classes de visualização;
- classes de análise.

Essas estruturas serão projetadas quando seus requisitos estiverem suficientemente claros.

---

## 12. Análise e caracterização

A análise deverá receber os mesmos pares numéricos usados pelo gráfico:

```text
(x1, y1)
(x2, y2)
...
(xn, yn)
```

A origem dos dados não deve alterar o algoritmo de análise.

Os dados podem vir de:

- digitação manual;
- colagem;
- CSV;
- banco de dados;
- futura aquisição automática.

A caracterização inicial terá foco em componentes semicondutores, especialmente:

- diodos;
- FETs.

O professor orientador indicou interesse inicial em FETs.

Não implementar algoritmos físicos de caracterização nesta primeira tarefa.

---

## 13. Convenções de código

### 13.1 Idioma

Usar português nos nomes do domínio:

```java
Experimento
Coluna
DadoColuna
TipoEixo
```

Evitar mistura desnecessária de português e inglês.

Nomes técnicos consagrados podem permanecer em inglês quando apropriado, como:

- DAO;
- CSV;
- JavaFX;
- JDBC.

### 13.2 Classes

Usar PascalCase.

Exemplo:

```java
DadoColuna
```

### 13.3 Atributos e métodos

Usar camelCase.

Exemplos:

```java
idExperimento
numeroDaMedida
buscarPorExperimento()
```

### 13.4 Constantes e enums

Usar letras maiúsculas.

Exemplo:

```java
X
Y
```

### 13.5 Identificadores

Usar tipos anuláveis para IDs ainda não persistidos.

Preferência inicial:

```java
Long
```

em vez de:

```java
long
```

Um objeto novo poderá possuir ID nulo até ser salvo.

### 13.6 Datas

Usar API moderna de datas do Java.

Preferência inicial:

```java
LocalDateTime
```

Não usar `java.util.Date`.

### 13.7 Valores medidos

Preferência inicial:

```java
Double
```

A escolha poderá ser reavaliada caso os requisitos de precisão científica indiquem necessidade de `BigDecimal`.

Não mudar para `BigDecimal` sem discussão explícita sobre precisão, gráficos e desempenho.

### 13.8 Comentários e documentação

Adicionar Javadoc curto e útil em:

- classes públicas;
- enums;
- métodos públicos com regra não óbvia.

Evitar comentários que apenas repitam o código.

### 13.9 Construtores

As classes de modelo poderão possuir:

- construtor sem argumentos;
- construtor com todos os campos relevantes.

### 13.10 Getters e setters

Criar getters e setters convencionais nesta fase.

Não introduzir Lombok.

### 13.11 Imutabilidade

Não impor imutabilidade completa nesta etapa, pois o projeto utilizará formulários, JDBC e edição de dados.

### 13.12 Records

Não utilizar `record` para as entidades persistentes iniciais.

---

## 14. Validação das entidades

### 14.1 Responsabilidade da classe `Coluna`

A classe poderá validar regras simples, como:

```text
X não pode ter idColunaX
Y deve ter idColunaX
```

### 14.2 Responsabilidade do serviço

A confirmação de que a coluna X existe e pertence ao mesmo experimento deve ocorrer na camada `service` e também ser protegida pelo banco.

### 14.3 Defesa em camadas

As regras importantes deverão ser reforçadas em:

- interface;
- service;
- banco de dados.

Não depender de uma única camada para consistência.

---

## 15. Estrutura inicial das classes `model`

A primeira implementação deve criar somente:

```text
src/main/java/br/uel/gacs/model/TipoEixo.java
src/main/java/br/uel/gacs/model/Experimento.java
src/main/java/br/uel/gacs/model/Coluna.java
src/main/java/br/uel/gacs/model/DadoColuna.java
```

### 15.1 `TipoEixo`

Valores:

```java
X,
Y
```

### 15.2 `Experimento`

Atributos:

```java
Long idExperimento;
String nomeExperimento;
LocalDateTime dataExperimento;
String observacoes;
```

### 15.3 `Coluna`

Atributos:

```java
Long idColuna;
Long idExperimento;
String nomeColuna;
TipoEixo tipoEixo;
Long idColunaX;
```

### 15.4 `DadoColuna`

Atributos:

```java
Long idDado;
Long idColuna;
Integer numeroDaMedida;
Double valorMedida;
```

---

## 16. Decisão inicial sobre relacionamentos nos modelos

Na primeira implementação, representar chaves estrangeiras por IDs:

```java
Long idExperimento;
Long idColuna;
Long idColunaX;
```

Não substituir nesta etapa por:

```java
Experimento experimento;
Coluna colunaX;
List<Coluna> colunas;
List<DadoColuna> dados;
```

A decisão reduz complexidade inicial e mantém proximidade com JDBC e o modelo relacional.

Relacionamentos por objetos poderão ser introduzidos posteriormente, se houver benefício claro.

---

## 17. Restrições para a primeira tarefa do Codex

Ao receber a tarefa de implementar os modelos, o agente deve:

1. ler este arquivo integralmente;
2. verificar a estrutura atual do projeto;
3. criar os pacotes ausentes;
4. implementar somente as classes do pacote `model`;
5. não criar banco, DAO, service, controller ou telas;
6. não adicionar dependências;
7. não modificar o `pom.xml` sem necessidade de compilação;
8. não introduzir Lombok;
9. não introduzir frameworks;
10. não criar entidade `Curva`;
11. não criar entidade `Grafico`;
12. não criar entidade `Componente`;
13. não alterar nomes oficiais do projeto;
14. não traduzir classes do domínio para inglês;
15. executar a compilação Maven;
16. corrigir apenas erros relacionados à tarefa;
17. apresentar resumo dos arquivos criados.

---

## 18. Comando recomendado para a primeira implementação

O pedido inicial ao Codex deve ser:

```text
Leia integralmente o arquivo AGENTS.md.

Crie, se ainda não existirem, a estrutura de pacotes definida para o projeto GACS.

Implemente somente as classes do pacote br.uel.gacs.model:

- TipoEixo
- Experimento
- Coluna
- DadoColuna

Respeite rigorosamente os atributos, tipos, nomes, regras e restrições descritos no AGENTS.md.

Não implemente DAOs, serviços, controllers, banco de dados, gráficos, análise ou interface nesta etapa.

Não adicione frameworks ou dependências.

Ao final, execute mvn clean compile, corrija apenas eventuais erros de compilação relacionados à tarefa e apresente um resumo das alterações.
```

---

## 19. Diretrizes para alterações futuras

Antes de implementar uma alteração estrutural, o agente deve:

1. identificar a decisão existente;
2. explicar a necessidade da mudança;
3. indicar os arquivos afetados;
4. preservar compatibilidade sempre que possível;
5. solicitar decisão humana quando houver alternativas arquitetônicas relevantes.

O agente não deve tomar decisões irreversíveis de arquitetura sozinho.

---

## 20. Documentação futura prevista

A documentação deverá evoluir para a seguinte estrutura:

```text
docs/
├── 01-visao-geral.md
├── 02-requisitos-do-prototipo.md
├── 03-arquitetura.md
├── 04-modelo-de-dados.md
├── 05-regras-de-negocio.md
├── 06-convencoes-de-codigo.md
└── 07-roadmap.md
```

Este arquivo `AGENTS.md` deve permanecer como guia operacional resumido e autoritativo para agentes.

Documentos mais extensos poderão detalhar decisões específicas.

---

## 21. Estado atual do projeto

Decisões consolidadas:

- Java 21;
- JavaFX;
- Maven;
- MySQL 8;
- JDBC inicialmente;
- nomes do domínio em português;
- experimento como unidade central;
- fluxo rápido de entrada, gráfico e análise;
- digitação manual;
- colagem direta de planilhas;
- CSV com ponto e vírgula;
- aceitação de vírgula e ponto decimal;
- notação científica consistente;
- núcleo inicial com `Experimento`, `Coluna` e `DadoColuna`;
- associação X–Y por `tipoEixo` e `idColunaX`;
- adiamento da entidade `Curva`;
- modularidade com baixa dependência entre partes;
- detalhamento progressivo, sem obrigar cadastros completos.

---

## 22. Autoridade das decisões

As decisões arquitetônicas são conduzidas em conjunto por:

- Ronaldo Rodrigues Godoi, autor do projeto;
- ChatGPT, no papel de apoio à arquitetura, modelagem e revisão;
- Codex, no papel de agente de implementação dentro do ambiente de desenvolvimento.

O Codex deve implementar as decisões documentadas, não substituí-las por preferências próprias.

Quando houver dúvida, deve preservar a solução mais simples e solicitar esclarecimento antes de ampliar o escopo.
