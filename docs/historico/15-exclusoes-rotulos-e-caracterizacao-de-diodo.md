# 15 — Exclusões, rótulos permanentes e caracterização de diodo

## 1. Identificação

- **Projeto:** GACS-TCC — Geração de Aplicativo de Caracterização de Componentes Semicondutores
- **Aplicativo:** GACS — Gerenciador para Análise e Caracterização de Componentes Semicondutores
- **Data:** 14 de julho de 2026
- **Tecnologias:** Java 21, JavaFX 21, Maven, JDBC e MySQL 8.0.28

## 2. Objetivo do incremento

Este incremento dá continuidade ao ciclo registrado no documento 14. O fluxo
do protótipo deixa de terminar apenas na plotagem e passa a produzir resultados
calculados a partir das curvas experimentais persistidas:

```text
Experimento
→ colunas e medidas
→ curvas cadastradas
→ gráficos
→ caracterização do componente
→ relatório calculado
```

Também foram implementadas operações de exclusão, preservação definitiva dos
rótulos das colunas, melhorias na apresentação dos pontos do gráfico e
organização dos arquivos versionados no Git.

## 3. Exclusão de experimentos

A lista de experimentos passou a apresentar o comando `Excluir Experimento`.
A operação é permitida somente:

- ao proprietário do experimento; ou
- a um usuário com perfil `ADMINISTRADOR`.

Antes da exclusão, o sistema solicita confirmação e informa que a ação também
remove os registros dependentes. A operação é executada em uma única transação,
na ordem necessária para respeitar as chaves estrangeiras:

1. associações entre curvas e gráficos;
2. curvas;
3. gráficos;
4. dados das colunas;
5. colunas;
6. experimento.

O `commit` somente ocorre quando todas as etapas são concluídas. Qualquer falha
provoca `rollback`, preservando a consistência do banco.

## 4. Exclusão de colunas e permanência dos rótulos

Foi incluído o comando `Excluir coluna` na barra de ferramentas da planilha.
O operador seleciona uma célula da coluna desejada e confirma a remoção. A
planilha deve manter ao menos uma coluna.

A exclusão é efetivada no banco quando o experimento é salvo. São removidos:

- os valores pertencentes à coluna;
- as curvas que utilizam a coluna como X ou Y;
- as associações dessas curvas com gráficos;
- o registro da própria coluna.

Os rótulos deixaram de ser calculados apenas pela posição visual. Cada coluna
agora conserva o valor de `rotulo` persistido na entidade `Coluna`.

Exemplo:

```text
Antes:  A, B, C, D
Excluir B
Depois: A, C, D
```

A antiga coluna C não passa a ser B. Essa regra preserva a identidade da coluna
durante toda a sua existência e evita que referências visuais mudem depois de
uma exclusão.

## 5. Ajustes na navegação e na plotagem

O atalho principal anteriormente denominado `Novo Gráfico` passou a se chamar
`Gráfico`, pois abre uma área que permite criar, consultar, alterar, excluir e
plotar gráficos e curvas.

A plotagem passou a apresentar cada medida experimental com um círculo
preenchido de aproximadamente 6 pixels, usando a mesma cor da respectiva curva.
As linhas de ligação foram mantidas.

Essa representação diferencia:

- os pontos efetivamente medidos ou importados; e
- os segmentos utilizados apenas para ligar visualmente as medidas.

Os marcadores integram o componente JavaFX do gráfico e, portanto, também são
incluídos na imagem PNG e na impressão.

## 6. Organização do repositório

Foi criado o arquivo `.gitignore` para impedir o versionamento de arquivos
gerados ou locais, incluindo:

- pasta `target` do Maven;
- configurações locais de editores e IDEs;
- arquivos temporários e de log;
- arquivos auxiliares do sistema operacional.

Os arquivos que já estavam rastreados devem ser retirados apenas do índice do
Git com `git rm --cached`, sem serem apagados do computador. Essa organização
mantém no repositório somente código-fonte, recursos, configuração necessária e
documentação pertinente.

## 7. Entrada para caracterização de componentes

