# 05 — Regras de Negócio

## Objetivo

Este documento define as regras de negócio que devem ser respeitadas pela aplicação e pelo banco de dados.

---

# Usuário

- O e-mail deve ser único.
- A senha deve ser armazenada apenas como hash.
- O perfil é representado pelo enum `PerfilUsuario`.
- Usuários inativos não podem autenticar.

---

# Experimento

- O nome do experimento é obrigatório.
- Um experimento pode possuir até 50 colunas.
- As observações utilizam texto longo.

---

# Coluna

- Toda coluna pertence a um único experimento.
- O rótulo é único dentro do experimento.
- O rótulo segue a sequência A, B, ..., AX.
- O rótulo é permanente após ser atribuído.
- O nome da coluna pode ser alterado sem alterar o rótulo.

---

# DadoColuna

- A chave lógica é composta por `idColuna + numeroDaMedida`.
- Não pode existir duplicidade dessa combinação.
- Cada coluna admite até 10.000 medidas.
- As medidas devem permanecer ordenáveis por `numeroDaMedida`.

---

# Curva

- Toda curva associa exatamente uma coluna X e uma coluna Y.
- `idColunaX` deve ser diferente de `idColunaY`.
- As duas colunas devem pertencer ao mesmo experimento.
- Uma coluna pode participar de várias curvas.
- Os pontos da curva são obtidos pareando valores com o mesmo `numeroDaMedida`.

---

# Gráfico

- Um gráfico pode conter várias curvas.
- Uma curva pode pertencer a vários gráficos.

---

# CurvaGrafico

- A chave lógica é `idGrafico + numeroCurva`.
- A combinação `idGrafico + idCurva` não pode se repetir.
- `numeroCurva` inicia em 1.
- `numeroCurva` define a ordem de exibição, legenda e processamento das curvas.

---

# Entrada de dados

- A aplicação deve aceitar digitação manual.
- Deve aceitar colagem de blocos do Excel.
- Deve aceitar importação CSV.
- Deve aceitar vírgula ou ponto decimal.
- Deve aceitar notação científica.

---

# Integridade

- Não criar entidades fora das sete tabelas oficiais.
- Não criar `TipoEixo`.
- Não converter enums em tabelas sem decisão arquitetônica.
- Toda alteração estrutural deve primeiro atualizar a documentação.

---

# Modelo oficial

As únicas tabelas desta versão são:

1. Usuario
2. Experimento
3. Coluna
4. DadoColuna
5. Curva
6. Grafico
7. CurvaGrafico
