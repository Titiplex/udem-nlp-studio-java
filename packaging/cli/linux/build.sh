#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-0.1.0}"
TYPE="${2:-app-image}"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$REPO_ROOT"

APP_NAME="nlp-studio-cli"
MAIN_CLASS="org.titiplex.Main"
JAR_NAME="nlp-studio-core-${VERSION}-all.jar"
INPUT_DIR="$REPO_ROOT/core/target"
DEST_DIR="$REPO_ROOT/core/target/installer"

echo "==> Building CLI with Maven"
mvn -pl core -am clean package

mkdir -p "$DEST_DIR"

if [[ ! -f "$INPUT_DIR/$JAR_NAME" ]]; then
  echo "Jar not found: $INPUT_DIR/$JAR_NAME" >&2
  exit 1
fi

ICON_PATH="$REPO_ROOT/packaging/resources/cli/nlp-studio-cli.png"

JPACKAGE_ARGS=(
  --type "$TYPE"
  --name "$APP_NAME"
  --app-version "$VERSION"
  --input "$INPUT_DIR"
  --main-jar "$JAR_NAME"
  --main-class "$MAIN_CLASS"
  --dest "$DEST_DIR"
  --vendor "Titiplex"
  --description "NLP Studio command line tools"
)

if [[ -f "$ICON_PATH" ]]; then
  JPACKAGE_ARGS+=(--icon "$ICON_PATH")
fi

echo "==> Packaging CLI with jpackage"
jpackage "${JPACKAGE_ARGS[@]}"

echo "==> Done. Artifact(s) available in $DEST_DIR"