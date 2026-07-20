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

`SessaoUsuario`:

- pertence ao pacote `application`;
- não é entidade persistente e não corresponde a uma tabela;
- mantém no máximo um `Usuario` autenticado durante a execução;
- permite iniciar a sessão somente com usuário não nulo e ativo;
- permite consultar se existe usuário autenticado;
- fornece o usuário autenticado às camadas que necessitem verificar identidade e permissões;
- permite encerrar a sessão, removendo a referência ao usuário autenticado.

A senha e o hash da senha não deverão ser copiados para campos próprios da sessão. A sessão manterá apenas a referência ao objeto `Usuario` autenticado.

A interface inicial da classe será composta pelas operações estáticas:

```java
iniciar(Usuario usuario)
estaAutenticado()
getUsuarioAutenticado()
encerrar()
```

------------------------------------------------------------------------

# 6. Controle de permissões

As permissões deverão ser centralizadas em um componente responsável
pela autorização.

Sempre que possível, operações não permitidas deverão permanecer ocultas
ou desabilitadas na interface.

As regras de autorização também deverão ser verificadas pelos controllers que
coordenam as operações. A interface poderá ocultar ou desabilitar ações, mas
essa proteção visual não substituirá a validação realizada pelo controller.

Não será criada uma camada `service` apenas para executar verificações de
permissão nesta fase do protótipo.

------------------------------------------------------------------------

# 7. Evolução futura

O modelo foi mantido simples para atender ao protótipo e poderá evoluir
futuramente sem alterar sua estrutura básica.
