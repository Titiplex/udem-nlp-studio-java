@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
  echo Usage: scripts\release-desktop.bat 0.1.0
  exit /b 1
)

set VERSION=%~1
set TAG=v%VERSION%

git rev-parse --is-inside-work-tree >nul 2>nul
if errorlevel 1 (
  echo Not inside a git repository.
  exit /b 1
)

git diff --quiet
if errorlevel 1 (
  echo Working tree has unstaged changes.
  exit /b 1
)

git diff --cached --quiet
if errorlevel 1 (
  echo Index has staged but uncommitted changes.
  exit /b 1
)

git fetch --tags
git rev-parse "%TAG%" >nul 2>nul
if not errorlevel 1 (
  echo Tag %TAG% already exists locally.
  exit /b 1
)

git ls-remote --tags origin %TAG% | findstr /C:"refs/tags/%TAG%" >nul
if not errorlevel 1 (
  echo Tag %TAG% already exists on origin.
  exit /b 1
)

echo Creating tag %TAG%...
git tag %TAG%
if errorlevel 1 exit /b 1

echo Pushing tag %TAG%...
git push origin %TAG%
if errorlevel 1 exit /b 1

echo.
echo Release triggered successfully for %TAG%.
echo Check GitHub Actions / Releases for progress.
endlocal