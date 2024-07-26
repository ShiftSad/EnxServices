#!/usr/bin/env sh

# Set default port if not provided by environment variable
PORT=${MC_PORT:-25565}

echo "Using port: $PORT"

PROJECT="paper"
MINECRAFT_VERSION="1.21"
LATEST_BUILD=$(curl -s https://api.papermc.io/v2/projects/${PROJECT}/versions/${MINECRAFT_VERSION}/builds | jq -r '.builds | map(select(.channel == "default") | .build) | .[-1]')

if [ "$LATEST_BUILD" != "null" ]; then

  JAR_NAME=${PROJECT}-${MINECRAFT_VERSION}-${LATEST_BUILD}.jar
  PAPERMC_URL="https://api.papermc.io/v2/projects/${PROJECT}/versions/${MINECRAFT_VERSION}/builds/${LATEST_BUILD}/downloads/${JAR_NAME}"

  # Download the latest Paper version
  echo "Downloading PaperMC..."
  curl -o server.jar $PAPERMC_URL
  echo "Download completed."

else
  echo "No stable build for version $MINECRAFT_VERSION found :("
  exit 1
fi

# Check if EULA environment variable is set to "true"
if [ "$EULA" = "true" ]; then
  echo "eula=true" > eula.txt
  echo "EULA has been accepted."
else
  echo "EULA not accepted. Please set EULA=true to accept the Minecraft EULA."
  exit 1
fi

mkdir plugins
curl -o plugins/Homes.jar https://file.garden/ZoTRYFZJg1bmA4WJ/Homes.jar
curl -o plugins/WindCharge.jar https://file.garden/ZoTRYFZJg1bmA4WJ/WindCharge.jar
curl -o plugins/CommandAPI.jar https://file.garden/ZoTRYFZJg1bmA4WJ/CommandAPI-9.5.1.jar
curl -o plugins/Luckperms.jar https://file.garden/ZoTRYFZJg1bmA4WJ/LuckPerms-Bukkit-5.4.137.jar

# Start the Minecraft server
java -Xms1G -Xmx1G -jar server.jar nogui --port $PORT
