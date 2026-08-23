# 🛠️ INC-001 — Absence de fournisseur Compose sous environnement Podman / Fedora

| Métadonnée | Valeur |
| :--- | :--- |
| **Identifiant** | `INC-001` |
| **Date** | 2026-08-23 |
| **Composant** | DevOps / Scripts Shell / Podman / Docker |
| **Sévérité** | Bloquant (Empêchait le démarrage de la base de données) |
| **Statut** | 🟢 Résolu |

---

## 💥 1. Symptôme & Message d'erreur

Lors de l'exécution du script de démarrage de la base de données `./start-db.sh` ou d'une commande `docker compose` / `podman compose`, l'erreur suivante est survenue :

```text
Error: looking up compose provider failed
7 errors occurred:
        * exec: "/home/trollgun/.docker/cli-plugins/docker-compose": stat /home/trollgun/.docker/cli-plugins/docker-compose: no such file or directory
        * exec: "/usr/local/lib/docker/cli-plugins/docker-compose": stat /usr/local/lib/docker/cli-plugins/docker-compose: no such file or directory
        * exec: "/usr/local/libexec/docker/cli-plugins/docker-compose": stat /usr/local/libexec/docker/cli-plugins/docker-compose: no such file or directory
        * exec: "/usr/lib/docker/cli-plugins/docker-compose": stat /usr/lib/docker/cli-plugins/docker-compose: no such file or directory
        * exec: "/usr/libexec/docker/cli-plugins/docker-compose": stat /usr/libexec/docker/cli-plugins/docker-compose: no such file or directory
        * exec: "docker-compose": executable file not found in $PATH
        * exec: "podman-compose": executable file not found in $PATH
```

---

## 🔍 2. Analyse de la Cause Racine (Root Cause)

- L'environnement hôte (Fedora / distribution avec Podman natif) ne dispose pas des binaires autonomes `docker-compose` ou `podman-compose`, ni de plugins `compose` dans les chemins CLI Docker standards.
- La commande `podman compose` n'est qu'un passe-plat (wrapper) cherchant un exécutable tiers pour interpréter les fichiers `docker-compose.yml`. En son absence, toute invocation de compose échoue.
- Cependant, le moteur de container `podman` est nativement installé (`/usr/bin/podman`) et capable de gérer directement les containers, réseaux et volumes rootless sans compose.

---

## 🛠️ 3. Solution Technique Apportée

1. **Scripts shell autonomes et agnostiques du moteur :**
   - Réécriture complète de [`start-db.sh`](file:///var/home/trollgun/IdeaProjects/Optimiam/start-db.sh) et [`stop-db.sh`](file:///var/home/trollgun/IdeaProjects/Optimiam/stop-db.sh) pour détecter dynamiquement `podman` ou `docker` et exécuter directement les commandes natives de l'outil (`podman run`, `podman start`, `podman stop`, `podman exec`).
2. **Cycle de vie du container `optimiam-postgres` :**
   - Vérification de l'existence du container avant création (`$ENGINE ps -a`).
   - Redémarrage propre s'il est déjà créé mais arrêté (`$ENGINE start`).
   - Montage du volume persistant nommé `optimiam_postgres_data`.
3. **Vérification active de disponibilité (Healthcheck) :**
   - Boucle d'attente active avec `$ENGINE exec optimiam-postgres pg_isready -U optimiam -d optimiam` avant de rendre la main à l'utilisateur.

---

## 📁 4. Fichiers Modifiés

- [`start-db.sh`](file:///var/home/trollgun/IdeaProjects/Optimiam/start-db.sh)
- [`stop-db.sh`](file:///var/home/trollgun/IdeaProjects/Optimiam/stop-db.sh)
- [`README.md`](file:///var/home/trollgun/IdeaProjects/Optimiam/README.md)

---

## ✅ 5. Validation

- `./start-db.sh` démarre le container `optimiam-postgres` et valide la connexion en ~2 secondes.
- `./stop-db.sh` stoppe le container proprement.
- Le cycle arrêt/redémarrage a été testé avec succès.
