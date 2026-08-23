#!/usr/bin/env bash
set -e

# Couleurs pour l'affichage
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

CONTAINER_NAME="optimiam-postgres"
VOLUME_NAME="optimiam_postgres_data"
IMAGE_NAME="docker.io/library/postgres:16-alpine"

echo -e "${BLUE}=== 🐘 Démarrage du container PostgreSQL OptiMiam ===${NC}"

# Détection de l'exécutable container (podman ou docker)
if command -v podman &> /dev/null; then
    ENGINE="podman"
elif command -v docker &> /dev/null; then
    ENGINE="docker"
else
    echo -e "${RED}❌ Erreur : ni 'podman' ni 'docker' n'ont été trouvés dans le PATH.${NC}"
    exit 1
fi

echo -e "${YELLOW}Moteur de container détecté : ${ENGINE}${NC}"

# Vérifier si le container existe déjà
if $ENGINE ps -a --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    # Vérifier s'il est déjà démarré
    if $ENGINE ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "${GREEN}Le container '${CONTAINER_NAME}' est déjà en cours d'exécution.${NC}"
    else
        echo -e "${YELLOW}Redémarrage du container existant '${CONTAINER_NAME}'...${NC}"
        $ENGINE start "$CONTAINER_NAME"
    fi
else
    echo -e "${YELLOW}Création et lancement du container '${CONTAINER_NAME}'...${NC}"
    $ENGINE run -d \
        --name "$CONTAINER_NAME" \
        -p 5432:5432 \
        -e POSTGRES_DB=optimiam \
        -e POSTGRES_USER=optimiam \
        -e POSTGRES_PASSWORD=optimiam_password \
        -v "$VOLUME_NAME":/var/lib/postgresql/data \
        "$IMAGE_NAME"
fi

echo -e "${YELLOW}Attente de la disponibilité de la base de données...${NC}"
max_attempts=30
attempt=0

until [ $attempt -ge $max_attempts ]
do
    if $ENGINE exec "$CONTAINER_NAME" pg_isready -U optimiam -d optimiam > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PostgreSQL est prêt et accepte les connexions !${NC}"
        echo ""
        echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "  🌐 Hôte     : ${BLUE}localhost:5432${NC}"
        echo -e "  📦 Base     : ${BLUE}optimiam${NC}"
        echo -e "  👤 User     : ${BLUE}optimiam${NC}"
        echo -e "  🔑 Password : ${BLUE}optimiam_password${NC}"
        echo -e "  🌱 Profil   : ${GREEN}dev (Spring Boot)${NC}"
        echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo ""
        echo -e "Pour démarrer le backend avec ce profil :"
        echo -e "  ${YELLOW}cd backend && ./mvnw spring-boot:run${NC}"
        exit 0
    fi
    attempt=$((attempt+1))
    sleep 1
done

echo -e "${RED}⚠️ Timeout : PostgreSQL n'a pas répondu dans le délai imparti.${NC}"
exit 1
