# 11 — Padrão de DAOs e transações

## 1. Objetivo

Este documento estabelece o padrão oficial de acesso ao banco de dados e de
controle de transações do GACS-TCC.

As regras aqui definidas devem orientar todas as operações de persistência
implementadas a partir deste estágio do projeto.

---

## 2. Um DAO para cada entidade persistente

Cada entidade correspondente a uma tabela do banco de dados possuirá seu
próprio DAO:

```text
Usuario       → UsuarioDAO
Experimento   → ExperimentoDAO
Coluna        → ColunaDAO
DadoColuna    → DadoColunaDAO
Curva         → CurvaDAO
Grafico       → GraficoDAO
CurvaGrafico  → CurvaGraficoDAO
```

Cada DAO será responsável por:

- executar o SQL relacionado à sua tabela;
- realizar operações de inserção, consulta, atualização e exclusão quando
  esta for permitida pelo modelo;
- converter registros do banco em objetos do domínio;
- converter objetos do domínio nos parâmetros necessários ao SQL.

Nenhum SQL deverá ser escrito em classes de interface, controllers, entidades
de `model` ou utilitários.

---

## 3. Limites de responsabilidade dos DAOs

Um DAO não deverá chamar outro DAO.

Por exemplo, `ExperimentoDAO` não chamará `ColunaDAO`. O primeiro trabalhará
com a tabela `Experimento`, enquanto o segundo trabalhará com a tabela
`Coluna`.

Essa separação evita:

- dependências ocultas entre DAOs;
- ciclos de chamadas;
- duplicação de regras de coordenação;
- dificuldade para controlar transações;
- operações parcialmente concluídas.

Consultas com `JOIN` poderão ser implementadas quando necessárias, desde que
tenham finalidade claramente definida e permaneçam sob a responsabilidade do
DAO correspondente ao resultado principal da consulta.

---

## 4. Coordenação de operações interligadas

Uma operação funcional poderá utilizar vários DAOs.

Nesta fase do protótipo, o controller correspondente ao processo coordenará os
DAOs necessários. Portanto, o fluxo oficial será:

```text
Interface JavaFX → Controller → um ou mais DAOs → MySQL
```

Exemplo conceitual:

```text
ExperimentoCtlr
├── ExperimentoDAO
├── ColunaDAO
├── DadoColunaDAO
├── CurvaDAO
├── GraficoDAO
└── CurvaGraficoDAO
```

Se a coordenação crescer a ponto de tornar os controllers excessivamente
complexos, ela poderá ser extraída futuramente para a camada `service`. Essa
evolução não deverá exigir alteração das responsabilidades dos DAOs.

---

## 5. Regra geral para transações

Toda operação de escrita no banco de dados deverá ser executada dentro de uma
transação explicitamente controlada.

Isso inclui:

- inserções;
- atualizações;
- ativações e inativações;
- exclusões permitidas;
- operações compostas que alterem uma ou várias tabelas.

A transação deverá seguir obrigatoriamente este comportamento:

1. obter uma conexão com o banco de dados;
2. desativar o modo de confirmação automática;
3. executar todas as etapas da operação utilizando a mesma conexão;
4. executar `commit` somente depois que todas as etapas terminarem com
   sucesso;
5. executar `rollback` se qualquer etapa falhar;
6. restaurar o modo de confirmação automática quando a conexão puder ser
   reutilizada;
7. fechar a conexão por meio de `try-with-resources`.

O objetivo é assegurar a atomicidade: ou todas as alterações pertencentes à
operação são gravadas, ou nenhuma delas permanece no banco.

---

## 6. Compartilhamento da conexão

Todos os DAOs participantes de uma mesma transação deverão receber e utilizar
a mesma instância de `Connection`.

Um DAO chamado dentro de uma transação não deverá abrir outra conexão. Se cada
DAO utilizasse uma conexão diferente, o `commit` e o `rollback` não poderiam
controlar o conjunto completo da operação.

Os DAOs poderão oferecer dois tipos de método:

