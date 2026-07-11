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

O software deve refletir:

- rigor científico;
- clareza de modelagem;
- simplicidade de uso;
- organização de engenharia de software;
- possibilidade de evolução futura para uso laboratorial.

---

## 2. Objetivo principal do protótipo

O protótipo inicial deve oferecer um fluxo funcional para:

1. criar e registrar um experimento;
2. inserir dados experimentais;
3. organizar os dados em colunas;
4. formar curvas pela associação entre colunas X e Y;
5. reunir curvas em gráficos;
6. analisar ou caracterizar as curvas;
7. salvar e recuperar os dados.

Fluxo central:

```text
Experimento
→ Colunas
→ Curvas
→ Gráficos
→ Análise/Caracterização
```

A entrada de dados, a geração de gráficos e a caracterização devem utilizar a mesma estrutura persistida.

---

## 3. Princípios fundamentais

### 3.1 Simplicidade para o operador

O fluxo mínimo deve permitir:

```text
Criar experimento
→ digitar, colar ou importar dados
→ nomear colunas
→ criar curvas
→ compor gráficos
→ analisar ou caracterizar
→ salvar
```

Cadastros futuros não devem bloquear o uso do núcleo experimental.

### 3.2 Detalhamento progressivo

Um experimento pode ser criado com poucos dados e complementado posteriormente.

Não exigir nesta versão:

- cadastro completo do componente;
- fabricante;
- lote;
- encapsulamento;
- equipamento;
- laboratório;
- condições experimentais completas.

### 3.3 Modularidade

O sistema deve utilizar camadas com responsabilidades claras e baixo acoplamento.

### 3.4 Coerência tecnológica

Evitar frameworks, bibliotecas e ferramentas adicionais sem necessidade comprovada.

### 3.5 Evolução controlada

Não alterar decisões arquitetônicas consolidadas sem autorização explícita.

Quando uma mudança estrutural for necessária, registrar:

- problema;
- limitação atual;
- alternativas;
- impacto;
- proposta de migração.

### 3.6 Escopo fechado do modelo atual

O banco de dados desta versão terá exatamente sete tabelas:

```text
Usuario
Experimento
Coluna
DadoColuna
Curva
Grafico
CurvaGrafico
```

Não criar novas tabelas sem decisão arquitetônica explícita.

---

## 4. Tecnologias definidas

- **Linguagem:** Java 21
- **Interface gráfica:** JavaFX
- **Gerenciamento e compilação:** Maven
- **Banco de dados:** MySQL 8
- **Persistência inicial:** JDBC

Não introduzir JPA, Hibernate, Spring, Spring Boot, Lombok ou outro framework sem autorização expressa.

---

## 5. Organização arquitetônica

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
- manutenção da sessão do usuário autenticado por meio de `SessaoUsuario`.

`SessaoUsuario` não é entidade persistente, não pertence ao pacote `model` e não cria tabela no banco de dados.

### 5.2 `controller`

Responsável por:

- integração entre telas JavaFX e serviços;
- eventos de interface;
- validações de interface;
- atualização de tabelas, campos e gráficos.

Controllers não devem conter SQL.

### 5.3 `dao`

Responsável por:

- acesso ao banco;
- CRUD;
- consultas SQL;
- conversão entre registros e objetos de modelo.

DAOs não devem conter regras de interface.

### 5.4 `model`

Responsável por:

- representar as entidades do domínio;
- armazenar estado;
- refletir o modelo lógico definido;
- oferecer apenas validações simples da própria entidade.

Classes de `model` não devem executar SQL nem manipular componentes JavaFX.

### 5.5 `service`

Responsável por:

- regras de negócio;
- coordenação entre DAOs;
- validações entre entidades;
- operações transacionais;
- preparação de curvas e gráficos;
- importação, normalização e tratamento de dados.

### 5.6 `util`

Responsável por utilitários genéricos, como:

- formatação numérica;
- leitura de CSV;
- notação científica;
- conversão de separadores decimais;
- conversão de rótulos de colunas;
- tratamento de datas.

Evitar transformar `util` em depósito de responsabilidades indefinidas.

---

## 6. Padrão de nomenclatura do banco e do domínio

### 6.1 Tabelas

Usar nomes no singular e em português:

```text
Usuario
Experimento
Coluna
DadoColuna
Curva
Grafico
CurvaGrafico
```

### 6.2 Chaves primárias

Toda tabela com chave primária própria utiliza:

```text
id
```

Exemplos:

```text
Usuario.id
Experimento.id
Coluna.id
Curva.id
Grafico.id
```

### 6.3 Chaves estrangeiras

Usar:

```text
id + nome da entidade referenciada
```

Exemplos:

```text
idExperimento
idColuna
idColunaX
idColunaY
idGrafico
idCurva
```

### 6.4 Classes e atributos

