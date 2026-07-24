# Estado atual e próximos passos

## Implementado

- inicialização do banco e criação das sete tabelas originais;
- autenticação, sessão e permissões;
- CRUD de usuários para administrador;
- criação, salvamento, reabertura e exclusão de experimentos;
- planilha com digitação, colagem, CSV e operações de linhas, colunas e células;
- persistência transacional das colunas e medidas;
- criação, listagem e visualização de gráficos e curvas;
- caracterização de diodos nas regiões direta, reversa e pós-ruptura;
- distribuição instalável `0.1.0` para Windows 11 de 64 bits, composta por
  aplicação autônoma, runtime Java e instalador Inno Setup;
- instalação, abertura pelo menu Iniciar, desinstalação e reinstalação
  validadas com preservação do banco `DadosGACS`.

## Implementado neste incremento

- oitava tabela `CurvaFet` na criação inicial de `DadosGACS`;
- enum `TipoCurvaFet`, entidade `CurvaFet` e `CurvaFetDAO`;
- nenhuma migração ou verificação complementar em bancos existentes.
- núcleo matemático de saída, transferência, joelho, `g_m` local e ganho
  intrínseco em `CaracterizacaoFetCtlr`.
- botão FET integrado à tela `TelaCaracterizacaoFet`;
- configuração persistente de curvas de saída e transferência;
- cálculo e relatório individual das regiões de saída e da transcondutância.
- cálculo consolidado de famílias de curvas de saída, ordenadas por `V_GS`.
- integração visual de `g_m` e `r_o` para obtenção de `A_v0 = g_m r_o`, com
  validação das condições de polarização.

## Aprovado e ainda não implementado

- testes completos com famílias experimentais de FET;
- integração da instalação e da configuração do MySQL 8.

## Próxima sequência

1. validar as caracterizações com famílias experimentais de FET;
2. corrigir eventuais problemas observados com dados experimentais;
3. conferir novamente código e documentação para a apresentação do protótipo;
4. planejar, em etapa posterior, a configuração do MySQL na distribuição.
