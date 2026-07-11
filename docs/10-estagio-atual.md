# 10 — Estágio atual do projeto

## 1. Identificação do registro

- **Projeto acadêmico:** GACS-TCC — Geração de Aplicativo para Caracterização de Componentes Semicondutores
- **Aplicativo:** GACS — Gerenciador para Análise e Caracterização de Componentes Semicondutores
- **Data deste registro:** 11 de julho de 2026
- **Tecnologias atuais:** Java 21, JavaFX 21, Maven, JDBC e MySQL 8.0.28

Este documento registra o estágio funcional alcançado pelo GACS e consolida as decisões arquiteturais e as implementações realizadas em 11 de julho de 2026.

## 2. Decisões arquiteturais consolidadas

### 2.1. Camadas adotadas

Para o estágio atual do protótipo, foi adotado o fluxo:

```text
Interface JavaFX → Controller → DAO → MySQL
```

A camada `service` não será utilizada neste momento. As regras do sistema ficam concentradas nos controllers, enquanto os DAOs permanecem responsáveis pelo acesso direto ao banco de dados.

Essa escolha privilegia uma arquitetura simples e compatível com o escopo atual. Uma camada `service` poderá ser introduzida futuramente somente se aparecer uma necessidade concreta, como repetição de regras entre controllers, operações envolvendo vários DAOs ou transações mais complexas.

### 2.2. Responsabilidade dos DAOs

Os DAOs são responsáveis por:

- obter conexões por meio de `ConexaoBanco`;
- escrever e executar comandos SQL;
- usar `PreparedStatement` e `ResultSet`;
- converter registros do banco em entidades Java;
- devolver o resultado técnico de cada operação;
- propagar `SQLException` quando ocorrer uma falha real de persistência;
- abrir e fechar uma conexão própria em cada operação com `try-with-resources`.

Os métodos dos DAOs não retornam obrigatoriamente `boolean`. O tipo de retorno representa a natureza da operação: entidade opcional, lista, identificador gerado ou resposta lógica.

### 2.3. Responsabilidade dos controllers

Os controllers recebem os dados das interfaces, aplicam validações e regras do sistema e solicitam as operações necessárias aos DAOs.

Foi adotada inicialmente a correspondência de um controller para cada DAO, admitindo exceções quando um caso de uso justificar uma classe própria, como ocorre com a autenticação.

O pacote oficial é:

```text
br.uel.gacs.controller
```

Todas as classes controller terminam em `Ctlr`, por exemplo:

```text
UsuarioCtlr
LoginCtlr
```

### 2.4. Sessão do usuário

`SessaoUsuario` pertence ao pacote:

```text
br.uel.gacs.application
```

A classe mantém em memória o usuário autenticado durante a execução do GACS. Não deve existir outra cópia de `SessaoUsuario` em `util`.

## 3. Persistência e inicialização do banco

### 3.1. Conexões

`ConexaoBanco` centraliza a configuração das conexões JDBC. Cada DAO solicita sua própria conexão com `DadosGACS` e a fecha ao final da operação.

As configurações de host, porta, banco, usuário e senha são carregadas de:

```text
src/main/resources/database.properties
```

### 3.2. Criação inicial

`CriadorBancoDados` verifica se `DadosGACS` existe. Quando o banco ainda não existe, cria o banco e as sete tabelas oficiais na ordem exigida pelas chaves estrangeiras:

1. `Usuario`;
2. `Experimento`;
3. `Coluna`;
4. `DadoColuna`;
5. `Curva`;
6. `Grafico`;
7. `CurvaGrafico`.

O método:

```java
CriadorBancoDados.criarSeNaoExistir()
```

passou a retornar:

- `true` quando o banco é criado na execução atual;
- `false` quando o banco já existia;
- `SQLException` quando a verificação ou criação não pode ser concluída.

Não estão previstas migrações de esquema nesta etapa. A criação integral ocorre somente no primeiro acesso.

## 4. Persistência de usuários

Foi implementado `UsuarioDAO` em:

```text
br.uel.gacs.dao.UsuarioDAO
```

Operações atualmente disponíveis:

- inserir usuário e devolver o ID gerado pelo MySQL;
- buscar usuário por ID;
- buscar usuário por e-mail;
- listar todos os usuários;
- atualizar os dados do usuário;
- ativar ou desativar usuário;
- verificar a existência de determinado e-mail;
- verificar se existe algum usuário cadastrado.

As buscas individuais retornam `Optional<Usuario>`, a listagem retorna `List<Usuario>`, a inserção retorna o identificador gerado e as operações naturalmente lógicas retornam `boolean`.

## 5. Proteção das senhas

Foi implementada a classe:

```text
br.uel.gacs.util.SenhaUtil
```

As senhas não são armazenadas em texto comum. `SenhaUtil` utiliza `PBKDF2WithHmacSHA256`, disponível no Java 21, com:

- sal aleatório individual;
- 210.000 iterações;
- hash de 256 bits;
- comparação por `MessageDigest.isEqual`;
- armazenamento conjunto do algoritmo, número de iterações, sal e hash.

Como o recurso faz parte do Java 21, não foi necessário adicionar outra dependência ao `pom.xml`.

## 6. Controle de usuários

Foi implementado:

```text
br.uel.gacs.controller.UsuarioCtlr
```

Responsabilidades atuais:

- validar os campos necessários ao cadastro;
- normalizar nome e e-mail;
- impedir e-mail duplicado;
- transformar a senha em hash antes da persistência;
- definir automaticamente as datas de criação e alteração;
- cadastrar o primeiro usuário obrigatoriamente como `ADMINISTRADOR` e ativo;
- realizar buscas e listagem;
- atualizar dados;
- alterar senha;
- ativar ou desativar usuários.