- classes: PascalCase;
- atributos e métodos: camelCase;
- nomes do domínio em português;
- nomes técnicos consagrados podem permanecer em inglês, como DAO, CSV, JavaFX e JDBC.

---

## 7. Modelo de dados oficial

Banco de dados:

```text
DadosGACS
```

### 7.1 `Usuario`

Atributos:

```java
Long id;
String nome;
String email;
String senhaHash;
PerfilUsuario perfil;
Boolean ativo;
LocalDateTime dataCriacao;
LocalDateTime dataUltimaAlteracao;
```

Regras principais:

- a senha nunca deve ser armazenada em texto puro;
- `perfil` é representado por enum;
- `PerfilUsuario` não é tabela nesta versão.

### 7.2 `Experimento`

Atributos:

```java
Long id;
String nomeExperimento;
LocalDateTime dataExperimento;
String observacoes;
Long idUsuario;
```

`observacoes` deverá ser persistido em campo de texto longo.

Regras principais:

- cada experimento pertence obrigatoriamente a um único usuário;
- `idUsuario` deve referenciar um usuário existente.

### 7.3 `Coluna`

Atributos:

```java
Long id;
Long idExperimento;
Short rotulo;
String nomeColuna;
```

Regras principais:

- cada coluna pertence a um experimento;
- cada experimento aceita no máximo 50 colunas;
- `rotulo` representa a posição visual A, B, C, ..., AX;
- o valor numérico de `rotulo` será convertido pela aplicação;
- o rótulo somente existe quando a coluna possui dados;
- depois de atribuído, o rótulo permanece associado à coluna;
- o rótulo deve ser único dentro do experimento.

Conversão prevista:

```text
1  -> A
2  -> B
26 -> Z
27 -> AA
50 -> AX
```

`rotulo` não é enum e não é tabela. É um valor numérico limitado e convertido pela aplicação.

### 7.4 `DadoColuna`

Atributos:

```java
Long idColuna;
Integer numeroDaMedida;
Double valorMedida;
```

`DadoColuna` não possui `id` próprio.

Sua identificação é composta por:

```text
idColuna + numeroDaMedida
```

Regras:

- a combinação deve ser única;
- `numeroDaMedida` varia de 1 a 10.000;
- cada coluna aceita no máximo 10.000 medidas;
- os dados devem ser recuperados preferencialmente em ordem crescente de `numeroDaMedida`.

### 7.5 `Curva`

Atributos:

```java
Long id;
String nome;
Long idColunaX;
Long idColunaY;
```

Regras:

- `idColunaX` e `idColunaY` devem referenciar colunas existentes;
- uma curva não pode usar a mesma coluna como X e Y;
- as duas colunas devem pertencer ao mesmo experimento;
- uma coluna pode participar de várias curvas;
- uma mesma coluna X pode ser compartilhada por várias curvas;
- uma coluna pode exercer papéis diferentes em curvas diferentes.

Os pontos são pareados por `numeroDaMedida`:

```text
X, medida 1 ↔ Y, medida 1
X, medida 2 ↔ Y, medida 2
...
```

### 7.6 `Grafico`

Atributos:

```java
Long id;
Long idExperimento;
String nome;
```

Cada gráfico pertence obrigatoriamente a um único experimento.

Um gráfico pode conter várias curvas.

Uma curva pode participar de vários gráficos.

Um gráfico somente pode conter curvas formadas por colunas do mesmo experimento ao qual ele pertence.

### 7.7 `CurvaGrafico`

Atributos:

```java
Long idGrafico;
Integer numeroCurva;
Long idCurva;
```

`CurvaGrafico` não possui `id` artificial próprio.

Regras:

- `idGrafico + numeroCurva` deve ser único;
- `idGrafico + idCurva` deve ser único;
- a mesma curva não pode ser adicionada duas vezes ao mesmo gráfico;
- `numeroCurva` começa em 1;
- `numeroCurva` define a ordem das curvas no gráfico e na legenda.

---

## 8. Relacionamentos

```text
Experimento 1:N Coluna

Experimento 1:N Grafico

Usuario 1:N Experimento

Coluna 1:N DadoColuna

Coluna 1:N Curva, no papel de eixo X

Coluna 1:N Curva, no papel de eixo Y

Grafico N:N Curva
```

A relação muitos-para-muitos entre `Grafico` e `Curva` é representada por `CurvaGrafico`.

Nesta versão, a relação entre `Usuario` e `Experimento` é representada por `Experimento.idUsuario`. Cada experimento pertence obrigatoriamente a um único usuário.

---

## 9. Enums

Enums representam conjuntos pequenos, estáveis e fechados de valores.

Enum confirmado:

```text
PerfilUsuario
```

Enums não devem ser transformados em tabelas nesta versão.

Um enum somente deverá se tornar tabela se seus valores precisarem ser:

