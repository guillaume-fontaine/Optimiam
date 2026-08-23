# Dossier d’architecture V1 — Projet OptiMiam

> **Version : 1.0 — Architecture cible pour le projet annuel**
> **Stack imposée : Java / Spring Boot + Angular + Angular Material 3 + PWA**
> **Hardware : simulé en V1**
> **Principe : architecture conçue pour être développée majoritairement avec une IA**

J’ai construit cette V1 à partir des éléments visibles sur tes tableaux, en conservant notamment les notions de **recettes, stock, produits, pertes, recommandation, planning, liste de courses, nutrition, personnalisation, transactions, synchronisation, PWA et matériel simulé**.
Lorsque le tableau ne permet pas de déterminer précisément un choix, je le marque comme **à valider** plutôt que de l’inventer.

---

# 1. Vision du projet

## 1.1 Problématique

Le projet vise à construire une application permettant de **gérer et valoriser les produits alimentaires disponibles**, tout en réduisant les pertes et en facilitant la préparation de repas/recettes.

Le système doit notamment permettre de :

* connaître les produits disponibles ;
* gérer les stocks ;
* suivre les dates et quantités ;
* identifier les produits proches de leur péremption ;
* proposer des recettes adaptées aux produits disponibles ;
* recommander des recettes selon les préférences et contraintes ;
* construire un planning ;
* générer une liste de courses ;
* gérer les informations nutritionnelles ;
* suivre les entrées/sorties de stock ;
* conserver un historique ;
* fonctionner sur mobile/tablette/ordinateur ;
* fonctionner partiellement hors ligne grâce à une PWA ;
* préparer l'intégration future d'une balance, d'une imprimante, d'un scanner, etc.

---

# 2. Objectifs

## 2.1 Objectif principal

> **Transformer les données de stock et les préférences utilisateur en recommandations de recettes et en actions concrètes permettant de limiter les pertes alimentaires.**

Le système doit donc faire le lien :

```text
PRODUITS
   │
   ├── Stock
   ├── Quantité
   ├── Date
   ├── Nutrition
   └── État
          │
          ▼
     RECOMMANDATION
          │
          ▼
       RECETTES
          │
          ├── Planning
          ├── Liste de courses
          └── Préparation
```

---

# 3. Principes directeurs

Les décisions d'architecture V1 sont les suivantes.

| Principe                 | Décision                                                        |
| ------------------------ | --------------------------------------------------------------- |
| Backend                  | Spring Boot                                                     |
| Frontend                 | Angular                                                         |
| UI                       | Angular Material 3                                              |
| Mobile                   | PWA                                                             |
| API                      | REST/JSON                                                       |
| Architecture backend     | Modular Monolith                                                |
| Base principale          | PostgreSQL                                                      |
| Cache/offline            | IndexedDB                                                       |
| Communication asynchrone | Events                                                          |
| Hardware                 | Simulateurs                                                     |
| Authentification         | JWT/OIDC à préciser                                             |
| Documentation API        | OpenAPI                                                         |
| Tests backend            | JUnit + Testcontainers                                          |
| Tests frontend           | Angular Testing                                                 |
| Déploiement              | Docker                                                          |
| CI/CD                    | GitHub Actions/GitLab CI à choisir                              |
| IA                       | utilisée pour générer, tester, documenter et refactorer le code |

---

# 4. Pourquoi un Modular Monolith ?

Je recommande **de ne surtout pas commencer en microservices** pour le projet annuel.

Architecture V1 :

```text
                 ┌─────────────────────────┐
                 │       Angular PWA       │
                 │ Angular Material 3      │
                 └────────────┬────────────┘
                              │ HTTPS / REST
                              ▼
                 ┌─────────────────────────┐
                 │      Spring Boot        │
                 │                         │
                 │ ┌─────────────────────┐ │
                 │ │ Stock               │ │
                 │ │ Produit             │ │
                 │ │ Recette             │ │
                 │ │ Recommendation      │ │
                 │ │ Planning            │ │
                 │ │ Nutrition           │ │
                 │ │ Courses             │ │
                 │ │ Transaction         │ │
                 │ │ Synchronisation     │ │
                 │ │ Hardware            │ │
                 │ └─────────────────────┘ │
                 └────────────┬────────────┘
                              │
                 ┌────────────▼────────────┐
                 │       PostgreSQL        │
                 └─────────────────────────┘
```

### Pourquoi ?

Parce que le projet doit être :

* suffisamment complexe pour démontrer une vraie architecture ;
* mais réalisable par une petite équipe ;
* facilement développable avec une IA ;
* facilement déployable ;
* facilement testable.

Les modules sont **séparés logiquement**, mais restent dans la même application.

Plus tard :

```text
Modular Monolith
       │
       ├── Stock
       ├── Recommendation
       ├── Recipe
       └── Sync
             │
             ▼
       Microservices
```

pourrait être envisagé si le besoin apparaît.

---

# 5. Architecture globale

## 5.1 Vue logique

```text
┌─────────────────────────────────────────────────────────────┐
│                         CLIENTS                             │
│                                                             │
│   Desktop        Tablet        Smartphone                   │
│      │              │              │                       │
│      └──────────────┴──────────────┘                       │
│                     PWA                                    │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          │ HTTPS
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    ANGULAR FRONTEND                         │
│                                                             │
│  Dashboard                                                 │
│  Stock                                                      │
│  Produits                                                   │
│  Recettes                                                   │
│  Recommandations                                            │
│  Planning                                                   │
│  Liste de courses                                           │
│  Nutrition                                                  │
│  Transactions                                               │
│  Administration                                             │
│                                                             │
│  Offline Store / IndexedDB                                  │
│  Service Worker                                             │
└─────────────────────────┬───────────────────────────────────┘
                          │ REST
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                     SPRING BOOT                             │
│                                                             │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌───────────────────────┐ │
│ │ Stock  │ │Produit │ │Recette │ │ Recommendation        │ │
│ └────────┘ └────────┘ └────────┘ └───────────────────────┘ │
│                                                             │
│ ┌────────────┐ ┌──────────┐ ┌──────────────┐              │
│ │ Planning   │ │ Nutrition│ │ Courses      │              │
│ └────────────┘ └──────────┘ └──────────────┘              │
│                                                             │
│ ┌──────────────┐ ┌──────────────┐ ┌─────────────────────┐ │
│ │ Transaction  │ │ Synchronisation│ │ Hardware Simulator │ │
│ └──────────────┘ └──────────────┘ └─────────────────────┘ │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
                  ┌─────────────────┐
                  │   PostgreSQL    │
                  └─────────────────┘
```