O CRUD visual administrativo de usuários ainda não foi implementado. O cadastro existente destina-se somente ao administrador inicial.

## 7. Autenticação e sessão

Foi implementado:

```text
br.uel.gacs.controller.LoginCtlr
```

O fluxo de autenticação:

1. normaliza o e-mail informado;
2. localiza o usuário por meio de `UsuarioDAO`;
3. rejeita usuário inexistente ou inativo;
4. verifica a senha usando `SenhaUtil`;
5. inicia `SessaoUsuario` quando as credenciais são válidas;
6. devolve o usuário autenticado;
7. permite encerrar a sessão por meio da operação de saída.

`SessaoUsuario` oferece operações para:

- iniciar uma sessão somente com usuário ativo;
- encerrar a sessão;
- verificar se existe usuário logado;
- obter opcionalmente o usuário logado;
- exigir um usuário logado, gerando erro quando não houver sessão.

## 8. Fluxo visual implementado

Foi implementada a classe:

```text
br.uel.gacs.application.FluxoInicialAplicacao
```

`App` passou a delegar a inicialização visual e funcional a essa classe.

O fluxo atual é:

```text
Abrir o GACS
    → mostrar janela de processamento
    → verificar ou criar o banco em segundo plano
    → verificar se há usuários
        → sem usuários: cadastrar administrador inicial
        → com usuários: mostrar login
    → iniciar SessaoUsuario
    → abrir tela principal provisória
```

### 8.1. Janela de inicialização

A janela de inicialização:

- possui 400 × 200 pixels;
- aparece centralizada no monitor;
- informa que o banco está sendo verificado e preparado;
- apresenta um indicador visual de progresso;
- permanece responsiva porque a operação de banco é executada em uma `Task` separada da thread do JavaFX;
- apresenta uma mensagem de erro quando a conexão ou criação não pode ser concluída.

### 8.2. Cadastro inicial

Quando não existe usuário, o sistema apresenta uma tela específica para cadastrar o primeiro administrador com:

- nome;
- e-mail;
- senha;
- confirmação da senha.

O perfil é definido obrigatoriamente como `ADMINISTRADOR`. Após o cadastro, a sessão é iniciada e a tela principal é aberta.

Essa tela possui 400 × 300 pixels para comportar os quatro campos.

### 8.3. Login

A tela de login:

- possui 400 × 200 pixels;
- aparece centralizada;
- recebe e-mail e senha;
- não revela se a falha ocorreu por e-mail, senha ou situação inativa;
- abre a tela principal somente após autenticação válida.

### 8.4. Tela principal provisória

A tela principal atual é provisória. Ela apenas:

- confirma a entrada no GACS;
- apresenta o nome do usuário conectado;
- oferece o botão `Sair`;
- encerra a sessão e retorna ao login.

## 9. Testes realizados

Em 11 de julho de 2026, foram executados com sucesso:

- `mvn clean compile` após a implementação do DAO;
- `mvn clean compile` após `SenhaUtil` e `UsuarioCtlr`;
- `mvn clean compile` após `SessaoUsuario` e `LoginCtlr`;
- `mvn clean compile` após o fluxo visual inicial;
- execução por `mvn javafx:run`;
- exibição da janela de processamento;
- detecção inicial do MySQL indisponível e apresentação de erro ao usuário;
- nova execução com o MySQL disponível;
- criação de `DadosGACS` e de suas tabelas;
- cadastro do primeiro administrador;
- geração e persistência do hash da senha;
- início da sessão;
- abertura da tela principal provisória;
- tentativa de login com senha incorreta;
- tentativa de login com senha vazia;
- login válido;
- encerramento da sessão e retorno ao login.

O fluxo completo da primeira execução e a autenticação básica encontram-se funcionais.

## 10. Estrutura relevante atual

```text
src/main/java/br/uel/gacs/
├── App.java
├── application/
│   ├── FluxoInicialAplicacao.java
│   └── SessaoUsuario.java
├── controller/
│   ├── LoginCtlr.java
│   └── UsuarioCtlr.java
├── dao/
│   ├── ConexaoBanco.java
│   ├── CriadorBancoDados.java
│   └── UsuarioDAO.java
├── model/
│   ├── PerfilUsuario.java
│   └── Usuario.java
└── util/
    └── SenhaUtil.java
```

As demais entidades do domínio permanecem no pacote `model` conforme a documentação específica do modelo de dados.

## 11. Situação alcançada

O GACS deixou de ser apenas uma estrutura de classes e passou a possuir um fluxo executável completo de entrada no sistema. Atualmente, o aplicativo é capaz de:

- abrir uma interface responsiva;
- informar o usuário durante a preparação inicial;
- criar sua infraestrutura de persistência no primeiro acesso;
- cadastrar com segurança o primeiro administrador;
- autenticar usuários ativos;
- manter a sessão do usuário;
- encerrar a sessão;
- abrir uma área principal provisória.

Esse conjunto constitui a base funcional para o desenvolvimento das telas e operações ligadas aos experimentos.

## 12. Próximos passos recomendados

Após a pausa, a continuação sugerida é:

1. revisar visualmente e organizar as telas atuais;
2. separar gradualmente as interfaces em arquivos FXML, quando isso trouxer benefício concreto;
3. implementar a estrutura definitiva da tela principal;
4. iniciar `ExperimentoDAO` e `ExperimentoCtlr`;
5. desenvolver o cadastro e a seleção de experimentos;
6. prosseguir para colunas, entrada de medidas, curvas e gráficos;
7. implementar o CRUD administrativo de usuários somente quando necessário ao protótipo.

As próximas alterações devem preservar o fluxo inicial e a autenticação que já foram testados e aprovados.
