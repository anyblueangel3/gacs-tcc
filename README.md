# GACS-TCC

**Geração de Aplicativo de Caracterização de Componentes Semicondutores**

Projeto de TCC do Bacharelado em Física da Universidade Estadual de Londrina
(UEL), de autoria de Ronaldo Rodrigues Godoi.

O projeto desenvolve o **GACS — Gerenciador para Análise e Caracterização de
Componentes Semicondutores**, integrando dados experimentais, curvas, gráficos
e caracterização elétrica.

## Fluxo central

```text
Experimento → Colunas → Curvas → Gráficos → Caracterização
```

## Escopo atual

- autenticação e controle de acesso;
- entrada manual, colagem e importação CSV;
- persistência dos experimentos em MySQL;
- criação e composição de curvas e gráficos;
- caracterização de diodos;
- caracterização genérica de FETs em desenvolvimento.

## Tecnologias

- Java 21;
- JavaFX;
- Maven;
- MySQL 8;
- JDBC.

Não há camada `service` nesta fase. O fluxo principal é:

```text
JavaFX → Controller → um ou mais DAOs → MySQL
```

## Documentação

O índice da documentação vigente está em [`docs/README.md`](docs/README.md).
Documentos numerados anteriores foram preservados em `docs/historico/` como
registros dos sprints e não constituem a especificação atual.

## Execução

Configure `src/main/resources/database.properties` para o MySQL local e use:

```bash
mvn clean compile
mvn javafx:run
```

## Estado

O protótipo está em desenvolvimento. Consulte
[`docs/estado-atual-e-proximos-passos.md`](docs/estado-atual-e-proximos-passos.md).

## Licença

Consulte o arquivo [`LICENSE`](LICENSE).