---

# 6. Architecture frontend

## 6.1 Angular

Le frontend sera une application Angular moderne structurée par fonctionnalités.

```text
src/
├── app/
│   ├── core/
│   │   ├── auth/
│   │   ├── guards/
│   │   ├── interceptors/
│   │   ├── http/
│   │   └── services/
│   │
│   ├── shared/
│   │   ├── components/
│   │   ├── directives/
│   │   ├── pipes/
│   │   └── models/
│   │
│   ├── features/
│   │   ├── dashboard/
│   │   ├── stock/
│   │   ├── produits/
│   │   ├── recettes/
│   │   ├── recommandations/
│   │   ├── planning/
│   │   ├── courses/
│   │   ├── nutrition/
│   │   ├── transactions/
│   │   └── administration/
│   │
│   └── app.routes.ts
│
├── assets/
└── environments/
```

---

# 7. Angular Material 3

L'interface doit utiliser Angular Material comme base de design.

Composants principaux :

* `MatToolbar`
* `MatSidenav`
* `MatCard`
* `MatTable`
* `MatDialog`
* `MatFormField`
* `MatInput`
* `MatSelect`
* `MatDatepicker`
* `MatChips`
* `MatProgressBar`
* `MatSnackBar`
* `MatButton`
* `MatIcon`
* `MatMenu`
* `MatTabs`

## Principe UI

L'application doit être :

### Desktop

```text
┌─────────────────────────────────────────────────────┐
│ OptiMiam                            👤 Guillaume    │
├──────────┬──────────────────────────────────────────┤
│ Dashboard│                                          │
│ Stock    │              CONTENU                     │
│ Recettes │                                          │
│ Planning │                                          │
│ Courses  │                                          │
│ ...      │                                          │
└──────────┴──────────────────────────────────────────┘
```

### Mobile

```text
┌────────────────────┐
│ OptiMiam        ☰  │
├────────────────────┤
│                    │
│      CONTENU       │
│                    │
│                    │
├────────────────────┤
│ 🏠  📦  🍳  🛒    │
└────────────────────┘
```

---

# 8. PWA

La PWA est un élément important du projet.

## Fonctionnalités

### Online

```text
Angular
   ↓
REST API
   ↓
Spring Boot
   ↓
PostgreSQL
```

### Offline

```text
Angular
   ↓
IndexedDB
   ↓
Pending Operations
```

Puis lorsque la connexion revient :

```text
IndexedDB
    │
    ▼
Sync Engine
    │
    ▼
Spring Boot
    │
    ▼
PostgreSQL
```

---

# 9. Stratégie Offline

On ne cherche pas à rendre **100 % de l'application** offline en V1.

### Offline prioritaire

* consultation du stock ;
* consultation des recettes ;
* consultation du planning ;
* modification simple du stock ;
* création d'une transaction ;
* consultation de la liste de courses.

### Online obligatoire

* calcul complexe de recommandation ;
* administration ;
* gestion utilisateurs ;
* opérations nécessitant des données serveur à jour.

---

# 10. Synchronisation

Le tableau mentionne explicitement :

* synchronisation ;
* export/import ;
* état courant ;
* transactions ;
* fonctionnement décentralisé ;
* historique ;
* P2P/cloud.

Je propose donc de formaliser cela ainsi :

```text
                    CLOUD
                      │
                      │
              ┌───────▼───────┐
              │ Spring Boot   │
              └───────┬───────┘
                      │
                Synchronisation
                      │
       ┌──────────────┴──────────────┐
       ▼                             ▼
   Smartphone                    Tablet
       │                             │
   IndexedDB                     IndexedDB
```

---

# 11. Modèle de synchronisation

Chaque modification locale devient une opération.

```json
{
  "operationId": "uuid",
  "deviceId": "device-001",
  "timestamp": "2026-08-23T10:30:00Z",
  "entityType": "STOCK",
  "entityId": "uuid",
  "operation": "UPDATE",
  "payload": {}
}
```

Le serveur doit pouvoir détecter :

* opérations déjà reçues ;
* conflits ;
* opérations invalides ;
* versions différentes.

---

# 12. Gestion des conflits

V1 :

> **Last Write Wins + version d'entité**

Chaque entité possède :

```text
version
updatedAt
updatedBy
```

Exemple :

```text
Client A
Stock #123
version 4
     │
     ├── modification
     ▼
version 5

Client B
Stock #123
version 4
     │
     └── modification
```

Le serveur détecte que B travaille sur une version obsolète.

Réponse :

```http
409 CONFLICT
```

La résolution avancée des conflits pourra être ajoutée ultérieurement.

---

# 13. Backend Spring Boot

## 13.1 Architecture interne

Je recommande :

```text
API
 │
 ▼
Application
 │
 ▼
Domain
 │
 ▼
Infrastructure
```

Exemple :

```text
stock/
├── api/
│   ├── StockController.java
│   └── dto/
│
├── application/
│   ├── StockService.java
│   └── usecase/
│
├── domain/
│   ├── Stock.java
│   ├── StockItem.java
│   └── StockRepository.java
│
└── infrastructure/
    ├── persistence/
    └── hardware/
```

---

# 14. Modules métier

## Module 1 — Produit

Responsable de :

* catalogue produit ;
* catégorie ;
* unité ;
* informations nutritionnelles ;
* durée de conservation ;
* informations diverses.

---

## Module 2 — Stock

Responsable de :

