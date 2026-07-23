# GACS-TCC

> **Geração de Aplicativo de Caracterização de Componentes Semicondutores**

## Sobre o projeto

O **GACS-TCC** é um projeto de Trabalho de Conclusão de Curso (TCC) do
Bacharelado em Física da Universidade Estadual de Londrina (UEL).

O projeto tem como objetivo o desenvolvimento do **GACS — Gerenciador para
Análise e Caracterização de Componentes Semicondutores**, um software destinado
ao gerenciamento de experimentos, armazenamento de dados experimentais,
geração de gráficos e caracterização elétrica de componentes semicondutores.

Desde sua concepção, o sistema é desenvolvido com foco em simplicidade,
organização, confiabilidade, extensibilidade e futura integração com
equipamentos de laboratório.

O desenvolvimento é realizado de forma colaborativa entre o autor e o ChatGPT
(OpenAI), utilizado como assistente de arquitetura, modelagem, documentação,
revisão técnica e proposição de melhorias. O projeto também utiliza o Codex
como agente de implementação no ambiente de desenvolvimento, sempre sob
supervisão e conforme as decisões documentadas.

---

## Equipe de trabalho do projeto GACS-TCC

Professor Dr. Edson Laureto  
Ronaldo Rodrigues Godoi  
ChatGPT (OpenAI)  
Codex — OpenAI's coding agent (OpenAI)  

---

## Primeira versão

A primeira versão do GACS é direcionada ao estudo e à caracterização elétrica
de:

* Diodos;
* Transistores de Efeito de Campo (FETs).

A arquitetura é projetada para permitir a futura inclusão de novos dispositivos
semicondutores sem necessidade de reestruturação significativa do sistema.

Para a entrega de 3 de agosto de 2026, o escopo funcional está congelado. Após
a caracterização de diodos, a análise de famílias de curvas de saída de FET, a
integração entre saída e transferência e a preparação do instalador, o trabalho
fica concentrado em correções, testes, documentação e preparação da entrega.

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

O desenvolvimento inicial está concentrado em um fluxo simples, consistente e
reutilizável:

```text
Entrada de dados
        ↓
Geração de gráficos
        ↓
Análise e caracterização
```

Essas três etapas utilizam a mesma estrutura de dados e permanecem consistentes
entre si.

O operador pode utilizar um fluxo rápido, sem ser obrigado a preencher
previamente todos os cadastros detalhados previstos para evoluções futuras do
sistema.

O fluxo funcional consolidado é:

```text
Experimento → Colunas → Curvas → Gráficos → Caracterização
```

---

## Funcionalidades implementadas

O protótipo dispõe atualmente de:

* inicialização do banco e criação automática de suas tabelas;
* criação do primeiro usuário administrador;
* autenticação, sessão e controle de permissões;
* cadastro e manutenção de usuários pelo administrador;
* criação, salvamento, reabertura e exclusão de experimentos;
* entrada de dados por digitação manual;
* colagem direta de dados provenientes de planilhas;
* importação de arquivos CSV;
* operações de edição de linhas, colunas e células;
* persistência transacional das colunas e medidas;
* criação de curvas;
* criação de gráficos compostos por uma ou mais curvas;
* visualização, impressão e salvamento do gráfico em PNG;
* caracterização elétrica de diodos;
* caracterização genérica de FETs;
* relatórios textuais das caracterizações.

---

## Caracterização de diodos

A caracterização de diodos contempla:

* seleção da curva direta e da curva reversa ou de ruptura;
* análise de uma curva completa quando ela contém as duas regiões;
* linearização da equação de Shockley na região direta;
* corrente de saturação \(I_s\);
* fator de idealidade \(n\);
* tensão térmica;
* tensão direta em uma corrente de referência;
* resistência dinâmica;
* estimativa da resistência em série;
* corrente de fuga média e máxima;
* tensão de ruptura em uma corrente reversa de referência;
* resistência dinâmica efetiva na região pós-ruptura;
* coeficientes de determinação e advertências dos ajustes.

---

## Caracterização de FETs

A caracterização genérica de FETs não pressupõe antecipadamente MOSFET, JFET ou
outro modelo físico específico. Ela contempla:

* configuração persistente do tipo da curva;
* registro de \(V_{GS}\) constante nas curvas de saída;
* registro de \(V_{DS}\) constante nas curvas de transferência;
* análise individual ou conjunta de curvas de saída;
* seleção, por `CheckBox`, das curvas que participarão da execução;
* intervalos ôhmico e de saturação próprios para cada curva de saída;
* condutância \(G_{DS,\mathrm{ômica}}\) e resistência
  \(R_{DS,\mathrm{ômica}}\);
