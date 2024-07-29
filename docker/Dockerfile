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

ENV MYSQL_USERNAME=shiftsad
ENV MYSQL_PASSWORD=banana
ENV MYSQL_DATABASE=database
ENV MYSQL_HOST=localhost
ENV MYSQL_PORT=3306
ENV MYSQL_MAXIMUM_POOL_SIZE=5
ENV MYSQL_MAX_LIFE_TIME=60000
ENV MYSQL_KEEPALIVE_TIME=30000
ENV MYSQL_CONNECTION_TIMEOUT=10000
ENV MYSQL_LEAK_DETECTION_THRESHOLD=2000

# Set the default command to run the script
CMD ["/start.sh"]
