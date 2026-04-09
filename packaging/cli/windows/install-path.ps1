param(
    [string]$TargetDir = "$env:LOCALAPPDATA\Programs\nlp-studio-cli"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $TargetDir))
{
    throw "Target directory not found: $TargetDir"
}

$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
$paths = @()

if ($currentPath)
{
    $paths = $currentPath.Split(';') | Where-Object { $_ -ne "" }
}

if ($paths -contains $TargetDir)
{
    Write-Host "Path already contains $TargetDir"
    exit 0
}

$newPath = ($paths + $TargetDir) -join ';'
[Environment]::SetEnvironmentVariable("Path", $newPath, "User")

Write-Host "Added to user PATH: $TargetDir"
Write-Host "Restart your terminal to use 'nlp-studio-cli'."