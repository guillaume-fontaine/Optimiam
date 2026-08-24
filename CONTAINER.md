# Mission — Containeriser le Frontend et le Backend

Le projet est composé de :

* un **frontend Angular**
* un **backend Java Spring Boot**
* une **base de données**, déjà configurée avec un `docker-compose.yml` existant
* un repository hébergé sur **GitHub**

Je souhaite maintenant préparer le projet pour un futur déploiement automatisé sur un VPS avec GitHub Actions.

## Objectif

Mettre en place une containerisation propre et adaptée à la production pour :

1. le frontend Angular ;
2. le backend Spring Boot.

**Ne modifie pas la configuration actuelle de la base de données et ne remplace pas le `docker-compose` existant.**

La base de données sera intégrée au système de déploiement dans une étape ultérieure.

---

# 1. Analyse préalable

Avant de modifier quoi que ce soit :

* inspecte la structure complète du repository ;
* identifie le dossier du frontend ;
* identifie le dossier du backend ;
* identifie les versions utilisées :

  * Angular ;
  * Node.js ;
  * Java ;
  * Spring Boot ;
  * Maven ou Gradle ;
* identifie les commandes actuellement utilisées pour compiler et lancer le frontend ;
* identifie les commandes actuellement utilisées pour compiler et lancer le backend ;
* analyse la configuration actuelle de l'application ;
* identifie les variables d'environnement nécessaires ;
* analyse le `docker-compose` existant uniquement afin de comprendre comment la base de données est configurée.

Ne suppose pas les technologies ou les versions : récupère-les depuis les fichiers du projet.

Avant toute modification, présente-moi ton analyse et les choix techniques proposés.

---

# 2. Dockerfile du backend Spring Boot

Créer un `Dockerfile` adapté à une utilisation de production.

Contraintes :

* utiliser un **multi-stage build** lorsque cela est pertinent ;
* utiliser une image Java adaptée à la version réellement utilisée par le projet ;
* ne pas embarquer inutilement les sources ou outils de build dans l'image finale ;
* produire une image finale aussi légère que raisonnablement possible ;
* exécuter l'application avec un utilisateur non-root lorsque cela est compatible ;
* exposer uniquement le port nécessaire ;
* utiliser une configuration permettant de fournir les paramètres de l'application via des variables d'environnement ;
* ne jamais mettre de secrets directement dans le Dockerfile ;
* ajouter un `.dockerignore` adapté.

Le Dockerfile doit permettre de construire une image avec une commande du type :

```bash
docker build -t mon-projet-backend .
```

et de démarrer le conteneur avec :

```bash
docker run ...
```

---

# 3. Dockerfile du frontend Angular

Créer un `Dockerfile` adapté à une utilisation de production.

Le build Angular doit être réalisé dans une étape de compilation séparée.

L'image finale doit utiliser un serveur HTTP adapté à la distribution d'une application Angular en production.

Privilégier une architecture du type :

```text
Node.js
   ↓
npm install / npm ci
   ↓
Angular build
   ↓
serveur HTTP
   ↓
conteneur final
```

Ne pas utiliser le serveur de développement Angular (`ng serve`) dans l'image finale.

Prendre en compte correctement :

* le routing Angular ;
* le fallback vers `index.html` ;
* les assets statiques ;
* la configuration de production ;
* les variables d'environnement si le projet en utilise.

Ajouter également un `.dockerignore` adapté.

---

# 4. Images Docker

Je souhaite pouvoir construire séparément :

```text
frontend
backend
```

Les images doivent être conçues pour être publiées ultérieurement dans un registry Docker.

Le futur objectif est de pouvoir obtenir des images similaires à :

```text
ghcr.io/<organisation-ou-utilisateur>/<projet>-frontend:<tag>
ghcr.io/<organisation-ou-utilisateur>/<projet>-backend:<tag>
```

Le registry cible sera **GitHub Container Registry (GHCR)**.

Ne mets pas encore en place le pipeline GitHub Actions complet sauf si cela est nécessaire pour tester la construction des images.

---

# 5. Gestion des variables d'environnement

Analyse la configuration actuelle du frontend et du backend.

Sépare clairement :