Foi acrescentado à faixa de atalhos o botão grande
`Caracterização de Componente`, posicionado ao lado de `Gráfico`.

O botão permanece desabilitado quando:

- não há experimento aberto;
- o experimento ainda não foi salvo; ou
- o experimento não possui nenhuma curva persistida.

Ao retornar da tela de gráficos, o sistema consulta novamente a existência de
curvas e atualiza o estado do botão. Ao acioná-lo, o operador escolhe
inicialmente entre:

```text
Diodo
FET
```

A caracterização de diodo foi implementada neste incremento. A caracterização
de FET permanece reservada para uma etapa posterior. A estrutura permite a
inclusão futura de outros tipos de componentes sem modificar as sete tabelas
oficiais desta versão.

## 8. Dados sintéticos para verificação

Foram produzidos dois arquivos CSV com 100 medidas cada:

- curva direta, com tensão de 0,00 V a 0,99 V;
- curva reversa, com tensão de -0,01 V a -1,00 V.

Os dados representam um diodo idealizado pela equação de Shockley:

```text
I = Is [exp(V / (n Vt)) - 1]
```

Foram adotados na simulação:

```text
Is = 1,0 × 10⁻¹² A
n  = 1,8
T  = 300 K
```

Na polarização reversa anterior à ruptura, a corrente se aproxima de `-Is`.
Não foi simulada uma região de ruptura, pois não havia sido definida uma tensão
de ruptura para esse componente idealizado.

Os CSVs utilizaram o formato aceito pelo GACS: separador ponto e vírgula,
vírgula decimal, cabeçalhos textuais e valores numéricos em notação científica.

## 9. Caracterização de diodo

Ao escolher `Diodo`, o sistema abre uma tela que lista as curvas persistidas do
experimento. Cada opção apresenta:

- nome da curva;
- nomes das colunas X e Y;
- quantidade de pares de pontos;
- perfil predominantemente direto, reverso ou misto.

A interface permite informar:

- curva direta obrigatória;
- curva reversa opcional;
- temperatura em kelvin, com valor inicial de 300 K;
- corrente de referência, inicialmente 1 mA;
- tensão mínima e máxima da região de ajuste;
- tensão utilizada no cálculo da razão de retificação.

As curvas são lidas diretamente de `Curva`, `Coluna` e `DadoColuna`. Os pares
X–Y são formados pelo mesmo `numeroDaMedida`, sem duplicação dos dados
experimentais e sem criação de novas tabelas.

## 10. Cálculos implementados

### 10.1 Linearização da equação de Shockley

Na região direta em que a corrente é positiva, utiliza-se:

```text
ln(I) = ln(Is) + V / (n Vt)
```

O GACS realiza uma regressão linear de `ln(I)` em função de `V` no intervalo
selecionado pelo operador:

```text
ln(I) = a + bV
```

Os parâmetros são obtidos por:

```text
Is = exp(a)
n  = 1 / (b Vt)
Vt = kB T / q
```

São utilizadas as constantes exatas:

```text
kB = 1,380649 × 10⁻²³ J/K
q  = 1,602176634 × 10⁻¹⁹ C
```

O coeficiente de determinação `R²` é calculado para indicar a qualidade do
ajuste linear na região escolhida.

### 10.2 Tensão direta e resistência dinâmica

A tensão direta `Vf` é estimada na corrente de referência por interpolação
linear entre os dois pontos experimentais que envolvem essa corrente.

A resistência dinâmica é estimada localmente por diferenças finitas:

```text
rd ≈ ΔV / ΔI
```

São utilizados pontos próximos da corrente de referência. Quando os dados não
abrangem o valor solicitado, o relatório informa que o resultado não está
disponível no intervalo da curva.

### 10.3 Polarização reversa e razão de retificação

Quando uma curva reversa é selecionada, são calculados:

- módulo médio da corrente reversa;
- módulo máximo da corrente reversa;
- quantidade de pontos reversos utilizados;
- razão de retificação em uma tensão escolhida.

A razão de retificação é definida por:

```text
Rret = |I(+V)| / |I(-V)|
```

As correntes nas tensões positiva e negativa são obtidas por interpolação entre
os pontos correspondentes das curvas.

