# 📋 Plan de Développement & Suivi de Projet — OptiMiam

> **Document de référence :** [`OptiMiam.md`](file:///var/home/trollgun/IdeaProjects/Optimiam/OptiMiam.md)  
> **Architecture cible :** Modular Monolith (Java Spring Boot) + PWA (Angular / Angular Material 3) + PostgreSQL + Hardware Simulé  
> **Objectif :** Suivi rigoureux de l'avancement tâche par tâche, sprint par sprint, pour garantir le respect de l'architecture et la réussite du MVP.

---

## 📌 Légende du Suivi

* `[ ]` **Non démarré**
* `[⏳]` **En cours**
* `[x]` **Terminé & validé** (code + tests)
* `[!]` **Bloqué / Nécessite clarification**

---

## 🎯 Vue d'ensemble des Jalons (Milestones)

```mermaid
gantt
    title Roadmap OptiMiam V1
    dateFormat  YYYY-MM-DD
    section Socle
    Sprint 0 : Architecture & Setup       :done,    des1, 2026-08-23, 1d
    section Cœur Métier
    Sprint 1 : Produits & Catégories     :active,  des2, 2026-08-24, 2d
    Sprint 2 : Stocks, Mouvements & Pertes:         des3, after des2, 3d
    Sprint 3 : Recettes & Nutrition       :         des4, after des3, 2d
    Sprint 4 : Moteur Recommandation      :         des5, after des4, 3d
    Sprint 5 : Planning des Repas         :         des6, after des5, 2d
    Sprint 6 : Liste de Courses & Consommation:     des7, after des6, 2d
    section Avancé & Hardware
    Sprint 7 : PWA & Sync Offline         :         des8, after des7, 3d
    Sprint 8 : Hardware Simulé (Balance/Ticket):    des9, after des8, 2d
    Sprint 9 : Sécurité, E2E & Finalisation:        des10, after des9, 2d
```

---

## 🏗️ Sprint 0 — Socle Technique, Architecture & Environnement

**Objectif :** Mettre en place le monorepo / multi-module propre, la configuration Spring Boot & Angular, Docker Compose, les conventions de code et les modules transverses.

- [x] **0.1 Structure du Projet & Configuration Globale**
  - [x] Initialiser le projet backend Spring Boot (Java 21+, Spring Boot 3.x, Maven/Gradle).
  - [x] Initialiser le projet frontend Angular (Angular 18+, standalone components, Angular Material 3).
  - [x] Configurer `docker-compose.yml` (PostgreSQL 16+, PGAdmin optionnel).
  - [x] Mettre en place les scripts de build et de lancement local.
  - [x] Définir `.editorconfig`, `.gitignore` et conventions de linting.

- [x] **0.2 Socle Backend (Transverse / Common)**
  - [x] Créer le package `common.exception` avec `GlobalExceptionHandler` et format JSON standardisé (`timestamp`, `status`, `code`, `message`, `path`).
  - [x] Définir les codes d'erreurs standards de l'application.
  - [x] Mettre en place le système d'événements de domaine applicatif (`DomainEventPublisher` / `ApplicationEventPublisher`).
  - [x] Configurer OpenAPI 3 / Swagger (`/swagger-ui.html`).
  - [x] Configurer Spring Actuator (`/actuator/health`, `/actuator/info`).
  - [x] Mettre en place la configuration de base pour `Testcontainers` (PostgreSQL pour tests d'intégration).

- [x] **0.3 Socle Frontend (Core / Shared / Layout)**
  - [x] Mettre en place la structure `src/app/{core, shared, features}`.
  - [x] Configurer Angular Material 3 avec thème personnalisé.
  - [x] Implémenter le Layout principal (Header responsive `MatToolbar`, Sidenav `MatSidenav` desktop, Bottom bar mobile).
  - [x] Créer le service HTTP de base avec gestion centralisée des erreurs (Intercepteur HTTP + `MatSnackBar`).
  - [x] Définir les composants UI partagés (dialogue de confirmation, spinners, alertes).

---

## 📦 Sprint 1 — Module Produit & Référentiel

**Objectif :** Gérer le catalogue de produits, les catégories, les unités de mesure et les durées indicatives de conservation.

- [x] **1.1 Backend — Module Produit (`com.project.nut.product`)**
  - [x] **Domain :**
    - [x] Entité `Product` (UUID, nom, code-barres, unité par défaut `KG/G/L/ML/UNIT`, catégorie, durée de conservation moyenne, timestamps, soft delete).
    - [x] Entité `Category` (UUID, nom, icône/couleur, description).
    - [x] Repositories `ProductRepository` et `CategoryRepository`.
  - [x] **Application / Use Cases :**
    - [x] `CreateProductUseCase`, `UpdateProductUseCase`, `GetProductUseCase`, `ListProductsUseCase`, `DeleteProductUseCase`.
    - [x] Gestion et validation des catégories associées.
  - [x] **API :**
    - [x] `ProductController` (`/api/v1/products`) avec pagination, recherche et filtres par catégorie.
    - [x] `CategoryController` (`/api/v1/categories`).
    - [x] DTOs de requête et réponse avec validations Bean Validation (`@NotBlank`, `@NotNull`, etc.).
  - [x] **Tests :**
    - [x] Tests unitaires des services.
    - [x] Tests d'intégration REST avec Testcontainers / MockMvc.

- [x] **1.2 Frontend — Gestion des Produits (`features/products`)**
  - [x] Service `ProductService` pour les appels API REST.
  - [x] Vue liste des produits avec recherche instantanée, filtres par catégories et pagination (`MatTable` / `MatCard`).
  - [x] Formulaire de création / édition de produit dans un dialogue Material (`MatDialog`).
  - [x] Gestion des unités et catégories dans l'interface.
  - [x] Tests unitaires des composants et du service.

---

## 🥫 Sprint 2 — Module Stock, Transactions & Pertes

**Objectif :** Gérer le stock disponible, les entrées, sorties, dates de péremption, emplacements, consommations et suivi des pertes alimentaires.

- [x] **2.1 Backend — Module Stock & Transactions (`com.project.nut.stock` & `com.project.nut.transaction`)**
  - [x] **Domain :**
    - [x] Entité `StockItem` (UUID, product_id, quantité, unité, date d'entrée, date de péremption DLC/DDM, emplacement `FRIDGE/FREEZER/PANTRY`, version optimiste, statut).
    - [x] Entité `StockTransaction` (UUID, stock_item_id, product_id, type `ENTRY/EXIT/CONSUMPTION/LOSS/ADJUSTMENT`, quantité, unité, motif, timestamp, device_id).
    - [x] Repositories `StockRepository` et `StockTransactionRepository`.
  - [x] **Application & Logique Métier Transactionnelle :**
    - [x] Service `StockEntryService` (création / ajout de quantité, émission d'événement `StockEntryCreated`).
    - [x] Service `StockExitService` (consommation, émission d'événement `StockExitCreated`).
    - [x] Service `StockLossService` (déclaration de perte, motifs `EXPIRED/SPOILED/OVERCOOKED`, calcul coût/poids perdu, émission `StockLossRecorded`).
    - [x] Logique d'alerte péremption (produits périmés, urgents `< 3 jours`, à surveiller).
  - [x] **API :**
    - [x] `StockController` (`GET /api/v1/stock`, `GET /api/v1/stock/expiring`, `POST /api/v1/stock/entries`, `POST /api/v1/stock/exits`, `POST /api/v1/stock/losses`).
    - [x] `TransactionController` (`GET /api/v1/transactions`, filtres par période et type).
    - [x] `LossStatisticsController` (`GET /api/v1/losses/stats` — agrégations par semaine/mois, top produits perdus).
  - [x] **Tests :**
    - [x] Tests unitaires des règles de mouvement de stock et vérification de la concurrence/version.
    - [x] Tests d'intégration des transactions et des rollbacks.

- [x] **2.2 Frontend — Stock, Mouvements & Alertes (`features/stock`, `features/transactions`)**
  - [x] Vue globale du Stock avec badges d'urgence colorés (vert = ok, orange = bientôt périmé, rouge = urgent).
  - [x] Filtres par emplacement (Frigo, Placard, Congélateur) et tri par date de péremption.
  - [x] Dialogues d'actions rapides :
    - [x] Entrée en stock (+ pesée / quantité / date péremption).
    - [x] Consommation partielle ou totale.
    - [x] Déclaration de perte avec motif.
  - [x] Écran Historique des transactions et journal d'audit.
  - [x] Tests des composants.

---

## 🍳 Sprint 3 — Module Recettes & Nutrition

**Objectif :** Gérer la base de recettes de cuisine, leurs ingrédients associés aux produits, étapes de préparation et valeurs nutritionnelles.

- [x] **3.1 Backend — Module Recettes & Nutrition (`com.project.nut.recipe` & `com.project.nut.nutrition`)**
  - [x] **Domain :**
    - [x] Entité `Recipe` (UUID, nom, description, temps de préparation, temps de cuisson, difficulté `EASY/MEDIUM/HARD`, portions, tags/catégories).
    - [x] Entité `RecipeIngredient` (UUID, recipe_id, product_id, quantité, unité, optionnel/obligatoire).
    - [x] Entité `RecipeStep` (UUID, recipe_id, ordre, instruction, durée).
    - [x] Entité `Nutrition` (calories, protéines, glucides, lipides, fibres, sel — liée à `Product` ou `Recipe`).
    - [x] Repositories `RecipeRepository`, `NutritionRepository`.
  - [x] **Application :**
    - [x] CRUD Recettes avec calcul automatique des valeurs nutritionnelles globales par portion.
    - [x] Recherche multi-critères (par nom, tag, temps max, ingrédients requis).
  - [x] **API :**
    - [x] `RecipeController` (`GET /api/v1/recipes`, `POST /api/v1/recipes`, `GET /api/v1/recipes/{id}`, `PUT /api/v1/recipes/{id}`).
    - [x] Endpoints de gestion des valeurs nutritionnelles.
  - [x] **Tests :**
    - [x] Tests unitaires calcul nutritionnel et intégrité recette/ingrédients.
    - [x] Tests d'intégration API.

- [x] **3.2 Frontend — Catalogue & Fiches Recettes (`features/recipes`, `features/nutrition`)**
  - [x] Vue catalogue des recettes avec cartes riches (image/icône, temps, difficulté, macros nutritionnelles).
  - [x] Fiche détaillée de la recette (liste des ingrédients avec indicateur de disponibilité en stock, étapes pas à pas, tableau nutritionnel).
  - [x] Formulaire complet de création/édition d'une recette avec ajout dynamique d'ingrédients et d'étapes.
  - [x] Tests unitaires des composants.

---

## 🧠 Sprint 4 — Moteur de Recommandation & Dashboard Intelligent

**Objectif :** Implémenter le moteur de scoring déterministe et explicable pour suggérer les meilleures recettes selon le stock et les péremptions, et alimenter le Dashboard.

- [ ] **4.1 Backend — Moteur de Recommandation V1 (`com.project.nut.recommendation`)**
  - [ ] **Domain & Scoring Strategy :**
    - [ ] Interface `RecommendationStrategy` (préparation pour le futur module ILP V2).
    - [ ] Implémentation `DeterministicScoringStrategy` :
      - [ ] `StockScore` (% d'ingrédients disponibles en stock).
      - [ ] `ExpirationScore` (bonus pour l'utilisation de produits proches de la DLC).
      - [ ] `PreferenceScore` (temps max, difficulté préférée, catégories favorites).
      - [ ] `NutritionScore` & `ComplexityScore`.
      - [ ] Formule de score pondéré configurable.
    - [ ] Modèle d'explicabilité : liste des raisons (`EXPIRING_PRODUCTS`, `STOCK_MATCH_90%`, `FAST_RECIPE`, etc.).
  - [ ] **API :**
    - [ ] `RecommendationController` (`POST /api/v1/recommendations`, `POST /api/v1/recommendations/preview`).
  - [ ] **Tests :**
    - [ ] Tests unitaires approfondis avec jeux de données de stock et dates variables.
    - [ ] Vérification du déterminisme des scores et de la pertinence des explications fournies.

- [ ] **4.2 Frontend — Dashboard & Recommandations (`features/dashboard`, `features/recommendations`)**
  - [ ] Dashboard principal V1 :
    - [ ] Compteurs KPI : Total produits en stock, Produits à consommer d'urgence, Pertes évitées / enregistrées.
    - [ ] Widget "🔴 À cuisiner en priorité" (produits proches de péremption).
    - [ ] Widget "⭐ Recettes recommandées du jour" avec score (%) et badges explicatifs ("Utilise 3 produits à péremption").
    - [ ] Raccourcis d'actions rapides ("Ajouter au stock", "Planifier ce repas").
  - [ ] Page dédiée Recommandations avec curseurs de préférences en direct.
  - [ ] Tests des composants.

---

## 📅 Sprint 5 — Planning des Repas

**Objectif :** Permettre la planification des repas (semaine/mois, midi/soir), liée aux recettes sélectionnées ou recommandées.

- [ ] **5.1 Backend — Module Planning (`com.project.nut.planning`)**
  - [ ] **Domain :**
    - [ ] Entité `MealPlan` (UUID, user_id, date, meal_type `BREAKFAST/LUNCH/DINNER/SNACK`, recipe_id, nombre de portions, statut `PLANNED/COOKED/CANCELLED`).
    - [ ] Repository `MealPlanRepository`.
  - [ ] **Application :**
    - [ ] Gestion des plannings (ajout, déplacement, suppression, validation d'un repas cuisiné).
    - [ ] Événement `MealPlanUpdated`, `MealCookedEvent`.
  - [ ] **API :**
    - [ ] `MealPlanController` (`GET /api/v1/planning?startDate=...&endDate=...`, `POST /api/v1/planning`, `PUT /api/v1/planning/{id}`, `DELETE /api/v1/planning/{id}`).
  - [ ] **Tests :**
    - [ ] Tests unitaires et intégration.

- [ ] **5.2 Frontend — Vue Calendrier / Planning Hebdomadaire (`features/planning`)**
  - [ ] Vue Planning hebdomadaire responsive (grille jours / créneaux repas midi et soir).
  - [ ] Ajout d'une recette au planning (depuis la vue planning ou directement depuis une fiche recette / recommandation).
  - [ ] Actions sur un repas planifié : Modifier portions, Remplacer recette, Supprimer, Marquer comme cuisiné.
  - [ ] Tests unitaires des composants.

---

## 🛒 Sprint 6 — Liste de Courses Automatique & Consommation

**Objectif :** Calculer automatiquement la liste de courses (Planning - Stock), permettre de cocher les articles et boucler le flux en consommant le stock.

- [ ] **6.1 Backend — Module Courses & Déduction de Stock (`com.project.nut.shopping`)**
  - [ ] **Domain :**
    - [ ] Entité `ShoppingList` (UUID, user_id, statut `ACTIVE/ARCHIVED`, dates).
    - [ ] Entité `ShoppingListItem` (UUID, shopping_list_id, product_id, quantité requise, unité, checked).
    - [ ] Repository `ShoppingListRepository`.
  - [ ] **Application :**
    - [ ] Service `ShoppingListGenerator` :
      - [ ] Agrège les ingrédients requis sur la période de planning sélectionnée.
      - [ ] Soustrait les quantités déjà disponibles en stock.
      - [ ] Génère les lignes de courses avec les quantités manquantes nettes.
    - [ ] Use case de validation des achats (transformation d'articles cochés en entrées de stock).
    - [ ] Use case de consommation de repas (`CookMealUseCase` : déduit les ingrédients de la recette du stock, génère les transactions `CONSUMPTION`).
  - [ ] **API :**
    - [ ] `ShoppingListController` (`GET /api/v1/shopping-lists`, `POST /api/v1/shopping-lists/generate`, `PUT /api/v1/shopping-lists/{id}/items/{itemId}`).
    - [ ] `POST /api/v1/planning/{id}/cook` (déclenche la consommation et la mise à jour de stock).
  - [ ] **Tests :**
    - [ ] Tests du calcul différentiel des ingrédients manquants.
    - [ ] Tests de la déduction automatique de stock.

- [ ] **6.2 Frontend — Gestion de la Liste de Courses (`features/shopping`)**
  - [ ] Vue Liste de courses avec regroupement par rayon / catégorie de produit.
  - [ ] Cases à cocher interactives (`MatCheckbox`).
  - [ ] Bouton "Générer depuis mon planning de la semaine".
  - [ ] Bouton "Valider mes achats" (ajoute automatiquement les articles cochés au stock).
  - [ ] Action "J'ai cuisiné ce repas" depuis le planning ou la recette avec confirmation des quantités déduites.
  - [ ] Tests unitaires.

---

## 📱 Sprint 7 — PWA, Mode Offline & Synchronisation

**Objectif :** Rendre l'application installable en PWA, permettre la consultation et modification du stock hors-ligne avec synchronisation et gestion des conflits à la reconnexion.

- [ ] **7.1 Frontend — Service Worker, IndexedDB & Sync Engine (`features/sync`, `core/offline`)**
  - [ ] Configurer `@angular/pwa` et le fichier `ngsw-config.json` (mise en cache des assets et shell de l'application).
  - [ ] Mettre en place la base de données locale IndexedDB (via Dexie.js ou `idb`) pour stocker en cache local : Stock, Recettes, Planning, Liste de courses.
  - [ ] Implémenter la file d'attente locale `PendingOperationsQueue` (stocke chaque opération locale avec UUID, timestamp, type, payload).
  - [ ] Créer le `SyncManager` :
    - [ ] Écoute l'état du réseau (`navigator.onLine`, événements `online/offline`).
    - [ ] Rejoue les opérations en attente lors du retour de la connexion.
    - [ ] Gère les notifications de statut de synchronisation (en ligne, hors ligne, synchronisation en cours, conflits).

- [ ] **7.2 Backend — Module Synchronisation (`com.project.nut.synchronization`)**
  - [ ] **Domain & Application :**
    - [ ] Entité `SyncOperation` pour tracer les flux et versions.
    - [ ] Gestion optimiste des versions (champs `version`, `updatedAt` sur toutes les entités synchronisables).
    - [ ] Détection des conflits (retour HTTP `409 CONFLICT` si la version locale est obsolète).
    - [ ] Stratégie de résolution V1 : *Last Write Wins* avec journalisation.
  - [ ] **API :**
    - [ ] `SyncController` (`POST /api/v1/sync/batch` pour traiter le lot d'opérations locales).
  - [ ] **Tests :**
    - [ ] Tests de rejeu d'opérations hors-ligne et de détection de conflits de versions concurrentes.

---

## ⚖️ Sprint 8 — Hardware Simulé (HAL — Hardware Abstraction Layer)

**Objectif :** Développer l'abstraction matérielle et les simulateurs (balance pour pesée des produits, imprimante de tickets de stock, scanner code-barres).

- [ ] **8.1 Backend — Abstraction Hardware (`com.project.nut.hardware`)**
  - [ ] Définir les interfaces métier pures :
    - [ ] `ScaleService` (`WeightMeasurement measure()`).
    - [ ] `PrinterService` (`PrintResult print(PrintDocument document)`).
    - [ ] `ScannerService` (`ScanResult scanBarcode()`).
  - [ ] Implémentations simulées :
    - [ ] `SimulatedScaleService` (retourne poids simulé, configurable).
    - [ ] `SimulatedPrinterService` (génère document / aperçu PDF ou log structuré).
    - [ ] `SimulatedScannerService` (simulation de lecture EAN/QR).
  - [ ] Endpoints API de simulation (`/api/v1/hardware/scales/simulated/*`, `/api/v1/hardware/printers/simulated/*`).
  - [ ] Tests unitaires et d'isolation (aucune fuite du hardware dans le domaine métier).

- [ ] **8.2 Frontend — Interface Simulateurs Hardware (`features/hardware`)**
  - [ ] Widget interactif "Balance Virtuelle" (ajustement du poids `[-] / [+]`, bouton `[PESER]` connecté directement au dialogue d'entrée en stock).
  - [ ] Dialogue "Ticket de Stock / Impression" (aperçu visuel du ticket généré avec nom produit, poids, DLC, code-barres).
  - [ ] Composant "Lecteur Code-Barres Simulé" (sélection rapide d'un code-barres de test).
  - [ ] Tests des composants.

---

## 🔒 Sprint 9 — Sécurité, Tests E2E & Finalisation

**Objectif :** Sécuriser les API avec JWT, peaufiner l'expérience utilisateur, automatiser les tests E2E du scénario de démonstration et préparer la documentation finale.

- [ ] **9.1 Sécurité & Profil Utilisateur**
  - [ ] Authentification Spring Security avec tokens JWT (`/api/v1/auth/login`, `/api/v1/auth/register`).
  - [ ] Rôles et autorisations (`USER`, `MANAGER`, `ADMIN`).
  - [ ] Module Préférences utilisateur (temps de préparation max, allergies/exclusions, objectifs nutritionnels).
  - [ ] Intercepteur JWT côté Angular et Guards d'authentification (`AuthGuard`).

- [ ] **9.2 Validation du Scénario E2E Démonstrateur (Jury)**
  - [ ] Écrire le test automatisé / guide de validation du parcours complet :
    1. Connexion utilisateur.
    2. Ajout de produits et entrées en stock (avec simulation de pesée).
    3. Déclaration de dates de péremption courtes.
    4. Consultation du Dashboard et vérification des alertes rouges.
    5. Calcul et affichage des recettes recommandées avec explications claires.
    6. Ajout de la recette au planning de la semaine.
    7. Génération de la liste de courses des ingrédients manquants.
    8. Cuisiner / consommer le repas -> déduction automatique du stock et génération de la transaction.
    9. Consultation des statistiques de pertes et de consommation.
    10. Test d'une déconnexion réseau (mode offline PWA) -> modification -> reconnexion -> sync réussie.
  - [ ] Tests E2E Playwright / Cypress.

- [ ] **9.3 Documentation & Packaging**
  - [ ] Vérifier la documentation OpenAPI / Swagger UI complète.
  - [ ] Rédiger le `README.md` avec guide d'installation en 1 commande (`docker compose up --build`).
  - [ ] Vérifier les métriques Spring Actuator et les logs structurés.

---

## 📈 Tableau de Bord d'Avancement Global

| Module / Domaine | Backend | Frontend | Tests | Statut Global |
| :--- | :---: | :---: | :---: | :---: |
| **0. Setup & Architecture** | `[x]` | `[x]` | `[x]` | 🟢 Terminé |
| **1. Produits & Catégories** | `[x]` | `[x]` | `[x]` | 🟢 Terminé |
| **2. Stocks, Mouvements & Pertes** | `[x]` | `[x]` | `[x]` | 🟢 Terminé |
| **3. Recettes & Nutrition** | `[x]` | `[x]` | `[x]` | 🟢 Terminé |
| **4. Moteur Recommandation & Dashboard** | `[ ]` | `[ ]` | `[ ]` | ⚪ Non démarré |
| **5. Planning des Repas** | `[ ]` | `[ ]` | `[ ]` | ⚪ Non démarré |
| **6. Courses & Consommation** | `[ ]` | `[ ]` | `[ ]` | ⚪ Non démarré |
| **7. PWA & Synchronisation Offline** | `[ ]` | `[ ]` | `[ ]` | ⚪ Non démarré |
| **8. Hardware Simulé** | `[ ]` | `[ ]` | `[ ]` | ⚪ Non démarré |
| **9. Sécurité & Scénario Démo E2E** | `[ ]` | `[ ]` | `[ ]` | ⚪ Non démarré |
