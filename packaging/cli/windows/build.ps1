param(
    [string]$Version = "0.1.0",
    [string]$Type = "exe"
)

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path "$PSScriptRoot\..\..\.."
Set-Location $RepoRoot

$AppName = "nlpstudio"
$Vendor = "Titiplex"
$MainClass = "org.titiplex.Main"

$TargetDir = Join-Path $RepoRoot "core\target"
$InputDir = Join-Path $RepoRoot "core\target\jpackage-input-windows"
$DestDir = Join-Path $RepoRoot "core\target\installer"
$TempDir = Join-Path $RepoRoot "core\target\jpackage-temp"

Write-Host "==> Building CLI with Maven"
mvn -pl core -am clean package

if (Test-Path $InputDir)
{
    Remove-Item $InputDir -Recurse -Force
}
if (Test-Path $DestDir)
{
    Remove-Item $DestDir -Recurse -Force
}
if (Test-Path $TempDir)
{
    Remove-Item $TempDir -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $InputDir | Out-Null
New-Item -ItemType Directory -Force -Path $DestDir | Out-Null
New-Item -ItemType Directory -Force -Path $TempDir | Out-Null

$JarFile = Get-ChildItem $TargetDir -Filter "nlp-studio-core-*-all.jar" |
        Sort-Object Name -Descending |
        Select-Object -First 1

if (-not $JarFile)
{
    throw "Shaded CLI jar not found in $TargetDir"
}

$JarName = $JarFile.Name
Copy-Item $JarFile.FullName (Join-Path $InputDir $JarName)

Write-Host "==> Using shaded CLI jar: $JarName"

$PrimaryIconPath = Join-Path $RepoRoot "packaging\resources\cli\nlpstudio.ico"
$LegacyIconPath = Join-Path $RepoRoot "packaging\resources\cli\nlp-studio-cli.ico"
$IconPath = $null

if (Test-Path $PrimaryIconPath)
{
    $IconPath = $PrimaryIconPath
}
elseif (Test-Path $LegacyIconPath)
{
    $IconPath = $LegacyIconPath
}

$jpackageArgs = @(
    "--type", $Type,
    "--name", $AppName,
    "--app-version", $Version,
    "--input", $InputDir,
    "--main-jar", $JarName,
    "--main-class", $MainClass,
    "--dest", $DestDir,
    "--temp", $TempDir,
    "--vendor", $Vendor,
    "--description", "NLP Studio command line tools",
    "--win-console",
    "--win-dir-chooser",
    "--win-shortcut-prompt",
    "--verbose"
)

if ($null -ne $IconPath)
{
    $jpackageArgs += @("--icon", $IconPath)
}

Write-Host "==> Packaging CLI with jpackage"
& jpackage @jpackageArgs

Write-Host "==> Done. Installer(s) available in $DestDir"