* quantité ;
* entrée ;
* sortie ;
* consommation ;
* perte ;
* péremption ;
* emplacement.

Exemple :

```text
Produit : Tomate
Quantité : 2.5 kg
Date entrée : 20/08
Date limite : 25/08
Emplacement : Frigo
État : DISPONIBLE
```

---

# 15. Module Transaction

Le tableau mentionne explicitement :

> **Transactionnel — Entrée / Sortie — Produit — Identifiant — Info Produit**

Une transaction représente donc une modification du stock.

```text
Stock
 │
 ├── ENTRY
 ├── EXIT
 ├── CONSUMPTION
 ├── LOSS
 ├── ADJUSTMENT
 └── INVENTORY
```

Exemple :

```json
{
  "type": "EXIT",
  "productId": "tomato-123",
  "quantity": 500,
  "unit": "g",
  "reason": "RECIPE_CONSUMPTION"
}
```

---

# 16. Module Recette

La BDD de recettes apparaît explicitement sur le tableau.

Une recette contient :

```text
Recipe
 ├── id
 ├── name
 ├── description
 ├── preparationTime
 ├── difficulty
 ├── servings
 ├── ingredients
 ├── steps
 ├── nutrition
 ├── tags
 └── constraints
```

Exemple :

```text
Omelette aux légumes

Ingrédients
- 3 œufs
- 200 g courgette
- 100 g tomate
- 20 g fromage

Temps : 20 min
Difficulté : facile
```

---

# 17. Module Recommandation

C'est un des modules centraux du projet.

Le tableau mentionne :

* recommandation ;
* personnalisation ;
* complexité ;
* scoring ;
* ILP ;
* diversité ;
* planning ;
* priorité aux produits à utiliser.

## V1

Le moteur de recommandation ne doit pas être une IA générative.

Il doit être **déterministe et explicable**.

---

# 18. Score de recommandation

On peut définir :

```text
Score =
    W_stock       × StockScore
  + W_expiration  × ExpirationScore
  + W_preference  × PreferenceScore
  + W_nutrition   × NutritionScore
  + W_complexity  × ComplexityScore
  + W_diversity   × DiversityScore
```

Exemple :

```text
Stock disponible        30 %
Proximité péremption    30 %
Préférences             20 %
Nutrition               10 %
Complexité              5 %
Diversité               5 %
```

**Ces pondérations sont des valeurs initiales de V1 et devront être validées.**

---

# 19. Exemple de recommandation

Stock :

```text
Tomates       1 kg   → expire demain
Courgettes    500 g  → expire dans 2 jours
Œufs          6
Fromage       200 g
```

Recettes :

```text
A — Ratatouille
B — Omelette légumes
C — Salade tomate
```

Le système calcule :

```text
Ratatouille       87
Omelette          81
Salade            64
```

Résultat :

```text
🥇 Ratatouille
🥈 Omelette légumes
🥉 Salade tomate
```

Et surtout :

> **Pourquoi cette recette ?**

```text
+ utilise 3 produits proches de la péremption
+ 92 % des ingrédients sont disponibles
+ correspond à vos préférences
+ difficulté faible
```

C'est très intéressant pour une démonstration de projet.

---

# 20. ILP

Le tableau mentionne :

> `ILP (integer linear programming)`

Je recommande de ne **pas commencer par l'ILP**.

Roadmap :

```text
V1
 │
 └── Scoring déterministe
          │
          ▼
V1.5
 │
 └── Optimisation
          │
          ▼
V2
 │
 └── ILP / optimisation planning
```

L'ILP pourra ensuite servir à optimiser :

* utilisation des stocks ;
* coût ;
* diversité ;
* nutrition ;
* nombre de recettes ;
* contraintes alimentaires ;
* temps de préparation.

---

# 21. Module Planning

Le planning permet d'associer :

```text
Date
 +
Repas
 +
Recette
```

Exemple :

| Jour     | Midi        | Soir     |
| -------- | ----------- | -------- |
| Lundi    | Salade      | Omelette |
| Mardi    | Ratatouille | Pâtes    |
| Mercredi | Curry       | Soupe    |

Le moteur peut ensuite calculer les ingrédients nécessaires.

---

# 22. Module Liste de courses

Le tableau mentionne explicitement :

> Recettes → Planning → liste courses

Processus :

```text
Planning
   │
   ▼
Recettes
   │
   ▼
Ingrédients nécessaires
   │
   -
   │
Stock disponible
   │
   ▼
Quantité manquante
   │
   ▼
LISTE DE COURSES
```

Exemple :

```text
Planning demande :

Tomates : 1.5 kg
Œufs : 10
Courgettes : 1 kg

Stock :

Tomates : 1 kg
Œufs : 6
Courgettes : 500 g

Courses :

☐ 500 g tomates
☐ 4 œufs
☐ 500 g courgettes
```

---

# 23. Module Nutrition

Le tableau mentionne :

> Nutrition → pouvoir énergie

Le modèle V1 doit donc pouvoir stocker :

```text
Calories
Protéines
Glucides
Lipides
Fibres
Sel
```

Par produit et éventuellement par recette.

---

# 24. Module Préférences utilisateur

Les éléments du tableau suggèrent une personnalisation selon :

* goûts ;
* complexité ;
* contraintes ;
* nutrition ;
* diversité.

Exemple :

```json
{
  "maxPreparationTime": 30,
  "preferredDifficulty": "EASY",
  "preferredCategories": [
    "ITALIAN",
    "VEGETARIAN"
  ],
  "excludedIngredients": [],
  "nutritionObjectives": {}
}
```

---

# 25. Module pertes

L'objectif :

> **Économique → Écologique → moins de pertes**

Une perte doit être enregistrée comme une transaction.

```text
LOSS
 ├── product
 ├── quantity
 ├── reason
 ├── date
 └── comment
```

Cela permet ensuite de produire :

```text
Pertes cette semaine
██████████░░ 8.2 kg

Coût estimé
32 €

Produits les plus perdus
1. Tomates
2. Salade
3. Pain
```

---

