#!/usr/bin/env bash

VERSION="0.5.1"
WINUSER="jlmoi"
INSTALL_PATH="/mnt/c/Users/$WINUSER/AppData/Roaming/PrismLauncher/instances/quantum-skies-$VERSION/minecraft/mods/"
CHUBES_VERSION="0.0.1"

cp "build/libs/chubes-$CHUBES_VERSION.jar" "$INSTALL_PATH"
