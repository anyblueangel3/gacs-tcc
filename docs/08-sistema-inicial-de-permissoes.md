# 08 -- Sistema Inicial de Permissões

## Objetivo

Este documento define o modelo inicial de autenticação e autorização do
GACS-TCC.

O objetivo desta primeira versão é fornecer um mecanismo simples, seguro
e suficientemente flexível para o desenvolvimento do protótipo. A
arquitetura deverá permitir evolução futura sem exigir grandes
alterações na estrutura do sistema.

As definições deste documento representam a versão inicial e poderão ser
revisadas durante o desenvolvimento do projeto.

------------------------------------------------------------------------

# 1. Perfis de usuário

O sistema utilizará o enum `PerfilUsuario`, composto pelos seguintes
perfis:

-   ADMINISTRADOR
-   PESQUISADOR
-   OPERADOR
-   CONSULTA

Nesta primeira versão do sistema, os perfis **PESQUISADOR** e
**OPERADOR** possuirão exatamente as mesmas permissões.

------------------------------------------------------------------------

# 2. Propriedade dos experimentos

Todo experimento pertence obrigatoriamente a um usuário, por meio do
campo `idUsuario` da tabela `Experimento`.

Nesta primeira versão não existirão participantes, grupos de trabalho ou
permissões específicas por experimento.

------------------------------------------------------------------------

# 3. Regras gerais de permissão

## ADMINISTRADOR

Pode realizar qualquer operação no sistema, incluindo gerenciamento de
usuários e manutenção de qualquer experimento.

## PESQUISADOR

Pode visualizar todos os experimentos e dados, criar experimentos,
alterar, inserir dados e excluir apenas os experimentos de sua
propriedade.

## OPERADOR

Nesta versão inicial possui exatamente as mesmas permissões do perfil
PESQUISADOR.

## CONSULTA

Pode apenas visualizar experimentos e seus respectivos dados.

------------------------------------------------------------------------

# 4. Cadastro de usuários

Somente o perfil ADMINISTRADOR poderá cadastrar, alterar, ativar ou
desativar usuários.

Usuários não deverão ser excluídos fisicamente do banco de dados.

------------------------------------------------------------------------

# 5. Sessão do usuário

Após autenticação, a aplicação manterá o usuário autenticado através da
classe `SessaoUsuario`.

------------------------------------------------------------------------

# 6. Controle de permissões

As permissões deverão ser centralizadas em um componente responsável
pela autorização.

Sempre que possível, operações não permitidas deverão permanecer ocultas
ou desabilitadas na interface.

As regras de autorização também deverão ser verificadas na camada de
serviço.

------------------------------------------------------------------------

# 7. Evolução futura

O modelo foi mantido simples para atender ao protótipo e poderá evoluir
futuramente sem alterar sua estrutura básica.
