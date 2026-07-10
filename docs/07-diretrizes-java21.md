# 07 — Diretrizes Java 21

## Objetivo

Estabelecer as diretrizes técnicas para implementação do GACS utilizando Java 21.

---

# Plataforma

- Java 21 (LTS)
- JavaFX
- Maven
- MySQL 8
- JDBC

---

# Entidades do domínio

O pacote `model` deverá conter exclusivamente:

- PerfilUsuario (enum)
- Usuario
- Experimento
- Coluna
- DadoColuna
- Curva
- Grafico
- CurvaGrafico

---

# Tipos recomendados

Identificadores:
```java
Long
```

Numeração:
```java
Integer
Short
```

Valores medidos:
```java
Double
```

Datas:
```java
LocalDateTime
```

---

# Chaves

Entidades com chave própria:

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

`DadoColuna` utiliza chave lógica composta:

```text
idColuna + numeroDaMedida
```

`CurvaGrafico` utiliza:

```text
idGrafico + numeroCurva
```

---

# Coleções

Preferir:

```java
List<T>
```

quando a ordem for relevante.

---

# Datas

Utilizar exclusivamente `java.time`.

Não utilizar `java.util.Date` ou `Calendar`.

---

# Interface

Toda interface gráfica deverá ser implementada com JavaFX.

---

# Persistência

- JDBC puro.
- Não utilizar Hibernate.
- Não utilizar JPA.
- Não utilizar Spring Data.

---

# Recursos da linguagem

Podem ser utilizados:

- switch expressions;
- var para variáveis locais quando melhorar a legibilidade;
- try-with-resources;
- Optional quando apropriado.

Não utilizar recursos preview.

---

# Qualidade

- Javadoc em classes públicas.
- Métodos curtos.
- Responsabilidades bem definidas.
- Evitar duplicação de código.

---

# Arquitetura

Controller:
- interface.

Service:
- regras de negócio.

DAO:
- acesso ao banco.

Model:
- entidades.

Util:
- funções auxiliares.

---

# Restrições

- Não criar `TipoEixo`.
- Não criar tabelas além das sete oficiais.
- Não substituir IDs por referências complexas sem decisão arquitetônica.
- Toda alteração estrutural deve ser refletida primeiro na documentação.

---

# Estado atual

Modelo oficial:

1. Usuario
2. Experimento
3. Coluna
4. DadoColuna
5. Curva
6. Grafico
7. CurvaGrafico

Este documento deve permanecer alinhado ao `04-modelo-de-dados.md` e ao `AGENTS.md`.