### Configuration de build

Variables nécessaires uniquement lors du build.

### Configuration d'exécution

Variables nécessaires lorsque le conteneur est lancé.

### Secrets

Mot de passe, tokens, clés, etc.

Aucun secret ne doit être :

* écrit dans un Dockerfile ;
* commit dans Git ;
* intégré directement dans une image Docker.

Explique également les éventuelles limitations d'Angular concernant les variables d'environnement injectées au runtime.

---

# 6. Santé des conteneurs

Lorsque cela est pertinent, prévoir un mécanisme de healthcheck.

Pour le backend Spring Boot, vérifier si Spring Boot Actuator est présent.

S'il est présent, utiliser son endpoint de santé.

S'il n'est pas présent, ne l'ajoute pas automatiquement sans me l'expliquer.

Pour le frontend, proposer une solution simple permettant de vérifier que le serveur HTTP fonctionne.

---

# 7. Sécurité

Appliquer les bonnes pratiques Docker :

* images de base officielles ou fiables ;
* versions explicites lorsque pertinent ;
* utilisateur non-root ;
* pas de secrets dans les images ;
* `.dockerignore` ;
* réduction de la surface de l'image finale ;
* aucun outil de développement inutile dans l'image finale ;
* pas de privilèges inutiles.

Explique les compromis effectués.

---

# 8. Compatibilité avec le futur CI/CD

La containerisation doit être pensée pour le futur pipeline GitHub Actions.

Le workflow cible sera :

```text
Git push
    ↓
GitHub Actions
    ↓
Tests
    ↓
Build frontend
    ↓
Build backend
    ↓
Build images Docker
    ↓
Push vers GHCR
    ↓
Déploiement VPS
```

Plus tard, nous aurons trois types d'environnement :

```text
main
   ↓
production

develop
   ↓
preproduction

feature/*
   ↓
environnement temporaire
```

Les images doivent donc pouvoir être taguées de manière fiable.

Proposer une stratégie de tags compatible avec ce fonctionnement.

Par exemple :

```text
commit SHA
branch
version
latest
```

mais ne retiens pas automatiquement cette stratégie : explique quelle stratégie serait la plus adaptée à mon projet.

---

# 9. Tests

Après création des Dockerfiles :

1. construire l'image frontend ;
2. construire l'image backend ;
3. lancer les conteneurs ;
4. vérifier qu'ils démarrent correctement ;
5. vérifier les ports ;
6. vérifier les logs ;
7. vérifier la communication avec la base de données existante lorsque cela est nécessaire ;
8. vérifier le fonctionnement du frontend avec le backend.

Ne modifie pas inutilement le code applicatif uniquement pour faire fonctionner Docker.

Si une modification du code est réellement nécessaire, explique-la avant de l'effectuer.

---

# 10. Livrables attendus

À la fin, fournir :

### Fichiers créés

Liste exacte des fichiers ajoutés.

Par exemple :

```text
frontend/Dockerfile
frontend/.dockerignore

backend/Dockerfile
backend/.dockerignore
```

Adapter cette liste à la structure réelle du projet.

### Fichiers modifiés

Liste exacte des fichiers modifiés et expliquer pourquoi.

### Commandes

Donner les commandes permettant de :

```bash
docker build
docker run
docker stop
docker rm
docker logs
```

pour le frontend et le backend.

### Tests

Donner les tests permettant de vérifier que les deux images fonctionnent correctement.

### Architecture

Présenter l'architecture finale :

```text
GitHub
   │
   ├── frontend
   │      ↓
   │   Docker image
   │
   └── backend
          ↓
       Docker image

        ↓

       GHCR

        ↓

       VPS
```

---

# Important

Ne mets pas en place Kubernetes.

Ne remplace pas le `docker-compose` actuel de la base de données.

Ne modifie pas inutilement l'architecture applicative.

Ne crée pas encore le système de déploiement dynamique des branches.

L'objectif de cette étape est uniquement :

> **Obtenir deux images Docker propres, reproductibles et prêtes à être utilisées par GitHub Actions et GHCR.**

Si plusieurs solutions sont possibles, présente-les et recommande-en une en justifiant le choix.