* condutância de saída \(g_{ds}\) e resistência de saída \(r_o\);
* tensão de joelho operacional pela interseção dos ajustes;
* transcondutância \(g_m\) por intervalo;
* transcondutância local por janelas de três pontos;
* ganho intrínseco \(A_{v0}=g_mr_o\);
* relatórios individuais e consolidados com \(R^2\) e advertências.

Os intervalos específicos das análises permanecem apenas na memória enquanto a
tela de caracterização está aberta. A curva de transferência pode ser analisada
independentemente das curvas de saída. Uma curva de saída compatível é exigida
somente para cálculos que dependem dela, como o ganho intrínseco.

---

## Tecnologias

* Java 21;
* JavaFX 21;
* Maven;
* MySQL 8;
* JDBC;
* Git;
* GitHub;
* `jpackage`;
* Inno Setup.

A diretriz tecnológica do projeto é utilizar uma pilha simples e coesa,
evitando a introdução de frameworks ou dependências que não apresentem
benefício claro para o protótipo.

---

## Arquitetura do projeto

A arquitetura segue uma organização em camadas:

```text
br.uel.gacs
├── application
├── controller
├── dao
├── model
└── util
```

Responsabilidades gerais:

* `application`: inicialização, interface JavaFX e composição da aplicação;
* `controller`: validações, regras do processo e coordenação de um ou mais DAOs;
* `dao`: persistência, acesso ao banco de dados e todo o SQL;
* `model`: representação das entidades e enums do domínio;
* `util`: recursos auxiliares reutilizáveis.

Os módulos devem possuir responsabilidades claras e baixo acoplamento.

O fluxo arquitetônico desta fase é:

```text
Interface JavaFX → Controller → um ou mais DAOs → MySQL
```

Não será utilizada uma camada `service` na versão atual. Sua eventual criação
dependerá de uma necessidade concreta identificada durante a evolução do
projeto e de nova decisão arquitetônica documentada.

Operações compostas utilizam uma única conexão, com `commit` em caso de sucesso
e `rollback` quando ocorre uma falha.

---

## Modelo vigente do domínio

O banco de dados possui oito tabelas:

* `Usuario`;
* `Experimento`;
* `Coluna`;
* `DadoColuna`;
* `Curva`;
* `CurvaFet`;
* `Grafico`;
* `CurvaGrafico`.

O experimento é a unidade central do sistema. Cada experimento pertence ao
usuário que o criou e agrega suas colunas de dados.

Uma coluna representa uma grandeza experimental e pode desempenhar o papel de
eixo X ou eixo Y. Os valores de diferentes colunas são associados por meio do
número lógico da medida.

`Curva` relaciona uma coluna X e uma coluna Y. `Grafico` reúne uma ou mais
curvas por meio de `CurvaGrafico`.

`CurvaFet` especializa opcionalmente uma curva, registrando seu tipo — saída ou
transferência — e a tensão mantida constante.

Esse modelo serve diretamente como base para:

```text
Entrada
→ Persistência
→ Gráfico
→ Caracterização
```

---

## Entrada de dados

O GACS aceita:

* digitação manual;
* colagem direta de dados provenientes de planilhas;
* importação de arquivos CSV;
* ponto e vírgula como separador padrão de colunas em CSV;
* ponto ou vírgula como separador decimal;
* valores escritos em notação científica.

Exemplo de arquivo CSV:

```csv
VDS;IDS
0,0;1,20E-9
0,5;3,70E-7
1,0;2,15E-5
```

Os nomes das colunas são textuais. Os valores das medidas são armazenados
numericamente como `Double`. A notação científica é tratada na entrada,
validação, persistência e apresentação dos dados.

A planilha admite até 50 colunas e 10.000 medidas por coluna.

---

## Estrutura do repositório

A estrutura principal do projeto é:

```text
gacs-tcc/
├── AGENTS.md
├── LICENSE
├── README.md
├── pom.xml
├── dados-para-teste/
├── docs/
│   ├── README.md
│   ├── arquitetura.md
│   ├── caracterizacao-de-diodos.md
│   ├── caracterizacao-de-fet.md
│   ├── entrada-e-persistencia-de-dados.md
│   ├── estado-atual-e-proximos-passos.md
│   ├── graficos-e-curvas.md
│   ├── implantacao.md
│   ├── modelo-de-dados.md
│   ├── regras-de-negocio.md
│   ├── visao-geral.md
│   ├── decisoes/
│   └── historico/
├── instalador/
│   ├── gacs.iss
│   ├── gerar-aplicacao.ps1
│   └── gerar-instalador.ps1
└── src/
    └── main/
        ├── java/
        └── resources/
```

