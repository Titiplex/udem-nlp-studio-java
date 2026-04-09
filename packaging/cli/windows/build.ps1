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

Write-Host "==> Building CLI with Maven"
mvn -pl core -am clean package

$InputDir = Join-Path $RepoRoot "core\target"
$DestDir = Join-Path $RepoRoot "core\target\installer"
$TempDir = Join-Path $RepoRoot "core\target\jpackage-temp"
$ResourceDir = Join-Path $RepoRoot "packaging\cli\windows\resources"

New-Item -ItemType Directory -Force -Path $DestDir | Out-Null
New-Item -ItemType Directory -Force -Path $TempDir | Out-Null

$JarFile = Get-ChildItem $InputDir -Filter "nlp-studio-core-*-all.jar" | Sort-Object Name -Descending | Select-Object -First 1
if (-not $JarFile)
{
    throw "Shaded CLI jar not found in $InputDir"
}
$JarName = $JarFile.Name
$JarPath = $JarFile.FullName

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
    "--resource-dir", $ResourceDir,
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