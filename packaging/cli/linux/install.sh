#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-0.1.0}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
APP_IMAGE_DIR="$REPO_ROOT/core/target/installer/nlpstudio"
INSTALL_DIR="${HOME}/.local/opt/nlpstudio"
BIN_DIR="${HOME}/.local/bin"
LAUNCHER_PATH="$BIN_DIR/nlpstudio"

if [[ ! -d "$APP_IMAGE_DIR" ]]; then
  echo "App image not found: $APP_IMAGE_DIR" >&2
  echo "Run packaging/cli/linux/build.sh first." >&2
  exit 1
fi

mkdir -p "$BIN_DIR"
rm -rf "$INSTALL_DIR"
cp -R "$APP_IMAGE_DIR" "$INSTALL_DIR"

cat > "$LAUNCHER_PATH" <<EOF
#!/usr/bin/env bash
exec "$INSTALL_DIR/bin/nlpstudio" "\$@"
EOF

chmod +x "$LAUNCHER_PATH"

echo "Installed to $INSTALL_DIR"
echo "Launcher created at $LAUNCHER_PATH"

path_contains_bin_dir() {
  case ":$PATH:" in
    *":$BIN_DIR:"*) return 0 ;;
    *) return 1 ;;
  esac
}

detect_shell_profile() {
  local shell_name
  shell_name="$(basename "${SHELL:-}")"

  case "$shell_name" in
    bash)
      if [[ -f "$HOME/.bashrc" ]]; then
        echo "$HOME/.bashrc"
      else
        echo "$HOME/.profile"
      fi
      ;;
    zsh)
      echo "$HOME/.zshrc"
      ;;
    fish)
      echo "$HOME/.config/fish/config.fish"
      ;;
    *)
      if [[ -f "$HOME/.profile" ]]; then
        echo "$HOME/.profile"
      else
        echo "$HOME/.bashrc"
      fi
      ;;
  esac
}

append_path_to_profile() {
  local profile_file="$1"

  mkdir -p "$(dirname "$profile_file")"
  touch "$profile_file"

  if grep -Fq "$BIN_DIR" "$profile_file"; then
    echo "PATH entry already present in $profile_file"
    return 0
  fi

  if [[ "$profile_file" == *"/config.fish" ]]; then
    {
      echo
      echo "# Added by NLP Studio CLI installer"
      echo "if not contains \$HOME/.local/bin \$PATH"
      echo "    set -gx PATH \$HOME/.local/bin \$PATH"
      echo "end"
    } >> "$profile_file"
  else
    {
      echo
      echo "# Added by NLP Studio CLI installer"
      echo 'export PATH="$HOME/.local/bin:$PATH"'
    } >> "$profile_file"
  fi

  echo "Added PATH entry to $profile_file"
}

if path_contains_bin_dir; then
  echo "Current shell already has $BIN_DIR in PATH"
else
  PROFILE_FILE="$(detect_shell_profile)"
  echo
  echo "$BIN_DIR is not currently in PATH."
  echo "Detected shell profile: $PROFILE_FILE"
  read -r -p "Do you want to add $BIN_DIR to your PATH automatically? [Y/n] " REPLY
  REPLY="${REPLY:-Y}"

  case "$REPLY" in
    [Yy]|[Yy][Ee][Ss])
      append_path_to_profile "$PROFILE_FILE"
      echo
      echo "Open a new terminal, or run:"
      echo "source $PROFILE_FILE"
      ;;
    *)
      echo
      echo "You can add it manually later with:"
      echo 'export PATH="$HOME/.local/bin:$PATH"'
      ;;
  esac
fi