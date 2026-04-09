#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-0.1.0}"
TYPE="${2:-app-image}"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$REPO_ROOT"

APP_NAME="nlpstudio"
MAIN_CLASS="org.titiplex.Main"

TARGET_DIR="$REPO_ROOT/core/target"
INPUT_DIR="$TARGET_DIR/jpackage-input-linux"
DEST_DIR="$TARGET_DIR/installer"
TEMP_DIR="$TARGET_DIR/jpackage-temp-linux"

echo "==> Building CLI with Maven"
mvn -pl core -am clean package

rm -rf "$INPUT_DIR" "$DEST_DIR" "$TEMP_DIR"
mkdir -p "$INPUT_DIR" "$DEST_DIR" "$TEMP_DIR"

JAR_PATH="$(find "$TARGET_DIR" -maxdepth 1 -type f -name 'nlp-studio-core-*-all.jar' | sort | tail -n 1)"
if [[ -z "$JAR_PATH" ]]; then
  echo "Shaded CLI jar not found in $TARGET_DIR" >&2
  exit 1
fi

JAR_NAME="$(basename "$JAR_PATH")"
cp "$JAR_PATH" "$INPUT_DIR/$JAR_NAME"

echo "==> Using shaded CLI jar: $JAR_NAME"

PRIMARY_ICON_PATH="$REPO_ROOT/packaging/resources/cli/nlpstudio.png"
LEGACY_ICON_PATH="$REPO_ROOT/packaging/resources/cli/nlp-studio-cli.png"
ICON_PATH=""
if [[ -f "$PRIMARY_ICON_PATH" ]]; then
  ICON_PATH="$PRIMARY_ICON_PATH"
elif [[ -f "$LEGACY_ICON_PATH" ]]; then
  ICON_PATH="$LEGACY_ICON_PATH"
fi

JPACKAGE_ARGS=(
  --type "$TYPE"
  --name "$APP_NAME"
  --app-version "$VERSION"
  --input "$INPUT_DIR"
  --main-jar "$JAR_NAME"
  --main-class "$MAIN_CLASS"
  --dest "$DEST_DIR"
  --temp "$TEMP_DIR"
  --vendor "Titiplex"
  --description "NLP Studio command line tools"
  --verbose
)

if [[ -n "$ICON_PATH" ]]; then
  JPACKAGE_ARGS+=(--icon "$ICON_PATH")
fi

echo "==> Packaging CLI with jpackage"
jpackage "${JPACKAGE_ARGS[@]}"

echo "==> Done. Artifact(s) available in $DEST_DIR"