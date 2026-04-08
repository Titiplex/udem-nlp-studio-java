param(
    [string]$Version = "0.1.0",
    [string]$Type = "exe"
)

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path "$PSScriptRoot\..\..\.."
Set-Location $RepoRoot

$AppName = "nlp-studio-cli"
$Vendor = "Titiplex"
$MainClass = "org.titiplex.Main"
$JarName = "nlp-studio-core-$Version-all.jar"

Write-Host "==> Building CLI with Maven"
mvn -pl core -am clean package

$InputDir = Join-Path $RepoRoot "core\target"
$DestDir = Join-Path $RepoRoot "core\target\installer"
New-Item -ItemType Directory -Force -Path $DestDir | Out-Null

$JarPath = Join-Path $InputDir $JarName
if (-not (Test-Path $JarPath))
{
    throw "Jar not found: $JarPath"
}

$IconPath = Join-Path $RepoRoot "packaging\resources\cli\nlp-studio-cli.ico"
$HasIcon = Test-Path $IconPath

$jpackageArgs = @(
    "--type", $Type,
    "--name", $AppName,
    "--app-version", $Version,
    "--input", $InputDir,
    "--main-jar", $JarName,
    "--main-class", $MainClass,
    "--dest", $DestDir,
    "--vendor", $Vendor,
    "--description", "NLP Studio command line tools",
    "--win-console"
)

if ($HasIcon)
{
    $jpackageArgs += @("--icon", $IconPath)
}

Write-Host "==> Packaging CLI with jpackage"
& jpackage @jpackageArgs

Write-Host "==> Done. Installer(s) available in $DestDir"