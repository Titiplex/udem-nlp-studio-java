@echo off
setlocal

set APP_NAME=NLP-Studio
set APP_VERSION=0.1.0
set APP_DIR=app\target
set INPUT_DIR=%APP_DIR%\jpackage\input
set MAIN_JAR=nlp-studio-app-%APP_VERSION%-all.jar
set MAIN_CLASS=org.titiplex.app.DesktopApp
set ICON_FILE=app\src\main\packaging\icon.ico

if not exist "%INPUT_DIR%" (
  echo Input directory not found: %INPUT_DIR%
  exit /b 1
)

if not exist "%INPUT_DIR%\%MAIN_JAR%" (
  echo Main jar not found: %INPUT_DIR%\%MAIN_JAR%
  exit /b 1
)

jpackage ^
  --type exe ^
  --name %APP_NAME% ^
  --app-version %APP_VERSION% ^
  --input "%INPUT_DIR%" ^
  --main-jar "%MAIN_JAR%" ^
  --main-class %MAIN_CLASS% ^
  --dest "%APP_DIR%\installer" ^
  --icon "%ICON_FILE%" ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut ^
  --verbose

if errorlevel 1 (
  echo jpackage failed
  exit /b 1
)

echo Installer generated in %APP_DIR%\installer
endlocal