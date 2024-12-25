FROM maven:3.8.5-openjdk-17-slim

# Installer Docker CLI
USER root
RUN apt-get update && apt-get install -y docker.io && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Configurer le retour à l'utilisateur Jenkins
USER jenkins
