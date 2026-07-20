# 06 — Convenções de Código

## Objetivo

Definir os padrões de codificação utilizados no projeto GACS.

---

# Linguagem

- Java 21.

---

# Idioma

- Classes do domínio em português.
- Métodos e atributos em camelCase.
- Classes em PascalCase.

---

# Estrutura de pacotes

```text
application
controller
dao
model
util
```

---

# Classes do pacote model

As classes oficiais desta versão são:

```text
PerfilUsuario (enum)

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

---

# Identificadores

Toda entidade com chave própria utiliza:

```java
Long id;
```

Chaves estrangeiras:

```java
Long idExperimento;
Long idColuna;
Long idColunaX;
Long idColunaY;
Long idGrafico;
Long idCurva;
```

---

# Tipos principais

```java
Long
Integer
Short
Double
Boolean
String
LocalDateTime
```

---

# Persistência

- JDBC.
- MySQL 8.
- Não utilizar frameworks ORM.

---

# JavaFX

Toda interface gráfica deve utilizar JavaFX.

---

# Documentação

Classes públicas devem possuir Javadoc sucinto.

---

# Organização

- SQL apenas em DAO.
- Regras do processo, validações e coordenação entre DAOs em Controller.
- Model representa apenas entidades.
- Controller utiliza DAOs, mas não contém SQL.
- Operações interligadas podem ser coordenadas por um único controller, sem a
  obrigação de criar um controller para cada DAO envolvido.

Não utilizar camada `service` nesta fase do protótipo. Sua eventual introdução
dependerá de necessidade concreta e de nova decisão arquitetônica documentada.

---

# Qualidade

- Código simples.
- Métodos curtos.
- Evitar duplicação.
- Evitar dependências desnecessárias.
