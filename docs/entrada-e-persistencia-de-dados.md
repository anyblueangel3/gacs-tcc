# Entrada e persistência de dados

## Formas de entrada

- digitação direta;
- colagem de planilha;
- importação CSV com separador `;`.

A primeira linha editável contém o nome da coluna. As demais células aceitam
somente valores numéricos `Double`, incluindo notação científica. Na entrada,
são aceitos ponto ou vírgula decimal; no banco, o valor permanece numérico e
sem arredondamento destrutivo.

## Edição da planilha

- Enter desce e Shift+Enter sobe;
- Tab avança e Shift+Tab retorna;
- setas permanecem disponíveis;
- inserir ou excluir célula desloca somente a coluna afetada;
- inserir ou excluir linha remaneja todas as colunas;
- excluir coluna não renumera os rótulos das demais colunas.

Alternar entre digitação, colagem e importação não apaga os dados existentes.
Novos blocos são acrescentados a partir de colunas disponíveis.

## Persistência

Salvar um experimento persiste cabeçalho, colunas e medidas em uma transação.
Falhas provocam `rollback`. Reabrir o experimento recompõe a planilha pelos
rótulos e números das medidas.

Os dados de FET devem ser fornecidos diretamente no SI: tensão em volt,
corrente em ampere e, quando informada, temperatura em kelvin.
