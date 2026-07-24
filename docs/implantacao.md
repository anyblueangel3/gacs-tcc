# Implantação do GACS

## Plataforma inicial

A primeira distribuição instalável do GACS é destinada ao Windows de 64 bits.
O processo combina:

- Maven, para compilar o projeto e reunir as dependências de execução;
- `jpackage` do JDK 21, para criar uma aplicação autônoma com runtime Java;
- Inno Setup, para criar o instalador, os atalhos e o desinstalador.

Java, JavaFX, Maven e o Connector/J não precisam estar instalados na máquina
do usuário final. Maven, JDK 21 e Inno Setup são requisitos apenas da máquina
que constrói o instalador.

## Arquivos de construção

| Arquivo | Responsabilidade |
|---|---|
| `instalador/gerar-aplicacao.ps1` | Compila e cria a aplicação autônoma |
| `instalador/gerar-instalador.ps1` | Cria a aplicação e compila o instalador |
| `instalador/gacs.iss` | Define a instalação pelo Inno Setup |
| `GacsLauncher.java` | Inicia o JavaFX no pacote autônomo |

## Geração da aplicação autônoma

No PowerShell, a partir da raiz do projeto:

```powershell
powershell -ExecutionPolicy Bypass -File .\instalador\gerar-aplicacao.ps1
```

O executável para o primeiro teste será criado em:

```text
target\instalador\app-image\GACS\GACS.exe
```

Esse executável deve ser testado antes da compilação do instalador.

## Geração do instalador

Com o Inno Setup instalado:

```powershell
powershell -ExecutionPolicy Bypass -File .\instalador\gerar-instalador.ps1
```

O resultado será:

```text
target\instalador\saida\Instalador-GACS-0.1.0.exe
```

## Validação da distribuição

A distribuição `0.1.0` foi validada no Windows 11 em 24 de julho de 2026.
O procedimento confirmou:

- compilação do projeto e criação da aplicação autônoma;
- geração do instalador pelo Inno Setup 6;
- instalação em `C:\Program Files\GACS`;
- abertura pelo atalho do menu Iniciar;
- funcionamento do login e acesso aos experimentos persistidos;
- execução da caracterização de uma família de três curvas de saída de FET e
  uma curva de transferência;
- cálculo integrado de `g_m`, `g_ds`, `r_o` e ganho intrínseco;
- encerramento, reabertura, desinstalação e reinstalação sem falhas.

A desinstalação remove os arquivos e atalhos do GACS, mas não altera o banco
`DadosGACS`. Após a reinstalação, os usuários e experimentos anteriormente
registrados permaneceram disponíveis.

## Banco de dados

O `database.properties` existente permanece empacotado como recurso da
aplicação nesta primeira versão. O GACS continua responsável por criar
`DadosGACS` e suas tabelas quando o banco ainda não existir.

A integração da instalação e da configuração do MySQL permanece como evolução
posterior. A distribuição atual pressupõe um servidor MySQL 8 previamente
instalado e compatível com as configurações de conexão empacotadas.
