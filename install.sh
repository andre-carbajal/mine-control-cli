#!/bin/bash

set -e

REPO="andre-carbajal/mine-control-cli"
LATEST=$(curl -s "https://api.github.com/repos/$REPO/releases/latest")
OS=$(uname -s)
ARCH=$(uname -m)

if [[ "$OS" == "Darwin" ]]; then
  if [[ "$ARCH" == "arm64" ]]; then
    ASSET="mine-control-cli-macos-arm64.zip"
  else
    ASSET="mine-control-cli-macos-x64.zip"
  fi
elif [[ "$OS" == "Linux" ]]; then
  ASSET="mine-control-cli-linux.zip"
else
  echo "Unsupported operating system"
  exit 1
fi

VERSION=$(echo "$LATEST" | grep -o '"tag_name": "[^"]*"' | sed 's/"tag_name": "//; s/"//')
DOWNLOAD_URL="https://github.com/$REPO/releases/download/$VERSION/$ASSET"

# Check if mine-control-cli is already installed
if command -v mine-control-cli >/dev/null 2>&1; then
  echo "mine-control-cli is already installed. Upgrading to the latest version..."
  sudo rm -f /usr/local/bin/mine-control-cli
fi

curl -L "$DOWNLOAD_URL" -o "mine-control-cli.zip"
unzip -q "mine-control-cli.zip" -d "mine-control-cli"
chmod +x "mine-control-cli/mine-control-cli"
sudo mv "mine-control-cli/mine-control-cli" /usr/local/bin/mine-control-cli
rm -rf "mine-control-cli" "mine-control-cli.zip"

# Add to PATH if not present
if ! echo "$PATH" | grep -q "/usr/local/bin"; then
  SHELL_NAME=$(basename "$SHELL")
  if [[ "$SHELL_NAME" == "zsh" ]]; then
    PROFILE="$HOME/.zshrc"
  else
    PROFILE="$HOME/.bashrc"
  fi
  echo "export PATH=\"/usr/local/bin:$PATH\"" >> "$PROFILE"
  echo "/usr/local/bin was added to your PATH in $PROFILE. Please restart your terminal or run: source $PROFILE"
fi

echo "mine-control-cli installed successfully"
