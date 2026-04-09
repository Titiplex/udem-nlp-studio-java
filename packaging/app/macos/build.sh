#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-0.1.0}"
TYPE="${2:-dmg}"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$REPO_ROOT"

APP_NAME="NLP Studio"
MAIN_CLASS="org.titiplex.app.DesktopApp"
INPUT_DIR="$REPO_ROOT/app/target"
DEST_DIR="$REPO_ROOT/app/target/installer"

echo "==> Building desktop app with Maven"
mvn -pl app -am -Pdesktop-prod clean package

mkdir -p "$DEST_DIR"

JAR_PATH="$(find "$INPUT_DIR" -maxdepth 1 -type f -name 'nlp-studio-app-*-all.jar' | sort | tail -n 1)"
if [[ -z "$JAR_PATH" ]]; then
  echo "Shaded desktop app jar not found in $INPUT_DIR" >&2
  exit 1
fi
JAR_NAME="$(basename "$JAR_PATH")"

echo "==> Using shaded desktop jar: $JAR_NAME"

ICON_PATH="$REPO_ROOT/packaging/resources/app/nlp-studio.icns"

JPACKAGE_ARGS=(
  --type "$TYPE"
  --name "$APP_NAME"
  --app-version "$VERSION"
  --input "$INPUT_DIR"
  --main-jar "$JAR_NAME"
  --main-class "$MAIN_CLASS"
  --dest "$DEST_DIR"
  --vendor "Titiplex"
  --description "NLP studio for low resource languages"
  --mac-package-identifier "org.titiplex.nlpstudio"
)

if [[ -f "$ICON_PATH" ]]; then
  JPACKAGE_ARGS+=(--icon "$ICON_PATH")
fi

echo "==> Packaging desktop app with jpackage"
jpackage "${JPACKAGE_ARGS[@]}"

echo "==> Done. Installer(s) available in $DEST_DIR"