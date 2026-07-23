$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$raizProjeto = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$diretorioInstalador = Join-Path $raizProjeto "target\instalador"
$diretorioEntrada = Join-Path $diretorioInstalador "input"
$diretorioImagem = Join-Path $diretorioInstalador "app-image"
$arquivoJar = Join-Path $raizProjeto "target\gacs-tcc-0.1.0-SNAPSHOT.jar"
$jarNoPacote = Join-Path $diretorioEntrada "gacs-tcc-0.1.0-SNAPSHOT.jar"

function Exigir-Comando {
    param([Parameter(Mandatory = $true)][string]$Nome)

    if (-not (Get-Command $Nome -ErrorAction SilentlyContinue)) {
        throw "O comando '$Nome' não foi encontrado no PATH."
    }
}

Exigir-Comando "mvn"
Exigir-Comando "jpackage"

Push-Location $raizProjeto
try {
    & mvn clean package
    if ($LASTEXITCODE -ne 0) {
        throw "O Maven não conseguiu construir o projeto."
    }

    if (-not (Test-Path $arquivoJar)) {
        throw "O JAR principal não foi encontrado: $arquivoJar"
    }

    Copy-Item -Path $arquivoJar -Destination $jarNoPacote -Force

    $aplicacaoAnterior = Join-Path $diretorioImagem "GACS"
    if (Test-Path $aplicacaoAnterior) {
        Remove-Item -Path $aplicacaoAnterior -Recurse -Force
    }
    New-Item -ItemType Directory -Path $diretorioImagem -Force | Out-Null

    & jpackage `
        --type app-image `
        --name "GACS" `
        --dest $diretorioImagem `
        --input $diretorioEntrada `
        --main-jar "gacs-tcc-0.1.0-SNAPSHOT.jar" `
        --main-class "br.uel.gacs.GacsLauncher" `
        --app-version "0.1.0" `
        --vendor "Ronaldo Rodrigues Godoi" `
        --description "Gerenciador para Analise e Caracterizacao de Componentes Semicondutores" `
        --java-options "-Dfile.encoding=UTF-8"

    if ($LASTEXITCODE -ne 0) {
        throw "O jpackage não conseguiu criar a aplicação autônoma."
    }

    $executavel = Join-Path $diretorioImagem "GACS\GACS.exe"
    if (-not (Test-Path $executavel)) {
        throw "O executável do GACS não foi criado."
    }

    Write-Host ""
    Write-Host "Aplicação autônoma criada com sucesso:"
    Write-Host $executavel
} finally {
    Pop-Location
}
