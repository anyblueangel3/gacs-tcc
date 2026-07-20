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

## Tecnologias

- Java 21;
- JavaFX;
- Maven;
- MySQL 8;
- JDBC.

O projeto prioriza simplicidade, rastreabilidade científica, responsabilidades
claras e evolução incremental. Frameworks adicionais só devem ser introduzidos
quando houver necessidade concreta e decisão documentada.
