#!/usr/bin/env bash

VERSION="0.5.1"
WINUSER="jlmoi"

WINPATH="/mnt/c/users/$WINUSER/AppData/Roaming"
LINPATH="$HOME/.local/share"
PACKPATH="instances/quantum-skies-$VERSION/minecraft/mods"

if [ -d "$WINPATH/PrismLauncher" ]; then
    INSTALL_PATH="$WINPATH/PrismLauncher/$PACKPATH"
elif [ -d "$LINPATH/PrismLauncher" ]; then
    INSTALL_PATH="$LINPATH/PrismLauncher/$PACKPATH"
else
    echo "Cannot find PrismLauncher path."
    exit 1
fi

#INSTALL_PATH="/mnt/c/Users/$WINUSER/AppData/Roaming/PrismLauncher/instances/quantum-skies-$VERSION/minecraft/mods/"
CHUBES_VERSION="0.0.1"

cp "build/libs/chubes-$CHUBES_VERSION.jar" "$INSTALL_PATH"
