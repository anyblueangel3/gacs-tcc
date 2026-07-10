# 07 – Diretrizes de Uso do Java 21

## Objetivo

Este documento estabelece como o projeto GACS deve utilizar os recursos modernos do Java 21.

A diretriz central é:

> Utilizar recursos modernos e estáveis do Java 21 quando eles aumentarem a clareza, a segurança de tipos e a facilidade de manutenção.

Recursos novos não devem ser usados apenas por novidade ou para aumentar artificialmente a complexidade do código.

---

## Recursos recomendados

O projeto poderá utilizar livremente, quando apropriado:

- `enum`;
- expressões `switch`;
- pattern matching;
- `instanceof` com pattern matching;
- `var` em variáveis locais quando o tipo continuar evidente;
- API moderna de datas (`java.time`);
- text blocks;
- coleções imutáveis;
- Streams em transformações simples e legíveis;
- `Optional` principalmente em retornos de consultas;
- `record` para objetos auxiliares, resultados e transporte de dados.

Exemplos adequados de uso de `record`:

```java
public record PontoExperimental(double x, double y) {
}
```

```java
public record ResultadoCaracterizacao(
        String parametro,
        double valor,
        String unidade) {
}
```

---

## Uso com cautela

Os seguintes recursos devem ser usados somente quando houver benefício claro:

- `record` para entidades persistentes;
- classes `sealed`;
- Streams em operações complexas;
- programação funcional excessiva;
- concorrência;
- reflexão;
- metaprogramação.

A legibilidade deve prevalecer sobre a concisão.

---

## Recursos não permitidos no protótipo inicial

Não utilizar:

- recursos preview;
- `--enable-preview`;
- APIs experimentais;
- construções que dificultem a compilação com Maven ou a integração com JavaFX;
- dependências adicionadas apenas para substituir recursos já disponíveis no Java 21.

---

## Entidades persistentes

Entidades do domínio como:

- `Experimento`;
- `Coluna`;
- `DadoColuna`;
- `Usuario`;

devem ser implementadas inicialmente como classes convencionais mutáveis.

Motivos:

- IDs podem ser nulos antes da persistência;
- formulários JavaFX poderão editar os dados;
- JDBC poderá preencher os objetos;
- o operador autorizado poderá alterar registros;
- a modelagem ainda está em evolução.

Não utilizar `record` para essas entidades nesta fase.

---

## Tipos recomendados

- IDs: `Long`;
- datas e horários: `LocalDateTime`;
- datas sem horário: `LocalDate`;
- valores medidos: `Double`, sujeito a futura revisão;
- estados controlados: `enum`;
- textos longos: `String`.

Evitar APIs antigas como:

```java
java.util.Date
```

---

## `var`

`var` pode ser utilizado somente em variáveis locais quando o tipo for evidente.

Uso aceitável:

```java
var experimento = experimentoDao.buscarPorId(id);
```

Uso a evitar:

```java
var resultado = processar();
```

quando o tipo ou significado não forem claros.

---

## Streams

Streams devem ser usados quando tornarem a transformação mais clara.

Exemplo adequado:

```java
var valoresOrdenados = dados.stream()
        .sorted(Comparator.comparing(DadoColuna::getNumeroDaMedida))
        .toList();
```

Evitar cadeias longas, efeitos colaterais e lógica de negócio difícil de depurar.

---

## `Optional`

`Optional` é recomendado principalmente para retornos de consultas:

```java
Optional<Experimento> buscarPorId(Long id);
```

Não utilizar `Optional` como atributo de entidade.

---

## Text blocks

Text blocks podem ser utilizados para consultas SQL longas:

```java
var sql = """
        SELECT id_experimento, nome_experimento
        FROM experimento
        WHERE id_experimento = ?
        """;
```

Consultas simples podem permanecer em strings convencionais.

---

## Validação e segurança de tipos

Sempre que um conjunto de valores for limitado, preferir `enum` a textos livres.

Exemplos previstos:

- `TipoEixo`;
- perfil de usuário;
- situação de um registro;
- tipo de permissão.

Evitar comparações frágeis com strings dispersas pelo código.

---

## Clareza acima de sofisticação

O código deve ser compreensível por:

- estudantes;
- pesquisadores;
- futuros colaboradores;
- avaliadores do TCC.

A utilização de recursos modernos deve servir ao projeto, e não transformar o projeto em demonstração de sintaxe avançada.

---

## Diretriz para o Codex

Ao implementar código no GACS, o Codex deve:

1. utilizar somente recursos estáveis do Java 21;
2. evitar recursos preview;
3. preferir clareza e simplicidade;
4. não introduzir frameworks sem autorização;
5. não converter entidades persistentes em `record`;
6. utilizar `record` apenas em objetos auxiliares e imutáveis;
7. utilizar `enum` para estados e categorias controladas;
8. manter compatibilidade com Maven, JavaFX e JDBC;
9. explicar usos avançados que não sejam imediatamente evidentes;
10. preservar as decisões documentadas no `AGENTS.md`.

---

## Princípio final

> O GACS deve ser moderno sem ser obscuro, expressivo sem ser excessivamente complexo e tecnologicamente atualizado sem depender de recursos experimentais.
