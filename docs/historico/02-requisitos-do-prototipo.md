# 02 — Requisitos do Protótipo

## Objetivo

O primeiro protótipo do GACS deve permitir que um usuário registre experimentos, organize dados experimentais, forme curvas, monte gráficos e preserve essas informações em banco de dados.

---

# Requisitos Funcionais

## RF01 – Usuários
- Autenticar usuários cadastrados.
- Controlar perfil de acesso por `PerfilUsuario`.

## RF02 – Experimentos
- Criar, editar, excluir e consultar experimentos.
- Registrar nome, data e observações.

## RF03 – Colunas
- Criar até 50 colunas por experimento.
- Exibir rótulos no padrão A, B, ..., AX.
- Permitir definir `nomeColuna`.

## RF04 – Entrada de dados
- Digitação direta em tabela.
- Colagem de blocos do Excel.
- Importação CSV.
- Até 10.000 medidas por coluna.

## RF05 – Curvas
- Criar curvas associando uma coluna X e uma coluna Y.
- Permitir reutilizar uma mesma coluna X em várias curvas.
- Nomear cada curva.

## RF06 – Gráficos
- Criar gráficos.
- Adicionar uma ou várias curvas ao gráfico.
- Definir a ordem das curvas por `numeroCurva`.
- Permitir reutilizar uma curva em diferentes gráficos.

## RF07 – Visualização
- Plotar todas as curvas pertencentes ao gráfico.
- Exibir legenda conforme `numeroCurva`.

## RF08 – Persistência
- Salvar e recuperar todas as sete entidades oficiais:
  - Usuario
  - Experimento
  - Coluna
  - DadoColuna
  - Curva
  - Grafico
  - CurvaGrafico

---

# Requisitos Não Funcionais

- Java 21.
- JavaFX.
- Maven.
- MySQL 8.
- JDBC.
- Interface semelhante a planilha.
- Aceitar ponto e vírgula como separador CSV.
- Aceitar vírgula ou ponto decimal.
- Aceitar notação científica.

---

# Restrições

- Máximo de 50 colunas por experimento.
- Máximo de 10.000 medidas por coluna.
- Não criar tabelas além das sete oficiais nesta versão.
- Não utilizar frameworks de persistência.

---

# Fluxo principal

```text
Login
→ Experimento
→ Colunas
→ Entrada de dados
→ Curvas
→ Gráficos
→ Visualização
→ Persistência
```
