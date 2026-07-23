# Documentação do GACS

Esta pasta separa a especificação vigente dos registros históricos.

## Fontes canônicas

| Assunto | Documento |
|---|---|
| Visão, escopo e tecnologias | `visao-geral.md` |
| Camadas e responsabilidades | `arquitetura.md` |
| Entidades, tabelas e relacionamentos | `modelo-de-dados.md` |
| Regras funcionais | `regras-de-negocio.md` |
| Entrada, edição e persistência | `entrada-e-persistencia-de-dados.md` |
| Curvas e gráficos | `graficos-e-curvas.md` |
| Caracterização de diodos | `caracterizacao-de-diodos.md` |
| Especificação da caracterização de FET | `caracterizacao-de-fet.md` |
| Empacotamento e instalação | `implantacao.md` |
| Situação corrente do desenvolvimento | `estado-atual-e-proximos-passos.md` |

`AGENTS.md` contém somente as instruções operacionais necessárias aos agentes.

## Autoridade e manutenção

Em caso de conflito, prevalece a decisão humana mais recente, seguida pelo
documento canônico específico do assunto. Uma informação normativa deve ter
uma única fonte canônica; os demais documentos devem apontar para ela.

Uma mudança deve atualizar apenas os documentos canônicos afetados e, quando
for arquiteturalmente relevante, receber um registro em `decisoes/`.

## Histórico

Os documentos numerados anteriores estão preservados em `historico/`. Eles
registram o estado do projeto em diferentes sprints, mas não definem o
comportamento vigente.

Arquivos cujo nome começa com `velho` são documentos substituídos e devem ser
ignorados por ferramentas de desenvolvimento.
