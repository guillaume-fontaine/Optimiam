#!/usr/bin/env bash
set -e

echo "=== 🔨 Build du projet OptiMiam ==="

# Détection JAVA_HOME si non défini
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/var/home/trollgun/.jdks/temurin-24.0.2" ]; then
        export JAVA_HOME="/var/home/trollgun/.jdks/temurin-24.0.2"
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

echo "--- 1. Compilation Backend (Spring Boot) ---"
cd backend
./mvnw clean package -DskipTests
cd ..

echo "--- 2. Build Frontend (Angular) ---"
cd frontend
npm run build
cd ..

echo "=== ✅ Build terminé avec succès ! ==="