# 26. Dashboard

Le dashboard V1 doit être simple et démonstratif.

```text
┌─────────────────────────────────────────────────┐
│                  DASHBOARD                      │
├───────────────┬───────────────┬─────────────────┤
│ Stock         │ À consommer   │ Pertes          │
│ 126 produits  │ 8 produits    │ 2.4 kg          │
├───────────────┴───────────────┴─────────────────┤
│                                                 │
│ 🔴 À utiliser rapidement                        │
│                                                 │
│ Tomates      expire demain                      │
│ Salade       expire dans 2 jours                │
│                                                 │
├─────────────────────────────────────────────────┤
│ ⭐ Recettes recommandées                        │
│                                                 │
│ Ratatouille             92 %                    │
│ Omelette légumes        87 %                    │
│ Curry                    81 %                    │
└─────────────────────────────────────────────────┘
```

---

# 27. Hardware

Le tableau montre une architecture avec :

* application ;
* matériel ;
* imprimante ;
* scanner ;
* balance ;
* ticket.

Mais tu précises :

> **pas de hardware pour le moment, tout sera simulé si besoin.**

C'est une excellente décision pour la V1.

---

# 28. Hardware Abstraction Layer

On ne doit donc **jamais coder directement l'application métier contre une balance réelle**.

On définit une interface :

```java
public interface ScaleService {

    WeightMeasurement measure();

}
```

Puis :

```text
ScaleService
     │
     ├── SimulatedScaleService
     │
     └── RealScaleService       ← futur
```

Même principe pour l'imprimante :

```java
public interface PrinterService {

    PrintResult print(PrintDocument document);

}
```

Implémentation V1 :

```text
PrinterService
       │
       └── SimulatedPrinterService
```

---

# 29. Simulateur de balance

L'interface peut proposer :

```text
┌───────────────────────────┐
│     BALANCE SIMULÉE       │
├───────────────────────────┤
│                           │
│       1.250 kg            │
│                           │
│  [-]              [+]     │
│                           │
│       [ MESURER ]         │
└───────────────────────────┘
```

L'API :

```http
POST /api/v1/hardware/scales/simulated/measure
```

Réponse :

```json
{
  "weight": 1.25,
  "unit": "KG",
  "timestamp": "..."
}
```

---

# 30. Simulateur imprimante

```text
┌─────────────────────────────┐
│      IMPRIMANTE SIMULÉE     │
├─────────────────────────────┤
│ Ticket #123                 │
│                             │
│ Tomates       1.2 kg        │
│ Courgettes    0.5 kg        │
│                             │
│ [ IMPRIMER ]                │
└─────────────────────────────┘
```

En V1 :

```text
PrintDocument
      │
      ▼
SimulatedPrinter
      │
      ▼
PDF / aperçu écran / log
```

---

# 31. Architecture matériel future

```text
                 Application
                     │
              Hardware API
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
     Balance      Scanner      Printer
        │            │            │
        └────────────┴────────────┘
                     │
               Adapter Layer
                     │
              Hardware réel
```

Cela permet de faire une démonstration sans matériel.

---

# 32. Modèle de données V1

## Entités principales

```text
User
Product
Category
StockItem
StockTransaction
Recipe
RecipeIngredient
RecipeStep
Nutrition
Preference
Recommendation
MealPlan
ShoppingList
ShoppingListItem
SyncOperation
Device
```

---

# 33. Relations principales

```text
User
 │
 ├──────────── Preference
 │
 ├──────────── MealPlan
 │
 └──────────── Device


Product
 │
 ├──────────── StockItem
 │
 ├──────────── RecipeIngredient
 │
 └──────────── Nutrition


StockItem
 │
 └──────────── StockTransaction


Recipe
 │
 ├──────────── RecipeIngredient
 │
 ├──────────── RecipeStep
 │
 └──────────── Nutrition


MealPlan
 │
 └──────────── Recipe


ShoppingList
 │
 └──────────── ShoppingListItem
```

---

# 34. Schéma relationnel simplifié

```text
USER
-----
id PK
email
name
created_at


PRODUCT
-------
id PK
name
category_id FK
unit
barcode
created_at


STOCK_ITEM
----------
id PK
product_id FK
quantity
unit
expiration_date
location
version
updated_at


STOCK_TRANSACTION
-----------------
id PK
stock_item_id FK
type
quantity
reason
created_at
device_id


RECIPE
------
id PK
name
description
difficulty
preparation_time
servings


RECIPE_INGREDIENT
-----------------
id PK
recipe_id FK
product_id FK
quantity
unit


MEAL_PLAN
---------
id PK
user_id FK
date
meal_type
recipe_id FK


SHOPPING_LIST
-------------
id PK
user_id FK
status


SHOPPING_LIST_ITEM
------------------
id PK
shopping_list_id FK
product_id FK
quantity
unit
checked
```

---

# 35. Identifiants

Utiliser des UUID.

```text
UUID
```

plutôt que :

```text
Long auto-increment
```

C'est particulièrement intéressant avec la synchronisation offline.

---

# 36. Versionnement

Les entités synchronisables auront :

```text
id
version
createdAt
updatedAt
deletedAt
```

La suppression sera idéalement une **soft delete** pour éviter les problèmes de synchronisation.

---

# 37. API REST

Préfixe :

```text
/api/v1
```

## Produits

```http
GET    /api/v1/products
GET    /api/v1/products/{id}
POST   /api/v1/products
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}
```

## Stock

```http
GET  /api/v1/stock
GET  /api/v1/stock/{id}
POST /api/v1/stock/entries
POST /api/v1/stock/exits
POST /api/v1/stock/losses
```

## Recettes

```http
GET  /api/v1/recipes
GET  /api/v1/recipes/{id}
POST /api/v1/recipes
PUT  /api/v1/recipes/{id}
```

## Recommandation

```http
GET /api/v1/recommendations
POST /api/v1/recommendations/preview
```

## Planning

```http
GET  /api/v1/planning
POST /api/v1/planning
PUT  /api/v1/planning/{id}
DELETE /api/v1/planning/{id}
```

