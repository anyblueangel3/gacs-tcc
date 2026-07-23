$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$raizProjeto = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$scriptAplicacao = Join-Path $PSScriptRoot "gerar-aplicacao.ps1"
$scriptInno = Join-Path $PSScriptRoot "gacs.iss"

& $scriptAplicacao
if ($LASTEXITCODE -ne 0) {
    throw "Não foi possível preparar a aplicação autônoma."
}

$compiladorInno = Get-Command "ISCC.exe" -ErrorAction SilentlyContinue
if ($compiladorInno) {
    $caminhoIscc = $compiladorInno.Source
} else {
    $candidatos = @(
        (Join-Path ${env:ProgramFiles(x86)} "Inno Setup 6\ISCC.exe"),
        (Join-Path $env:ProgramFiles "Inno Setup 6\ISCC.exe"),
        (Join-Path ${env:ProgramFiles(x86)} "Inno Setup 7\ISCC.exe"),
        (Join-Path $env:ProgramFiles "Inno Setup 7\ISCC.exe")
    )
    $caminhoIscc = $candidatos |
        Where-Object { $_ -and (Test-Path $_) } |
        Select-Object -First 1
}

if (-not $caminhoIscc) {
    throw "O compilador do Inno Setup (ISCC.exe) não foi encontrado."
}

Push-Location $raizProjeto
try {
    & $caminhoIscc $scriptInno
    if ($LASTEXITCODE -ne 0) {
        throw "O Inno Setup não conseguiu criar o instalador."
    }

    $instalador = Join-Path $raizProjeto `
        "target\instalador\saida\Instalador-GACS-0.1.0.exe"
    if (-not (Test-Path $instalador)) {
        throw "O arquivo final do instalador não foi encontrado."
    }

    Write-Host ""
    Write-Host "Instalador criado com sucesso:"
    Write-Host $instalador
} finally {
    Pop-Location
}