- cadastrados pelo usuário;
- alterados sem recompilar;
- associados a atributos adicionais;
- administrados dinamicamente.

Não criar ou manter `TipoEixo`.

Os papéis X e Y pertencem à entidade `Curva`, por meio de `idColunaX` e `idColunaY`.

---

## 10. Entrada de dados

O GACS deverá aceitar:

### 10.1 Digitação manual

Valores inseridos diretamente em uma tabela editável.

### 10.2 Colagem de planilha

Dados copiados do Excel ou software semelhante.

A interface deve utilizar:

```text
A, B, C, ..., Z, AA, ..., AX
```

como rótulos visuais de coluna.

As linhas devem ser numeradas.

### 10.3 Importação CSV

Separador padrão:

```text
;
```

### 10.4 Separador decimal

Aceitar:

- vírgula decimal;
- ponto decimal.

A normalização deve ocorrer antes da persistência.

### 10.5 Cabeçalhos

Cabeçalhos importados devem alimentar inicialmente `nomeColuna`.

### 10.6 Validação

Valores inválidos devem gerar mensagem clara.

Não substituir silenciosamente valores inválidos por zero.

---

## 11. Notação científica

O sistema deve aceitar:

```text
1.23E-6
1,23E-6
-4.50E+3
```

Os valores devem ser armazenados numericamente como `Double`.

Não armazenar `valorMedida` apenas como texto formatado.

Não aplicar arredondamento destrutivo na importação ou persistência.

A mudança para `BigDecimal` exige discussão explícita sobre precisão, gráficos e desempenho.

---

## 12. Gráficos e curvas

### 12.1 Formação da curva

Uma curva é formada por:

- uma coluna X;
- uma coluna Y;
- pareamento pelo mesmo `numeroDaMedida`.

### 12.2 Composição do gráfico

Um gráfico reúne curvas por meio de `CurvaGrafico`.

### 12.3 Ordem

`numeroCurva` determina:

- ordem de apresentação;
- ordem na legenda;
- sequência de processamento;
- reorganização futura das séries.

### 12.4 Seleção de curvas

O usuário poderá selecionar curvas com faixas de dados que produzam melhor visualização conjunta.

### 12.5 JavaFX

A visualização gráfica deverá ser implementada prioritariamente com JavaFX.

Não duplicar os dados apenas para gerar o gráfico. Utilizar as entidades persistidas.

---

## 13. Análise e caracterização

A análise deverá receber os mesmos pares numéricos utilizados pelo gráfico:

```text
(x1, y1)
(x2, y2)
...
(xn, yn)
```

A origem dos dados não deve alterar o algoritmo:

- digitação;
- colagem;
- CSV;
- banco;
- futura aquisição automática.

O foco inicial de caracterização será em:

- diodos;
- FETs.

Não implementar algoritmos físicos sem especificação funcional e científica documentada.

---

## 14. Diretrizes Java 21

### 14.1 IDs

Usar `Long` para identificadores, permitindo `null` antes da persistência.

### 14.2 Datas

Usar:

```java
LocalDateTime
```

Não usar `java.util.Date`.

### 14.3 Valores medidos

Usar inicialmente:

```java
Double
```

### 14.4 Entidades

Entidades persistentes devem ser classes convencionais mutáveis.

Não utilizar `record` para:

- `Usuario`;
- `Experimento`;
- `Coluna`;
- `DadoColuna`;
- `Curva`;
- `Grafico`;
- `CurvaGrafico`.

### 14.5 Construtores

As classes poderão possuir:

- construtor sem argumentos;
- construtor com todos os campos.

### 14.6 Getters e setters

Criar getters e setters convencionais.

### 14.7 Comentários

Adicionar Javadoc curto e útil em classes públicas, enums e regras não óbvias.

Evitar comentários que apenas repitam o código.

### 14.8 Recursos modernos

Usar apenas recursos estáveis do Java 21.

Não usar recursos preview nem `--enable-preview`.

---

## 15. Representação dos relacionamentos no pacote `model`

Na etapa atual, representar chaves estrangeiras por IDs:

```java
Long idExperimento;
Long idColuna;
Long idColunaX;
Long idColunaY;
Long idGrafico;
Long idCurva;
```

Não substituir sem decisão explícita por referências complexas como:

```java
Experimento experimento;
Coluna colunaX;
List<Curva> curvas;
```

A representação por IDs mantém proximidade com JDBC e com o modelo relacional.

---

## 16. Validação e integridade

Aplicar defesa em camadas:

- interface;
- service;
- banco de dados.

### 16.1 Validações simples da entidade

Exemplos:

- limites básicos de `rotulo`;
- limites básicos de `numeroDaMedida`;
- `idColunaX` diferente de `idColunaY`;
- `numeroCurva` maior ou igual a 1.

### 16.2 Validações do serviço