## Courses

```http
GET  /api/v1/shopping-lists
POST /api/v1/shopping-lists
PUT  /api/v1/shopping-lists/{id}/items/{itemId}
```

---

# 38. API recommandation

Exemple :

```http
POST /api/v1/recommendations
```

Request :

```json
{
  "date": "2026-08-23",
  "maxResults": 10,
  "maxPreparationTime": 30,
  "useExpiringProducts": true
}
```

Response :

```json
{
  "recommendations": [
    {
      "recipeId": "uuid",
      "score": 0.92,
      "reasons": [
        "EXPIRING_PRODUCTS",
        "STOCK_MATCH",
        "USER_PREFERENCE"
      ]
    }
  ]
}
```

---

# 39. Événements métier

Le tableau suggère une architecture événementielle.

V1 :

```text
StockEntryCreated
StockExitCreated
StockLossRecorded
RecipeCreated
PlanningUpdated
ShoppingListGenerated
RecommendationGenerated
```

Exemple :

```text
StockLossRecorded
       │
       ├──► Statistics
       │
       ├──► Dashboard
       │
       └──► Recommendation Engine
```

---

# 40. Event bus

En V1, il n'est pas nécessaire d'ajouter Kafka.

Spring permet de commencer avec :

```text
ApplicationEventPublisher
```

ou une abstraction interne :

```java
DomainEventPublisher
```

Architecture :

```text
Domain
  │
  ▼
Event
  │
  ▼
Event Handler
```

Plus tard :

```text
Spring Event
     ↓
Kafka
```

si nécessaire.

---

# 41. Transactionnalité

Les opérations de stock doivent être atomiques.

Exemple :

```text
POST /stock/exits
```

doit effectuer :

```text
1. Vérifier stock
2. Vérifier quantité
3. Modifier stock
4. Créer transaction
5. Publier événement
6. Commit
```

Si une étape échoue :

```text
ROLLBACK
```

---

# 42. Sécurité

V1 :

```text
Angular
   │
   │ JWT
   ▼
Spring Security
   │
   ├── USER
   ├── MANAGER
   └── ADMIN
```

Endpoints protégés :

```text
/api/v1/**
```

Endpoints publics éventuels :

```text
/api/v1/auth/**
```

---

# 43. Validation

Validation côté frontend **et** backend.

Exemple :

```java
@NotNull
@Positive
private BigDecimal quantity;
```

Le frontend ne doit jamais être considéré comme une barrière de sécurité.

---

# 44. Gestion des erreurs API

Format standard :

```json
{
  "timestamp": "...",
  "status": 400,
  "code": "INVALID_QUANTITY",
  "message": "Quantity must be greater than zero",
  "path": "/api/v1/stock/exits"
}
```

Codes métier explicites :

```text
PRODUCT_NOT_FOUND
STOCK_NOT_FOUND
INSUFFICIENT_STOCK
RECIPE_NOT_FOUND
INVALID_QUANTITY
CONFLICT
SYNC_CONFLICT
```

---

# 45. Documentation OpenAPI

L'API doit être documentée automatiquement.

Objectif :

```text
Spring Boot
     │
     ▼
OpenAPI
     │
     ▼
Swagger UI
```

Cela est particulièrement utile pour le développement avec une IA.

L'IA peut ainsi disposer d'un contrat précis :

```text
GET /products
POST /stock/entries
GET /recommendations
```

au lieu de devoir deviner le backend.

---

# 46. Tests

## Backend

Pyramide :

```text
             E2E
             /\
            /  \
       Integration
          /      \
         /        \
      Unit Tests
```

### Unit

```text
RecommendationServiceTest
StockServiceTest
ShoppingListServiceTest
```

### Integration

```text
PostgreSQL réel via Testcontainers
```

---

# 47. Frontend

Tests :

```text
Component tests
Service tests
Guard tests
Pipe tests
```

Puis quelques tests E2E :

```text
Playwright
```

---

# 48. Scénarios E2E essentiels

### Scénario 1

```text
Connexion
→ Dashboard
→ Stock
→ Ajout produit
→ Ajout quantité
→ Produit visible
```

### Scénario 2

```text
Produit proche péremption
→ Recommandation
→ Recette sélectionnée
→ Planning
→ Liste de courses
```

### Scénario 3

```text
Offline
→ Modification stock
→ Reconnexion
→ Synchronisation
→ Données serveur mises à jour
```

### Scénario 4

```text
Balance simulée
→ Mesure
→ Entrée stock
```

---

# 49. Qualité de code

Backend :

```text
Java
Spring Boot
Spring Data JPA
Spring Security
Bean Validation
JUnit
Mockito
Testcontainers
OpenAPI
```

Frontend :

```text
Angular
TypeScript
Angular Material 3
RxJS
PWA
IndexedDB
```

---

# 50. Docker

Environnement de développement :

```text
docker-compose.yml

services:

  postgres:
    image: postgres

  backend:
    build: ./backend

  frontend:
    build: ./frontend
```

Architecture :

```text
┌───────────────┐
│    Browser    │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│   Angular     │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Spring Boot   │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ PostgreSQL    │
└───────────────┘
```

---

# 51. CI/CD

Pipeline :

```text
git push
   │
   ▼
Build
   │
   ├── Frontend tests
   ├── Backend tests
   ├── Integration tests
   ├── Lint
   └── Security checks
          │
          ▼
       Package
          │
          ▼
      Docker image
          │
          ▼
       Deploy
```

---

# 52. Observabilité

Minimum V1 :

```text
Spring Actuator
```

Endpoints :

```text
/actuator/health
/actuator/info
```

Logs structurés :

```text
timestamp
level
service
requestId
userId
message
```

Exemple :

```text
INFO StockService
transaction=uuid
product=uuid
quantity=500
type=EXIT
```

---

# 53. Architecture de packages complète

Je recommande cette structure :

