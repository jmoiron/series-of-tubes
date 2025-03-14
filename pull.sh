#!/usr/bin/env bash

VERSION="0.5.1"
WINUSER="jlmoi"

WINPATH="/mnt/c/users/$WINUSER/AppData/Roaming"
LINPATH="$HOME/.local/share"

UIPATH="instances/quantum-skies-$VERSION/minecraft/ldlib/assets/ldlib/projects/ui"

if [ -d "$WINPATH/PrismLauncher" ]; then
    PULL_PATH="$WINPATH/PrismLauncher/$UIPATH"
elif [ -d "$LINPATH/PrismLauncher" ]; then
    PULL_PATH="$LINPATH/PrismLauncher/$UIPATH"
else
    echo "Cannot find PrismLauncher path."
    exit 1
fi

#INSTALL_PATH="/mnt/c/Users/$WINUSER/AppData/Roaming/PrismLauncher/instances/quantum-skies-$VERSION/minecraft/mods/"
# CHUBES_VERSION="0.0.1"

cp "$PULL_PATH/"*.ui "src/main/resources/assets/chubes/ui/"

# cp "build/libs/chubes-$CHUBES_VERSION.jar" "$INSTALL_PATH"
