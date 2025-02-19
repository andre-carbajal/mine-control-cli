#define MyAppName "MineControl CLI"
#define MyAppVersion "0.1.0"
#define MyAppPublisher "Andre Carbajal"
#define MyAppExeName "mine-control-cli.exe"

[Setup]
AppId={{642feabb-e491-404b-8e7d-27f56f64615e}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
OutputBaseFilename=mine-control-cli-setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Files]
Source: "target\mine-control-cli.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "target\*.dll"; DestDir: "{app}"; Flags: ignoreversion

[Registry]
Root: HKLM; Subkey: "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"; \
    ValueType: expandsz; ValueName: "Path"; ValueData: "{olddata};{app}"

[Run]
Filename: "{sys}\cmd.exe"; \
    Parameters: "/C setx PATH ""%PATH%;{app}"""; \
    Flags: runhidden

[UninstallDelete]
Type: files; Name: "{app}\*.*"
Type: dirifempty; Name: "{app}"