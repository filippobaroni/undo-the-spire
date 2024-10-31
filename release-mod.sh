#!/usr/bin/env bash

#!/bin/bash

# Define variables
SLAY_THE_SPIRE_PATH="$HOME/.local/share/Steam/steamapps/common/SlayTheSpire/"
MOD_DIR="undothespire"
UNDO_FILE="src/main/java/undobutton/UndoButtonMod.java"
JAVA_COMMAND="/home/delfad0r/.jdks/liberica-1.8.0_422/bin/java -Dmaven.multiModuleProjectDirectory=/home/delfad0r/Documents/programming/undo-the-spire -Dfile.encoding=UTF-8 -Dmaven.home=/usr/share/idea/plugins/maven/lib/maven3 -Dclassworlds.conf=/usr/share/idea/plugins/maven/lib/maven3/bin/m2.conf -classpath /usr/share/idea/plugins/maven/lib/maven3/boot/plexus-classworlds-2.8.0.jar org.codehaus.classworlds.Launcher package -P linux"
TARGET_JAR="./target/undothespire.jar"
CONFIG_FILE="$MOD_DIR/config.json"
MOD_UPLOADER="java -jar mod-uploader.jar upload -w $MOD_DIR"

# Exit script on error
set -e

# Step 1: Modify DEBUG flag to false
sed -i 's/public static final boolean DEBUG = true;/public static final boolean DEBUG = false;/g' "$UNDO_FILE"

# Step 2: Run Maven command
if ! $JAVA_COMMAND; then
  # Reset DEBUG flag to true if Maven command fails
  sed -i 's/public static final boolean DEBUG = false;/public static final boolean DEBUG = true;/g' "$UNDO_FILE"
  exit 1
fi

sed -i 's/public static final boolean DEBUG = false;/public static final boolean DEBUG = true;/g' "$UNDO_FILE"

# Step 3.1: Ask the user if they want to upload to Steam
read -rp "Do you want to upload to Steam? (y/n): " UPLOAD_CONFIRMATION
if [[ "$UPLOAD_CONFIRMATION" != "y" ]]; then
  echo "Upload to Steam cancelled."
  exit 0
fi


# Step 4: Ask the user for a changelog
read -rp "Enter a short changelog: " CHANGELOG

# Step 5: Copy the jar to the Slay the Spire mod directory
cp "$TARGET_JAR" "$SLAY_THE_SPIRE_PATH/$MOD_DIR/content/undothespire.jar"

# Step 6: Change directory to Slay the Spire path
cd "$SLAY_THE_SPIRE_PATH"

# Step 7: Update the changelog in config.json
jq ".changeNote = \"$CHANGELOG\"" "$CONFIG_FILE" > tmp.$$.json && mv tmp.$$.json "$CONFIG_FILE"

# Step 8: Output summary and ask for confirmation
echo "Summary:"
echo "Copied $TARGET_JAR to $SLAY_THE_SPIRE_PATH/$MOD_DIR/content/undothespire.jar"
echo "Updated changeNote in $CONFIG_FILE to: $CHANGELOG"
read -rp "Do you want to proceed with uploading? (y/n): " CONFIRMATION

# Step 9: Run mod uploader if confirmed
if [[ "$CONFIRMATION" == "y" ]]; then
  $MOD_UPLOADER
else
  echo "Upload cancelled."
fi
