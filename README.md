# GACS-TCC

> **Geração de Aplicativo de Caracterização de Componentes Semicondutores**

## Sobre o projeto

O **GACS-TCC** é um projeto de Trabalho de Conclusão de Curso (TCC) do Bacharelado em Física da Universidade Estadual de Londrina (UEL).

O projeto tem como objetivo o desenvolvimento do **GACS – Gerenciador para Análise e Caracterização de Componentes Semicondutores**, um software destinado ao gerenciamento de experimentos, armazenamento de dados experimentais, geração de gráficos e caracterização elétrica de componentes semicondutores.

Desde sua concepção, o sistema é desenvolvido com foco em simplicidade, organização, confiabilidade, extensibilidade e futura integração com equipamentos de laboratório.

O desenvolvimento é realizado de forma colaborativa entre o autor e o ChatGPT (OpenAI), utilizado como assistente de arquitetura, modelagem, documentação, revisão técnica e proposição de melhorias. O projeto também utiliza o Codex como agente de implementação no ambiente de desenvolvimento, sempre sob supervisão e conforme as decisões documentadas.

---

## Equipe de trabalho do projeto GACS-TCC

Professor Dr. Edison Laureto  
Ronaldo Rodrigues Godoi  
ChatGPT (OpenAI)  
Codex - OpenAI's coding agent (OpenAI)  


---

## Primeira versão

A primeira versão do GACS será direcionada ao estudo e à caracterização elétrica de:

* Diodos;
* Transistores de Efeito de Campo (FETs).

A arquitetura será projetada para permitir a futura inclusão de novos dispositivos semicondutores sem necessidade de reestruturação significativa do sistema.

---

## Objetivos

O GACS foi concebido para:

* gerenciar experimentos;
* cadastrar componentes semicondutores;
* armazenar dados experimentais em banco de dados;
* permitir a entrada manual de dados;
* permitir a colagem direta de dados provenientes de planilhas;
* importar arquivos CSV separados por ponto e vírgula;
* reconhecer valores em notação científica;
* gerar gráficos científicos;
* comparar curvas experimentais;
* auxiliar na análise e caracterização elétrica de componentes;
* servir como base para futura aquisição automática de dados laboratoriais.

---

## Núcleo do protótipo

O desenvolvimento inicial está concentrado em um fluxo simples, consistente e reutilizável:

```text
Entrada de dados
        ↓
Geração de gráficos
        ↓
Análise e caracterização
```

Essas três etapas devem utilizar a mesma estrutura de dados e permanecer consistentes entre si.

O operador poderá utilizar um fluxo rápido, sem ser obrigado a preencher previamente todos os cadastros detalhados do sistema.

---

## Tecnologias

* Java 21;
* JavaFX;
* Maven;
* MySQL 8;
* JDBC;
* Git;
* GitHub.

A diretriz tecnológica do projeto é utilizar uma pilha simples e coesa, evitando a introdução de frameworks ou dependências que não apresentem benefício claro para o protótipo.

---

## Arquitetura do projeto

A arquitetura inicial segue uma organização em camadas:

```text
br.uel.gacs
├── application
├── controller
├── dao
├── model
└── util
```

Responsabilidades gerais:

* `application`: inicialização e composição da aplicação;
* `controller`: integração com a interface JavaFX, validações, regras do processo e coordenação de um ou mais DAOs;
* `dao`: persistência e acesso ao banco de dados;
* `model`: representação das entidades do domínio;
* `util`: recursos auxiliares reutilizáveis.

Os módulos devem possuir responsabilidades claras e baixo acoplamento.

O fluxo arquitetônico desta fase é:

```text
Interface JavaFX → Controller → um ou mais DAOs → MySQL
```

Não será utilizada uma camada `service` na versão atual. Sua eventual criação
dependerá de uma necessidade concreta identificada durante a evolução do
projeto e de nova decisão arquitetônica documentada.

---

## Modelo inicial do domínio

O núcleo inicial do sistema será baseado nas seguintes entidades:

