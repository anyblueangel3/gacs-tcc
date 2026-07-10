# 02 -- Requisitos do Protótipo

## Objetivo

Definir o escopo funcional da primeira versão do GACS.

# Requisitos funcionais

RF-01 -- Criar, editar e consultar experimentos.

RF-02 -- Registrar observações do experimento.

RF-03 -- Criar colunas de dados associadas a um experimento.

RF-04 -- Inserir valores manualmente.

RF-05 -- Colar dados diretamente de planilhas.

RF-06 -- Importar arquivos CSV delimitados por ponto e vírgula.

RF-07 -- Aceitar valores em notação científica.

RF-08 -- Associar cada coluna Y à sua coluna X correspondente.

RF-09 -- Armazenar os dados em banco de dados.

RF-10 -- Recuperar experimentos salvos.

RF-11 -- Gerar gráficos a partir das colunas X e Y.

RF-12 -- Disponibilizar os pares (x,y) para os algoritmos de
caracterização.

# Requisitos não funcionais

RNF-01 -- Java 21.

RNF-02 -- JavaFX para a interface.

RNF-03 -- Maven para gerenciamento do projeto.

RNF-04 -- MySQL 8 como banco de dados inicial.

RNF-05 -- JDBC para persistência inicial.

RNF-06 -- Arquitetura em camadas.

RNF-07 -- Código documentado e organizado.

RNF-08 -- Domínio modelado em português.

RNF-09 -- Modularidade e baixo acoplamento.

RNF-10 -- Evolução incremental sem perda de compatibilidade sempre que
possível.

# Fora do escopo da primeira versão

-   aquisição automática de dados;
-   múltiplos tipos avançados de caracterização;
-   gerenciamento avançado de usuários e permissões;
-   integração com equipamentos laboratoriais;
-   geração completa de relatórios científicos.

# Critério de sucesso do protótipo

O protótipo será considerado bem-sucedido quando permitir que um
operador:

1.  crie um experimento;
2.  insira ou importe dados;
3.  gere um gráfico;
4.  disponibilize os dados para caracterização;
5.  salve e recupere o experimento posteriormente.