Exemplos:

- colunas X e Y pertencem ao mesmo experimento;
- rótulo é único no experimento;
- curva não se repete no gráfico;
- sequência de `numeroCurva` é válida;
- máximo de 50 colunas;
- máximo de 10.000 medidas.

### 16.3 Restrições do banco

As regras de unicidade e integridade referencial devem ser protegidas pelo banco quando o esquema SQL for implementado.

---

## 17. Classes oficiais do pacote `model`

O pacote deverá conter:

```text
PerfilUsuario
Usuario
Experimento
Coluna
DadoColuna
Curva
Grafico
CurvaGrafico
```

Não criar:

```text
TipoEixo
```

Não adicionar novas entidades ao pacote `model` sem autorização.

---

## 18. Restrições para tarefas de implementação

Ao implementar ou corrigir o modelo, o agente deve:

1. ler este arquivo integralmente;
2. consultar `docs/04-modelo-de-dados.md`;
3. verificar a estrutura atual do projeto;
4. preservar os nomes exatos das classes e atributos;
5. não criar tabelas ou entidades extras;
6. não introduzir frameworks;
7. não introduzir Lombok;
8. não modificar o `pom.xml` sem necessidade real;
9. não implementar DAO, service, controller ou interface quando a tarefa estiver restrita ao pacote `model`;
10. executar `mvn clean compile`;
11. corrigir apenas erros relacionados à tarefa;
12. apresentar resumo claro dos arquivos alterados.

---

## 19. Comando recomendado para correção do pacote `model`

```text
Leia integralmente o arquivo AGENTS.md e o arquivo docs/04-modelo-de-dados.md.

Revise o pacote br.uel.gacs.model e alinhe-o ao modelo oficial de sete tabelas.

As classes e o enum oficiais são:

- PerfilUsuario
- Usuario
- Experimento
- Coluna
- DadoColuna
- Curva
- Grafico
- CurvaGrafico

Respeite rigorosamente os nomes, tipos e atributos documentados.

Remova do modelo qualquer dependência de TipoEixo.

Não implemente DAOs, serviços, controllers, banco de dados, gráficos, análise ou interface nesta tarefa.

Não adicione frameworks ou dependências.

Ao final, execute mvn clean compile, corrija apenas os erros relacionados à tarefa e apresente um resumo das alterações.
```

---

## 20. Documentação oficial

A documentação principal está organizada em:

```text
docs/
├── 01-visao-geral.md
├── 02-requisitos-do-prototipo.md
├── 03-arquitetura.md
├── 04-modelo-de-dados.md
├── 05-regras-de-negocio.md
├── 06-convencoes-de-codigo.md
├── 07-diretrizes-java21.md
├── 08-sistema-inicial-de-permissoes.md
└── 09-diretrizes-de-implantacao-e-documentacao.md
```

Ordem de autoridade para implementação:

1. decisão humana mais recente registrada;
2. `docs/04-modelo-de-dados.md`, para estrutura das tabelas;
3. `AGENTS.md`, para execução e limites dos agentes;
4. demais documentos especializados.

Em caso de conflito, o agente deve interromper a ampliação do escopo e relatar a inconsistência.

---

## 21. Estado atual consolidado

Decisões vigentes:

- Java 21;
- JavaFX;
- Maven;
- MySQL 8;
- JDBC;
- domínio em português;
- banco `DadosGACS`;
- sete tabelas oficiais;
- chave primária `id` nas tabelas que possuem identificador próprio;
- chaves estrangeiras com nomes descritivos;
- `DadoColuna` com chave composta;
- `CurvaGrafico` sem `id` artificial;
- `CurvaGrafico.numeroCurva` como ordem interna do gráfico;
- até 50 colunas por experimento;
- até 10.000 medidas por coluna;
- rótulos visuais de A até AX;
- curvas como associação explícita entre coluna X e coluna Y;
- gráficos como agrupamentos ordenados de curvas;
- cada gráfico vinculado obrigatoriamente a um experimento;
- `SessaoUsuario` como componente não persistente do pacote `application`;
- `PerfilUsuario` como enum;
- ausência de `TipoEixo`;
- digitação manual;
- colagem de planilhas;
- importação CSV;
- vírgula e ponto decimal;
- notação científica;
- detalhamento progressivo;
- nenhuma tabela adicional nesta versão.

---

## 22. Autoridade das decisões

As decisões arquitetônicas são conduzidas por:

- Ronaldo Rodrigues Godoi, autor do projeto;
- ChatGPT, como apoio à arquitetura, modelagem e revisão;
- Codex, como agente de implementação.

O agente de implementação deve executar as decisões documentadas, não substituí-las por preferências próprias.

Quando houver dúvida, deve preservar o modelo oficial, evitar ampliar o escopo e relatar a questão antes de realizar mudança arquitetônica.
