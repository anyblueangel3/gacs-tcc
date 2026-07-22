# Visão geral

O **GACS-TCC — Geração de Aplicativo de Caracterização de Componentes
Semicondutores** é o projeto acadêmico que desenvolve o **GACS — Gerenciador
para Análise e Caracterização de Componentes Semicondutores**.

O sistema integra:

```text
Experimento → Colunas → Curvas → Gráficos → Caracterização
```

## Escopo da primeira versão

- autenticação e permissões;
- criação, consulta, alteração e exclusão de experimentos;
- digitação, colagem e importação CSV de dados numéricos;
- persistência e reabertura dos experimentos;
- criação de curvas e gráficos;
- caracterização elétrica de diodos;
- caracterização elétrica de FETs.
- distribuição instalável para o ambiente Windows.

Para a entrega de 3 de agosto de 2026, o escopo funcional está congelado. Após
a análise de famílias de curvas de saída, a integração do ganho intrínseco e a
preparação do instalador, o trabalho limita-se a correções, testes, documentação
e preparação da versão de entrega. Famílias de curvas de transferência ficam
fora desta etapa.

## Tecnologias

- Java 21;
- JavaFX;
- Maven;
- MySQL 8;
- JDBC.

O projeto prioriza simplicidade, rastreabilidade científica, responsabilidades
claras e evolução incremental. Frameworks adicionais só devem ser introduzidos
quando houver necessidade concreta e decisão documentada.
