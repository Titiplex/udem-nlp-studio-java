param(
    [string]$Version = "0.1.0",
    [string]$Type = "msi"
)

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path "$PSScriptRoot\..\..\.."
Set-Location $RepoRoot

$AppName = "NLP Studio"
$Vendor = "Titiplex"
$MainClass = "org.titiplex.app.DesktopApp"
$JarName = "nlp-studio-app-$Version-all.jar"

Write-Host "==> Building desktop app with Maven"
mvn -pl app -am -Pdesktop-prod clean package

$InputDir = Join-Path $RepoRoot "app\target"
$DestDir = Join-Path $RepoRoot "app\target\installer"
New-Item -ItemType Directory -Force -Path $DestDir | Out-Null

$JarPath = Join-Path $InputDir $JarName
if (-not (Test-Path $JarPath))
{
    throw "Jar not found: $JarPath"
}

$IconPath = Join-Path $RepoRoot "packaging\resources\app\nlp-studio.ico"
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
    "--description", "NLP studio for low resource languages",
    "--win-menu",
    "--win-shortcut",
    "--win-dir-chooser"
)

if ($HasIcon)
{
    $jpackageArgs += @("--icon", $IconPath)
}

Write-Host "==> Packaging desktop app with jpackage"
& jpackage @jpackageArgs

Write-Host "==> Done. Installer(s) available in $DestDir"