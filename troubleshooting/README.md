# 🛠️ Dossier Troubleshooting & Post-Mortems — OptiMiam

> Ce répertoire contient les fiches détaillées de chaque incident / bug rencontré sur le projet **OptiMiam**, comprenant le symptôme, la cause racine, les modifications de code et les preuves de validation.

---

## 📋 Registre des Incidents

| ID | Date | Composant | Titre du Bug | Statut | Fiche détaillée |
| :---: | :---: | :---: | :--- | :---: | :---: |
| **INC-001** | 2026-08-23 | DevOps / Podman | Erreur de recherche du compose provider sous Fedora / Podman | 🟢 Résolu | [📄 Voir la fiche](file:///var/home/trollgun/IdeaProjects/Optimiam/troubleshooting/INC-001-podman-compose-provider-missing.md) |
| **INC-002** | 2026-08-23 | JPA / PostgreSQL | Erreur PostgreSQL `function lower(bytea) does not exist` | 🟢 Résolu | [📄 Voir la fiche](file:///var/home/trollgun/IdeaProjects/Optimiam/troubleshooting/INC-002-postgresql-lower-bytea-error.md) |

---

## 📝 Structure Standard d'une Fiche de Bug

Pour tout nouveau problème, un fichier `INC-XXX-<description-courte>.md` est créé dans ce dossier en respectant le canevas suivant :

1. **Métadonnées :** ID, Date, Composant, Sévérité, Statut.
2. **Symptôme & Message d'erreur :** Stack trace ou comportement anormal observé.
3. **Analyse de la Cause Racine (Root Cause) :** Explication technique détaillée de l'origine du problème.
4. **Solution Technique Apportée :** Corrections architecturales et algorithmiques mises en œuvre.
5. **Fichiers Modifiés :** Liste des fichiers touchés.
6. **Validation :** Tests manuels et automatisés attestant de la résolution.