## 11. Relatório emitido

O relatório de caracterização apresenta:

- identificação do experimento;
- curvas direta e reversa utilizadas;
- temperatura considerada;
- quantidade de pontos disponíveis e usados no ajuste;
- intervalo de tensão analisado;
- tensão direta na corrente de referência;
- corrente de saturação `Is`;
- fator de idealidade `n`;
- resistência dinâmica;
- coeficiente `R²`;
- resultados da polarização reversa;
- razão de retificação;
- método de cálculo e observação sobre as limitações da estimativa.

O relatório é calculado em memória e não é persistido no banco nesta fase. O
botão `Copiar relatório` permite transferir o texto para outro documento.

## 12. Validação numérica

O algoritmo foi verificado com os dados sintéticos conhecidos. Utilizando a
região de 0,20 V a 0,85 V, foram obtidos aproximadamente:

```text
Pontos usados no ajuste: 66
Is:                       9,946 × 10⁻¹³ A
n:                        1,7993
R²:                       0,9999997
Vf em 1 mA:               0,9641 V
Resistência dinâmica:     50,68 ohm
Razão em ±0,8 V:          2,926 × 10⁷
```

Os resultados recuperam, dentro da discretização e do intervalo utilizados, os
valores `Is = 1,0 × 10⁻¹² A` e `n = 1,8` empregados na geração das curvas.

O sistema foi executado no ambiente oficial e o relatório foi emitido a partir
das curvas cadastradas no experimento.

## 13. Ajuste visual da tela

Após o primeiro teste, verificou-se que alguns rótulos dos parâmetros eram
abreviados pela interface. A grade foi reorganizada para reservar largura aos
textos e permitir que as caixas de seleção das curvas ocupem as colunas
restantes.

A janela passou a abrir com largura de 1100 pixels e largura mínima de 1050
pixels. O ajuste procura manter visíveis expressões como:

- `Incluir curva reversa`;
- `Corrente de referência (A)`;
- `Tensão mínima do ajuste (V)`;
- `Tensão máxima do ajuste (V)`;
- `Tensão da razão de retificação (V)`.

## 14. Classes incluídas e alteradas

As principais classes incluídas neste incremento foram:

```text
controller/CaracterizacaoDiodoCtlr.java
application/TelaCaracterizacaoComponente.java
application/TelaCaracterizacaoDiodo.java
```

Também foram alteradas ou integradas ao fluxo:

```text
application/MenuPrincipal.java
application/TelaPrincipal.java
application/PainelExperimento.java
application/PlanilhaExperimento.java
application/TelaPlotagemGrafico.java
controller/ExperimentoCtlr.java
controller/GraficoCtlr.java
dao/CurvaDAO.java
dao/CurvaGraficoDAO.java
dao/GraficoDAO.java
.gitignore
```

## 15. Limitações atuais e próximos passos

Permanecem para etapas futuras:

- impressão ou exportação própria do relatório de caracterização;
- persistência e histórico dos relatórios calculados;
- seleção visual da região de ajuste diretamente no gráfico;
- análise da tensão de ruptura em curvas que contenham essa região;
- modelos que considerem resistência série em correntes elevadas;
- comparação entre caracterizações realizadas em temperaturas diferentes;
- caracterização de FET;
- inclusão futura de outros componentes semicondutores.

Antes dessas expansões, devem ser concluídos os testes visuais da tela ampliada
e os testes com curvas experimentais reais, observando a influência do ruído e
da escolha do intervalo sobre `Is`, `n` e `R²`.

## 16. Estado ao final do incremento

O GACS passa a completar, pela primeira vez, o fluxo central proposto para o
protótipo:

```text
entrada de dados
→ persistência
→ criação de curvas
→ composição e plotagem de gráficos
→ caracterização física
→ relatório calculado
```

A caracterização utiliza as mesmas curvas persistidas empregadas nos gráficos,
mantendo uma única fonte de dados para entrada, visualização e análise. O
incremento demonstra que a arquitetura atual suporta a evolução do GACS de um
gerenciador de dados experimentais para uma ferramenta de apoio à
caracterização de componentes semicondutores.