---

## Documentação

O projeto mantém sua documentação técnica junto ao código-fonte.

Os principais documentos canônicos são:

* `AGENTS.md` — instruções operacionais para agentes de desenvolvimento;
* `docs/README.md` — índice da documentação vigente;
* `docs/visao-geral.md` — visão, escopo e tecnologias;
* `docs/arquitetura.md` — organização arquitetônica;
* `docs/modelo-de-dados.md` — entidades, tabelas e relacionamentos;
* `docs/regras-de-negocio.md` — regras funcionais;
* `docs/entrada-e-persistencia-de-dados.md` — entrada, edição e persistência;
* `docs/graficos-e-curvas.md` — curvas e gráficos;
* `docs/caracterizacao-de-diodos.md` — caracterização de diodos;
* `docs/caracterizacao-de-fet.md` — caracterização de FETs;
* `docs/implantacao.md` — empacotamento e instalação;
* `docs/estado-atual-e-proximos-passos.md` — situação corrente.

Os documentos numerados anteriores permanecem em `docs/historico/` como
registros dos sprints. Eles preservam a evolução do projeto, mas não substituem
as fontes canônicas atuais.

A documentação deve ser atualizada quando decisões arquitetônicas relevantes
forem tomadas. Documentos consolidados devem ser atualizados por integração do
novo conteúdo, preservando informações históricas, autoria, objetivos,
filosofia e demais partes ainda válidas.

---

## Roadmap de desenvolvimento

O desenvolvimento segue uma estratégia incremental:

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

Os itens de 1 a 10 constituem o núcleo já desenvolvido ou em consolidação na
primeira versão. O cadastro detalhado de componentes, a aquisição automática e
a integração direta com equipamentos permanecem como possibilidades futuras,
sujeitas à avaliação do orientador e da equipe.

---

## Estado do projeto

🚧 **Protótipo funcional em preparação para distribuição**

O GACS possui atualmente um núcleo funcional integrado de entrada de dados,
persistência, curvas, gráficos e caracterização elétrica de diodos e FETs.

Em 23 de julho de 2026, iniciou-se a preparação da distribuição para Windows:

* o Maven compila o projeto e reúne as dependências;
* o `jpackage` cria uma aplicação autônoma com runtime Java;
* o Inno Setup cria o instalador, os atalhos e o desinstalador;
* o `database.properties` vigente permanece incluído como recurso nesta
  primeira versão do protótipo.

A sequência imediata é:

1. gerar e testar a aplicação autônoma fora do VS Code;
2. validar o acesso ao MySQL e as funcionalidades principais;
3. compilar e testar o instalador do Windows;
4. integrar a verificação e preparação do MySQL;
5. realizar correções, testes finais e revisão da documentação.

---

## Execução durante o desenvolvimento

Configure `src/main/resources/database.properties` para o MySQL local e use:

```powershell
mvn clean compile
mvn javafx:run
```

Para gerar a aplicação autônoma no Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\instalador\gerar-aplicacao.ps1
```

Após a validação da aplicação autônoma, o instalador poderá ser gerado com:

```powershell
powershell -ExecutionPolicy Bypass -File .\instalador\gerar-instalador.ps1
```

---

## Filosofia do projeto

O GACS nasce com a proposta de contribuir para o desenvolvimento científico por
meio de uma ferramenta aberta, organizada, confiável e extensível.

O projeto busca incentivar:

* a reprodutibilidade de experimentos;
* o compartilhamento do conhecimento;
* a organização dos dados científicos;
* a colaboração entre estudantes, pesquisadores e desenvolvedores;
* a aplicação de boas práticas de engenharia de software no desenvolvimento de
  ferramentas científicas.

---

## Licença

Este projeto será disponibilizado como **software livre e de código aberto**,
sob a **Licença MIT**.

Isso permitirá que estudantes, pesquisadores e desenvolvedores possam utilizar,
estudar, modificar e distribuir o software, preservando os devidos créditos aos
autores.

---

> *A ciência evolui quando o conhecimento é compartilhado.*