```text
backend/
└── src/main/java/com/project/nut/

    ├── common/
    │   ├── exception/
    │   ├── security/
    │   ├── event/
    │   └── pagination/
    │
    ├── product/
    │   ├── api/
    │   ├── application/
    │   ├── domain/
    │   └── infrastructure/
    │
    ├── stock/
    │   ├── api/
    │   ├── application/
    │   ├── domain/
    │   └── infrastructure/
    │
    ├── recipe/
    │   ├── api/
    │   ├── application/
    │   ├── domain/
    │   └── infrastructure/
    │
    ├── recommendation/
    │   ├── api/
    │   ├── application/
    │   ├── domain/
    │   └── infrastructure/
    │
    ├── planning/
    ├── shopping/
    ├── nutrition/
    ├── transaction/
    ├── synchronization/
    └── hardware/
```

---

# 54. Architecture Angular complète

```text
frontend/
└── src/app/

    ├── core/
    │   ├── auth/
    │   ├── http/
    │   ├── guards/
    │   └── layout/
    │
    ├── shared/
    │   ├── ui/
    │   ├── models/
    │   └── utilities/
    │
    └── features/
        ├── dashboard/
        ├── stock/
        ├── products/
        ├── recipes/
        ├── recommendations/
        ├── planning/
        ├── shopping/
        ├── nutrition/
        ├── transactions/
        ├── sync/
        └── hardware/
```

---

# 55. Architecture offline frontend

```text
                 Angular
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
      API Client         Offline Store
          │                   │
          │               IndexedDB
          │                   │
          └─────────┬─────────┘
                    ▼
              Sync Manager
                    │
                    ▼
                 REST API
```

---

# 56. Contraintes techniques

## Obligatoires

### Backend

* Java ;
* Spring Boot ;
* API REST ;
* PostgreSQL ;
* architecture modulaire ;
* validation ;
* tests ;
* documentation OpenAPI.

### Frontend

* Angular ;
* Angular Material 3 ;
* responsive ;
* PWA ;
* fonctionnement offline partiel.

### Architecture

* UUID ;
* transactions ;
* gestion des conflits ;
* événements métier ;
* abstraction hardware.

### Hardware

Aucun hardware réel en V1.

---

# 57. Contraintes de développement avec IA

C'est un point important puisque tu veux que **le développement soit réalisé avec une IA**.

Il ne faut surtout pas demander :

> "Développe toute l'application."

L'IA va produire énormément de code difficile à maintenir.

Il faut imposer :

```text
Architecture
    ↓
Contrat
    ↓
Module
    ↓
Use case
    ↓
Tests
    ↓
Implementation
    ↓
Review
```

---

# 58. Règle fondamentale pour l'IA

Chaque génération doit avoir :

```text
INPUT
 ↓
SPECIFICATION
 ↓
CODE
 ↓
TESTS
 ↓
REVIEW
```

L'IA ne doit jamais décider seule d'une modification d'architecture.

---

# 59. Prompt maître pour l'IA

Voici le **prompt système principal** que je recommande de donner à l'IA de développement :

```text
Tu es l'architecte logiciel principal du projet OptiMiam.

Tu développes une application de gestion intelligente des produits alimentaires,
des stocks, des recettes, des recommandations, du planning et des listes de courses.

STACK OBLIGATOIRE

Backend:
- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- PostgreSQL
- Bean Validation
- OpenAPI
- JUnit
- Testcontainers

Frontend:
- Angular
- TypeScript
- Angular Material 3
- PWA
- IndexedDB
- RxJS

ARCHITECTURE

Le backend est un Modular Monolith.
Les modules doivent être isolés :
- product
- stock
- recipe
- recommendation
- planning
- shopping
- nutrition
- transaction
- synchronization
- hardware

Chaque module doit respecter :
API
Application
Domain
Infrastructure

Le domaine ne doit pas dépendre de l'infrastructure.

REGLES

1. Ne jamais introduire une nouvelle technologie sans justification.
2. Ne jamais créer de microservice en V1.
3. Ne jamais connecter directement le métier à un hardware.
4. Les équipements sont simulés.
5. Les entités synchronisables utilisent des UUID.
6. Les modifications de stock doivent être transactionnelles.
7. Les erreurs API utilisent un format standardisé.
8. Toute fonctionnalité doit avoir des tests.
9. Toute API doit être documentée.
10. Le code doit être production-ready.
11. Ne jamais casser une API existante sans migration.
12. Avant de coder, analyser l'architecture existante.
13. Ne jamais inventer une classe ou un service déjà existant.
14. Ne modifier que les fichiers nécessaires.
15. Toujours expliquer les décisions architecturales importantes.

RECOMMANDATION

Le moteur de recommandation V1 est déterministe et explicable.
Il utilise un système de scoring.
L'optimisation ILP est prévue pour une version ultérieure.

PWA

L'application doit fonctionner offline pour les fonctionnalités définies
dans la documentation.
Les modifications offline sont stockées localement puis synchronisées.

HARDWARE

Créer des interfaces :
- ScaleService
- PrinterService
- ScannerService

Créer uniquement les implémentations simulées en V1.

QUALITE

Pour chaque fonctionnalité :
1. analyser
2. proposer l'architecture
3. implémenter
4. écrire les tests
5. vérifier les erreurs
6. documenter
7. fournir un résumé des fichiers modifiés

Ne jamais générer du code inutile.
```

---

# 60. Prompt pour générer un module

```text
Implémente le module [NOM DU MODULE].

Contexte:
Projet OptiMiam.
Architecture Modular Monolith.
Java Spring Boot.

Avant toute modification :

1. analyse l'architecture existante ;
2. identifie les classes concernées ;
3. identifie les dépendances ;
4. propose les changements ;
5. attends validation si une décision architecturale est nécessaire.

Le module doit respecter :

api/
application/
domain/
infrastructure/

Implémente :
- entities
- repositories
- use cases
- services
- DTO
- controllers
- validation
- exceptions
- tests

Contraintes :
- UUID
- transactions Spring
- aucune dépendance du domaine vers l'infrastructure
- OpenAPI
- tests unitaires
- tests d'intégration si nécessaire

À la fin :
- liste les fichiers créés
- liste les fichiers modifiés
- explique les choix
- donne les commandes pour lancer les tests.
```

