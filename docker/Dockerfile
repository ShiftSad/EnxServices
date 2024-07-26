# Use Alpine as the base image
FROM alpine:latest

# Install necessary packages
RUN \
  apk add --no-cache openjdk21-jdk curl jq bash

# Set working directory
WORKDIR /minecraft

# Copy the startup script into the container
COPY start.sh /start.sh

# Make the script executable
RUN chmod +x /start.sh

# Expose the port for the Minecraft server
EXPOSE 25565

# Set the default command to run the script
CMD ["/start.sh"]
