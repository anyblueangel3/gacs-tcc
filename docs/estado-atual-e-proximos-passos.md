# Estado atual e próximos passos

## Implementado

- inicialização do banco e criação das sete tabelas originais;
- autenticação, sessão e permissões;
- CRUD de usuários para administrador;
- criação, salvamento, reabertura e exclusão de experimentos;
- planilha com digitação, colagem, CSV e operações de linhas, colunas e células;
- persistência transacional das colunas e medidas;
- criação, listagem e visualização de gráficos e curvas;
- caracterização de diodos nas regiões direta, reversa e pós-ruptura.

## Implementado neste incremento

- oitava tabela `CurvaFet` na criação inicial de `DadosGACS`;
- enum `TipoCurvaFet`, entidade `CurvaFet` e `CurvaFetDAO`;
- nenhuma migração ou verificação complementar em bancos existentes.
- núcleo matemático de saída, transferência, joelho, `g_m` local e ganho
  intrínseco em `CaracterizacaoFetCtlr`.
- botão FET integrado à tela `TelaCaracterizacaoFet`;
- configuração persistente de curvas de saída e transferência;
- cálculo e relatório individual das regiões de saída e da transcondutância.

## Aprovado e ainda não implementado

- integração visual do ganho intrínseco entre curvas compatíveis;
- testes completos com famílias experimentais de FET.

## Próxima sequência

1. compilar e testar o novo fluxo no ambiente Java 21 do projeto;
2. corrigir eventuais problemas observados na interface;
3. integrar o ganho intrínseco entre curvas compatíveis;
4. validar uma família completa de curvas;
5. conferir novamente código e documentação.