```java
inserir(Entidade entidade)
inserir(Connection conexao, Entidade entidade)
```

O método que recebe `Connection` será utilizado em operações compostas. Um
método autônomo de escrita deverá criar e controlar sua própria transação e
poderá delegar o SQL ao método que recebe a conexão.

---

## 7. Modelo padronizado de transação

O seguinte modelo deverá orientar operações de escrita coordenadas pelo
controller:

```java
try (Connection conexao = ConexaoBanco.obterConexaoBanco()) {
    conexao.setAutoCommit(false);

    try {
        experimentoDAO.inserir(conexao, experimento);
        colunaDAO.inserirTodos(conexao, colunas);
        dadoColunaDAO.inserirTodos(conexao, dados);

        conexao.commit();
    } catch (Exception excecao) {
        conexao.rollback();
        throw excecao;
    } finally {
        conexao.setAutoCommit(true);
    }
}
```

Quando ocorrer falha no próprio `rollback`, essa falha não deverá ocultar a
exceção original. A implementação deverá preservar a causa principal e poderá
registrar a falha de reversão como exceção suprimida.

---

## 8. Consultas somente de leitura

Operações exclusivamente de consulta não precisam desativar o `autoCommit` nem
executar `commit` ou `rollback`, desde que não façam parte de uma operação
maior que exija uma visão consistente de vários dados.

Consultas simples continuarão usando conexão e recursos fechados por
`try-with-resources`.

Se várias leituras precisarem compor uma visão única e consistente, elas
poderão compartilhar uma transação de leitura, conforme a necessidade do
processo.

---

## 9. Responsabilidade pelo controle da transação

A classe que inicia uma transação será responsável por encerrá-la.

Consequentemente:

- o controller inicia, confirma ou reverte transações compostas;
- um DAO não executa `commit` ou `rollback` quando recebe uma conexão externa;
- um DAO pode controlar a transação apenas em um método autônomo que tenha
  aberto sua própria conexão;
- nenhuma classe deve confirmar uma transação iniciada por outra classe sem
  que essa responsabilidade esteja explicitamente definida.

---

## 10. Exclusões e integridade histórica

A existência de um método de exclusão em um DAO dependerá das regras de
negócio da entidade.

No caso de `Usuario`, não haverá exclusão física. O desligamento será realizado
por inativação, preservando referências, autoria e histórico.

Exclusões envolvendo dados experimentais deverão respeitar as chaves
estrangeiras e ser executadas em uma única transação, na ordem necessária para
manter a integridade referencial.

---

## 11. Tratamento de falhas

Falhas de persistência deverão:

- provocar `rollback` quando houver transação de escrita ativa;
- ser propagadas ao controller;
- ser apresentadas pela interface em linguagem compreensível ao operador;
- preservar detalhes técnicos na exceção para diagnóstico;
- nunca deixar o operador acreditar que uma operação parcial foi concluída.

Os DAOs continuarão lançando `SQLException`. A conversão dessa falha em uma
mensagem de interface será responsabilidade das camadas superiores.

---

## 12. Decisões consolidadas

Ficam estabelecidas as seguintes regras oficiais:

1. cada tabela possui um DAO correspondente;
2. cada DAO concentra o SQL de sua própria responsabilidade;
3. um DAO não chama outro DAO;
4. operações interligadas são coordenadas pelo controller nesta fase;
5. toda escrita ocorre em transação explicitamente controlada;
6. todos os DAOs de uma operação atômica compartilham a mesma `Connection`;
7. `commit` ocorre somente após o sucesso integral;
8. qualquer falha provoca tentativa de `rollback`;
9. quem inicia a transação é responsável por encerrá-la;
10. consultas simples de leitura não exigem transação manual;
11. controllers e interfaces não contêm SQL;
12. a futura introdução de serviços deverá preservar estes limites.

Este padrão deverá ser aplicado a partir da implementação de
`ExperimentoDAO` e, progressivamente, aos DAOs já existentes quando forem
alterados ou ampliados.
