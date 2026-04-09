#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
APP_IMAGE_DIR="$REPO_ROOT/core/target/installer/nlp-studio-cli"
INSTALL_DIR="${HOME}/Applications/nlp-studio-cli"
BIN_DIR="${HOME}/.local/bin"

if [[ ! -d "$APP_IMAGE_DIR" ]]; then
  echo "App image not found: $APP_IMAGE_DIR" >&2
  echo "Run packaging/cli/macos/build.sh first." >&2
  exit 1
fi

mkdir -p "$BIN_DIR"

rm -rf "$INSTALL_DIR"
cp -R "$APP_IMAGE_DIR" "$INSTALL_DIR"

cat > "$BIN_DIR/nlp-studio-cli" <<EOF
#!/usr/bin/env bash
exec "$INSTALL_DIR/bin/nlp-studio-cli" "\$@"
EOF

chmod +x "$BIN_DIR/nlp-studio-cli"

echo "Installed to $INSTALL_DIR"
echo "Launcher created at $BIN_DIR/nlp-studio-cli"

case ":$PATH:" in
  *":$BIN_DIR:"*) ;;
  *)
    echo
    echo "Add this to your shell profile if needed:"
    echo 'export PATH="$HOME/.local/bin:$PATH"'
    ;;
esac