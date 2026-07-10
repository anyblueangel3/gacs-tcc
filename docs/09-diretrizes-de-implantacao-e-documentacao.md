# 09 -- Diretrizes de Implantação e Documentação

## Objetivo

Este documento registra diretrizes relacionadas à implantação do
GACS-TCC e à organização de sua documentação.

------------------------------------------------------------------------

# 1. Instalador do sistema

Ao final do desenvolvimento será produzido um instalador responsável por
instalar automaticamente o aplicativo e todos os requisitos necessários
para sua execução.

------------------------------------------------------------------------

# 2. Organização da documentação

Sempre que um documento oficial for substituído, sua versão anterior
permanecerá na pasta `docs`, iniciando obrigatoriamente com o prefixo:

`velho`

Exemplo:

`velho-04-modelo-de-dados.md`

------------------------------------------------------------------------

# 3. Leitura da documentação

Ferramentas de desenvolvimento, como o Codex, deverão ignorar qualquer
documento cujo nome comece com `velho`, considerando apenas a
documentação oficial vigente.

------------------------------------------------------------------------

# 4. Primeiro acesso ao sistema

Na primeira execução, caso não exista nenhum usuário cadastrado, o
sistema deverá abrir automaticamente o processo de criação do primeiro
usuário.

Esse usuário será criado obrigatoriamente com o perfil `ADMINISTRADOR`.

Após sua criação, novos usuários somente poderão ser cadastrados por um
administrador.

------------------------------------------------------------------------

# 5. Evolução futura

Essas diretrizes poderão ser ampliadas futuramente para contemplar
atualização automática, migração de banco de dados e outras
funcionalidades de implantação.
