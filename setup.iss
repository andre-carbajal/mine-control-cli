#define MyAppName "MineControl CLI"
#define MyAppVersion "2.2.4"
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
Source: "target\*.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "target\*.dll"; DestDir: "{app}"; Flags: ignoreversion

[Code]
procedure ModifyPath();
var
  Path: string;
  AppDir: string;
begin
  if not RegQueryStringValue(HKLM, 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment', 'Path', Path) then
  begin
    Exit;
  end;

  AppDir := ExpandConstant('{app}');

  if Pos(LowerCase(AppDir), LowerCase(Path)) = 0 then
  begin
    if Pos(';', Path[Length(Path)]) <> 0 then
      Path := Path + AppDir
    else
      Path := Path + ';' + AppDir;

    RegWriteExpandStringValue(HKLM, 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment', 'Path', Path);
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    ModifyPath();
  end;
end;

[UninstallDelete]
Type: files; Name: "{app}\*.*"
Type: dirifempty; Name: "{app}"