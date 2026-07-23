#define NomeAplicativo "GACS"
#define VersaoAplicativo "0.1.0"
#define AutorAplicativo "Ronaldo Rodrigues Godoi"
#define ExecutavelAplicativo "GACS.exe"

[Setup]
AppId={{4E7E7A63-5FB6-4C43-A6C8-4A2B24C907C1}
AppName={#NomeAplicativo}
AppVersion={#VersaoAplicativo}
AppPublisher={#AutorAplicativo}
AppCopyright=Copyright (c) 2026 Ronaldo Rodrigues Godoi
DefaultDirName={autopf}\GACS
DefaultGroupName=GACS
DisableProgramGroupPage=yes
LicenseFile=..\LICENSE
OutputDir=..\target\instalador\saida
OutputBaseFilename=Instalador-GACS-0.1.0
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
UninstallDisplayIcon={app}\{#ExecutavelAplicativo}
CloseApplications=yes
RestartApplications=no

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "Criar um atalho na área de trabalho"; \
    GroupDescription: "Atalhos adicionais:"; Flags: unchecked

[Files]
Source: "..\target\instalador\app-image\GACS\*"; DestDir: "{app}"; \
    Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\GACS"; Filename: "{app}\{#ExecutavelAplicativo}"; \
    WorkingDir: "{app}"
Name: "{autodesktop}\GACS"; Filename: "{app}\{#ExecutavelAplicativo}"; \
    WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#ExecutavelAplicativo}"; \
    Description: "Executar o GACS"; Flags: postinstall nowait skipifsilent