* `Experimento`;
* `Coluna`;
* `DadoColuna`.

O experimento será a unidade central do sistema.

Uma coluna representará uma grandeza experimental e poderá desempenhar inicialmente o papel de eixo X ou eixo Y.

Uma coluna Y poderá referenciar:

* uma coluna X compartilhada por várias colunas Y;
* uma coluna X exclusiva para aquela coluna Y.

Os valores X e Y serão associados por meio do número lógico da medida.

Esse modelo servirá diretamente como base para:

```text
Entrada
→ Gráfico
→ Caracterização
```

---

## Entrada de dados

O GACS deverá aceitar inicialmente:

* digitação manual;
* colagem direta de dados provenientes de planilhas;
* importação de arquivos CSV;
* ponto e vírgula como separador padrão de colunas;
* ponto ou vírgula como separador decimal;
* valores escritos em notação científica.

Exemplo de arquivo CSV:

```csv
VDS;IDS
0,0;1,20E-9
0,5;3,70E-7
1,0;2,15E-5
```

Os valores serão armazenados numericamente no banco de dados. A notação científica será tratada na entrada, validação e apresentação dos dados.

---

## Estrutura do repositório

A estrutura prevista para o projeto é:

```text
gacs-tcc/
├── AGENTS.md
├── README.md
├── pom.xml
├── docs/
│   ├── 03-arquitetura.md
│   ├── 04-modelo-de-dados.md
│   ├── 05-regras-de-negocio.md
│   └── 06-convencoes-de-codigo.md
├── database/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
└── .gitignore
```

---

## Documentação

O projeto mantém sua documentação técnica junto ao código-fonte.

Os principais documentos são:

* `AGENTS.md` — instruções operacionais para agentes de desenvolvimento;
* `docs/03-arquitetura.md` — organização arquitetônica do sistema;
* `docs/04-modelo-de-dados.md` — entidades, relacionamentos e restrições;
* `docs/05-regras-de-negocio.md` — regras funcionais consolidadas;
* `docs/06-convencoes-de-codigo.md` — padrões para implementação.

A documentação deverá ser atualizada quando decisões arquitetônicas relevantes forem tomadas.

---

## Roadmap de desenvolvimento

O desenvolvimento seguirá uma estratégia incremental:

1. Modelagem do domínio.
2. Documentação da arquitetura.
3. Estrutura do banco de dados.
4. Implementação das classes de modelo.
5. Persistência com JDBC.
6. Interface gráfica com JavaFX.
7. Entrada, edição, colagem e importação de dados.
8. Visualização de gráficos.
9. Ferramentas de análise e caracterização.
10. Relatórios.
11. Cadastro detalhado de componentes e equipamentos.
12. Integração futura com equipamentos laboratoriais.

---

## Estado do projeto

🚧 **Em desenvolvimento**

Atualmente, o projeto encontra-se na fase de consolidação da arquitetura, documentação técnica, modelagem do banco de dados e implementação das primeiras classes do domínio.

O modelo inicial do banco de dados já foi definido para sustentar de forma consistente a entrada de dados, a geração de gráficos e a caracterização.

---

## Filosofia do projeto

O GACS nasce com a proposta de contribuir para o desenvolvimento científico por meio de uma ferramenta aberta, organizada, confiável e extensível.

O projeto busca incentivar:

* a reprodutibilidade de experimentos;
* o compartilhamento do conhecimento;
* a organização dos dados científicos;
* a colaboração entre estudantes, pesquisadores e desenvolvedores;
* a aplicação de boas práticas de engenharia de software no desenvolvimento de ferramentas científicas.

---

## Licença

Este projeto será disponibilizado como **software livre e de código aberto**, sob a **Licença MIT**.

Isso permitirá que estudantes, pesquisadores e desenvolvedores possam utilizar, estudar, modificar e distribuir o software, preservando os devidos créditos aos autores.

---

> *A ciência evolui quando o conhecimento é compartilhado.*