---

# 61. Prompt génération frontend

```text
Implémente la fonctionnalité Angular suivante :

[FONCTIONNALITÉ]

Stack :
- Angular
- TypeScript
- Angular Material 3
- PWA
- RxJS

Architecture :

core/
shared/
features/

Contraintes :

1. utiliser Angular Material ;
2. interface responsive ;
3. mobile-first lorsque pertinent ;
4. respecter les patterns Angular existants ;
5. ne pas mettre de logique métier complexe dans les composants ;
6. créer des services dédiés ;
7. gérer loading/error/empty states ;
8. gérer le mode offline si nécessaire ;
9. écrire les tests ;
10. respecter l'accessibilité.

Avant de coder :
analyse les composants existants et réutilise-les.

Ne crée pas de composant dupliqué.
```

---

# 62. Prompt pour le moteur de recommandation

```text
Implémente le moteur de recommandation V1 du projet OptiMiam.

Objectif :

Classer les recettes en fonction :
- des produits disponibles ;
- des produits proches de la péremption ;
- des préférences utilisateur ;
- du temps de préparation ;
- de la difficulté ;
- de la nutrition ;
- de la diversité.

Utiliser un score explicable.

Pour chaque recommandation retourner :
- recipeId
- score
- raisons

Le score doit être déterministe.

Ne pas utiliser de LLM dans le calcul du score.

Préparer l'architecture afin qu'un algorithme ILP puisse être ajouté
ultérieurement sans modifier l'API publique.

Créer :
- domain model
- scoring strategy
- recommendation service
- DTO
- controller
- tests unitaires
- tests d'intégration
```

---

# 63. Prompt pour le mode offline

```text
Implémente le système offline PWA pour le projet OptiMiam.

Objectif :

Permettre à l'utilisateur de consulter le stock et d'effectuer certaines
opérations sans connexion.

Architecture :

Angular
→ IndexedDB
→ Pending Operations
→ Sync Manager
→ REST API

Chaque opération doit posséder :
- operationId
- deviceId
- timestamp
- entityType
- entityId
- operation
- payload

Lors de la reconnexion :
1. détecter la connexion ;
2. envoyer les opérations ;
3. gérer les succès ;
4. gérer les conflits ;
5. gérer les erreurs ;
6. marquer les opérations synchronisées.

Ne jamais perdre une opération locale.
```

---

# 64. Prompt hardware

```text
Implémente l'abstraction hardware du projet OptiMiam.

Créer :

ScaleService
PrinterService
ScannerService

Ne jamais mettre de logique hardware dans les modules métier.

Créer les implémentations :

SimulatedScaleService
SimulatedPrinterService
SimulatedScannerService

La simulation doit permettre de démontrer le fonctionnement
sans matériel physique.

Préparer une architecture permettant d'ajouter ultérieurement
des implémentations réelles sans modifier le domaine.
```

---

# 65. Prompt de revue de code IA

À utiliser après chaque fonctionnalité :

```text
Effectue une code review complète du code fourni.

Analyse :

1. architecture
2. SOLID
3. séparation des responsabilités
4. sécurité
5. validation
6. gestion des erreurs
7. concurrence
8. transactions
9. performances
10. tests
11. maintenabilité
12. dette technique
13. sécurité des données
14. cohérence avec l'architecture OptiMiam

Ne réécris pas immédiatement le code.

Commence par produire :

CRITICAL
HIGH
MEDIUM
LOW

Pour chaque problème :
- fichier
- ligne si disponible
- problème
- risque
- correction proposée

Ne propose aucune technologie supplémentaire sans justification.
```

---

# 66. Roadmap de développement

Je conseille fortement cette progression.

## Sprint 0 — Architecture

```text
Architecture
Documentation
Repository
Docker
CI
Base Spring Boot
Base Angular
```

---

## Sprint 1 — Produit

```text
Product
Category
CRUD
Database
API
Frontend
Tests
```

---

## Sprint 2 — Stock

```text
Stock
Entry
Exit
Loss
Transactions
Dashboard
```

---

## Sprint 3 — Recettes

```text
Recipe
Ingredients
Steps
Nutrition
CRUD
```

---

## Sprint 4 — Recommandation

```text
Scoring
Priorité expiration
Stock matching
Préférences
Explications
```

---

## Sprint 5 — Planning

```text
Calendar
Meals
Recipes
Planning
```

---

## Sprint 6 — Courses

```text
Planning
   ↓
Ingredients
   ↓
Stock
   ↓
Shopping list
```

---

## Sprint 7 — PWA

```text
Service Worker
IndexedDB
Offline
Sync
Conflict
```

---

## Sprint 8 — Hardware simulé

```text
Balance
Scanner
Printer
```

---

## Sprint 9 — Qualité

```text
Tests
E2E
Security
Performance
Documentation
```

---

# 67. MVP / V1 réellement démontrable

Pour éviter que le projet annuel devienne trop gros, je définirais le **MVP V1** ainsi :

```text
             ┌─────────────┐
             │  PRODUITS   │
             └──────┬──────┘
                    │
                    ▼
             ┌─────────────┐
             │    STOCK    │
             └──────┬──────┘
                    │
           ┌────────┴─────────┐
           ▼                  ▼
      Péremption          Quantité
           │                  │
           └────────┬─────────┘
                    ▼
             ┌─────────────┐
             │RECOMMANDATION│
             └──────┬──────┘
                    ▼
             ┌─────────────┐
             │   RECETTE   │
             └──────┬──────┘
                    ▼
             ┌─────────────┐
             │  PLANNING   │
             └──────┬──────┘
                    ▼
             ┌─────────────┐
             │    COURSES  │
             └─────────────┘
```

C'est **le parcours utilisateur principal à faire fonctionner parfaitement**.

---

# 68. V1.1

Ensuite :

