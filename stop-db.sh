#!/usr/bin/env bash
set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

CONTAINER_NAME="optimiam-postgres"

echo -e "${BLUE}=== 🛑 Arrêt du container PostgreSQL OptiMiam ===${NC}"

if command -v podman &> /dev/null; then
    ENGINE="podman"
elif command -v docker &> /dev/null; then
    ENGINE="docker"
else
    echo -e "${RED}❌ Erreur : ni 'podman' ni 'docker' n'ont été trouvés dans le PATH.${NC}"
    exit 1
fi

if $ENGINE ps -a --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    $ENGINE stop "$CONTAINER_NAME"
    echo -e "${GREEN}✅ Container '${CONTAINER_NAME}' arrêté avec succès.${NC}"
else
    echo -e "${YELLOW}Aucun container nommé '${CONTAINER_NAME}' n'est actif.${NC}"
fi