```text
Nutrition avancée
Statistiques pertes
Personnalisation
Offline
Synchronisation
```

---

# 69. V2

Puis :

```text
ILP
Optimisation planning
Machine Learning éventuel
Hardware réel
Synchronisation P2P avancée
```

---

# 70. Ce qu'il ne faut PAS faire en V1

Pour protéger le projet contre l'explosion de périmètre :

### ❌ Microservices

Pas nécessaire.

### ❌ Kafka

Pas nécessaire au début.

### ❌ Kubernetes

Pas nécessaire.

### ❌ IA générative pour les recommandations

Pas nécessaire.

### ❌ Machine Learning

Pas nécessaire.

### ❌ Hardware réel

Pas nécessaire.

### ❌ P2P complexe

Préparer l'architecture, mais ne pas le développer entièrement.

### ❌ ILP dès le début

Commencer par le scoring.

---

# 71. Critères de réussite

La V1 sera considérée comme fonctionnelle lorsqu'un utilisateur peut réaliser ce parcours :

```text
1. Se connecter
       ↓
2. Ajouter des produits
       ↓
3. Constituer son stock
       ↓
4. Déclarer une date de péremption
       ↓
5. Consulter son stock
       ↓
6. Voir les produits prioritaires
       ↓
7. Obtenir des recommandations
       ↓
8. Choisir une recette
       ↓
9. L'ajouter au planning
       ↓
10. Générer la liste de courses
       ↓
11. Consommer la recette
       ↓
12. Le stock est automatiquement mis à jour
       ↓
13. La transaction est historisée
       ↓
14. Les statistiques de pertes sont mises à jour
```

**C'est ce scénario que je ferais passer devant un jury.**

---

# 72. Décisions d'architecture V1 à figer

| ID      | Décision               | Statut        |
| ------- | ---------------------- | ------------- |
| ADR-001 | Modular Monolith       | ✅             |
| ADR-002 | Spring Boot            | ✅             |
| ADR-003 | Angular                | ✅             |
| ADR-004 | Angular Material 3     | ✅             |
| ADR-005 | PWA                    | ✅             |
| ADR-006 | PostgreSQL             | 🟢 recommandé |
| ADR-007 | REST                   | ✅             |
| ADR-008 | UUID                   | 🟢 recommandé |
| ADR-009 | Hardware abstraction   | ✅             |
| ADR-010 | Hardware simulé        | ✅             |
| ADR-011 | Scoring recommandation | 🟢 V1         |
| ADR-012 | ILP                    | 🔵 V2         |
| ADR-013 | IndexedDB              | 🟢 PWA        |
| ADR-014 | Synchronisation        | 🟢 V1/V1.1    |
| ADR-015 | Kafka                  | ❌ V1          |
| ADR-016 | Microservices          | ❌ V1          |

---

# 73. Points encore à valider

Les photos du tableau ne permettent pas de trancher certains points. Je les laisserais explicitement dans le dossier plutôt que de faire semblant qu'ils sont définis.

### Métier

* Qui est exactement l'utilisateur cible ?
* Est-ce une application personnelle, familiale, restaurant/cuisine professionnelle ou autre ?
* Le terme **OptiMiam** correspond-il bien au nom du projet ?
* Les notions de `Chef`, `Gastro`, etc. visibles sur le tableau sont-elles des rôles métier ?
* Quelles sont les règles exactes de péremption ?
* Quelles contraintes nutritionnelles doivent être supportées ?

### Technique

* PostgreSQL confirmé ?
* Hébergement ?
* GitHub ou GitLab ?
* Authentification locale ou SSO ?
* Besoin d'une vraie synchronisation multi-device en V1 ?
* Niveau d'offline attendu ?
* ILP obligatoire dans le projet final ou seulement piste d'évolution ?

---

# 74. Architecture finale V1 en une image mentale

Le projet peut être résumé comme ceci :

```text
                         ┌───────────────────┐
                         │      USER         │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │   ANGULAR PWA     │
                         │ Material 3        │
                         │ Offline           │
                         └─────────┬─────────┘
                                   │
                              REST / JWT
                                   │
                                   ▼
              ┌─────────────────────────────────────┐
              │            SPRING BOOT               │
              │                                     │
              │ Product ────────┐                  │
              │ Stock ──────────┤                  │
              │ Transaction ────┤                  │
              │ Recipe ─────────┤                  │
              │ Nutrition ──────┤                  │
              │ Recommendation ─┤                  │
              │ Planning ───────┤                  │
              │ Shopping ───────┤                  │
              │ Synchronisation ┤                  │
              │ Hardware ───────┘                  │
              │                                     │
              └──────────────┬──────────────────────┘
                             │
                  ┌──────────┴──────────┐
                  ▼                     ▼
          ┌───────────────┐      ┌───────────────┐
          │  PostgreSQL   │      │ Event System  │
          └───────────────┘      └───────────────┘
                                        │
                         ┌──────────────┼──────────────┐
                         ▼              ▼              ▼
                       Stats      Recommendation    Sync


                  HARDWARE — V1 SIMULÉ

                    ┌─────────────┐
                    │ ScaleService│
                    └──────┬──────┘
                           │
                    SimulatedScale

                    ┌─────────────┐
                    │PrinterService│
                    └──────┬──────┘
                           │
                  SimulatedPrinter
```

## Conclusion

Cette V1 donne une base suffisamment **professionnelle pour un projet annuel**, tout en restant réaliste à développer avec une IA.

Le point le plus important est de **ne pas essayer de tout construire en même temps** : le cœur démontrable doit être **Produit → Stock → Péremption → Recommandation → Recette → Planning → Courses → Consommation → Historique**. La PWA, la synchronisation et le hardware simulé viennent ensuite.

Pour la suite, je te conseille de transformer ce dossier en **vrai référentiel de développement** : `README + ADR + spécifications fonctionnelles + modèle SQL + contrat OpenAPI + backlog Jira/GitHub + prompts IA par sprint`. C'est ce qui permettra ensuite à une IA de développer le projet **module par module sans perdre l'architecture en route**.
