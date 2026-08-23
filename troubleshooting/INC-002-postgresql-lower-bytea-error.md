# 🛠️ INC-002 — Erreur PostgreSQL `function lower(bytea) does not exist`

| Métadonnée | Valeur |
| :--- | :--- |
| **Identifiant** | `INC-002` |
| **Date** | 2026-08-23 |
| **Composant** | Backend Spring Boot / Spring Data JPA / PostgreSQL 16 |
| **Sévérité** | Bloquant (Crash lors du chargement des listes de stocks et de recettes) |
| **Statut** | 🟢 Résolu |

---

## 💥 1. Symptôme & Message d'erreur

Lors de l'appel aux points d'entrée de recherche sans mot-clé (ex: `GET /api/v1/recipes` ou `GET /api/v1/stock`), l'application renvoyait une erreur HTTP 500 avec la trace PostgreSQL suivante :

```text
Caused by: org.postgresql.util.PSQLException: ERROR: function lower(bytea) does not exist
  Indice : No function matches the given name and argument types. You might need to add explicit type casts.
```

---

## 🔍 2. Analyse de la Cause Racine (Root Cause)

- Dans les interfaces Spring Data JPA [`RecipeRepository`](file:///var/home/trollgun/IdeaProjects/Optimiam/backend/src/main/java/fr/trollgun/optimiam/recipe/domain/RecipeRepository.java) et [`StockItemRepository`](file:///var/home/trollgun/IdeaProjects/Optimiam/backend/src/main/java/fr/trollgun/optimiam/stock/domain/StockItemRepository.java), les requêtes JPQL utilisaient la construction suivante :
  ```sql
  WHERE (:query IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')))
  ```
- Lorsque `:query` vaut `null` (aucun filtre saisi par l'utilisateur), le driver JDBC PostgreSQL associe le type `bytea` (binaire) par défaut au paramètre `null` non typé.
- PostgreSQL évalue alors l'expression `LOWER(CONCAT('%', NULL::bytea, '%'))` qui tente d'appliquer la fonction `LOWER()` à un type `bytea`.
- Comme la fonction `LOWER()` n'est définie dans PostgreSQL que pour les types `text` et `character varying`, PostgreSQL lève une exception `PSQLException`.

---

## 🛠️ 3. Solution Technique Apportée

1. **Formatage préalable du motif dans la couche Service :**
   - La construction de la chaîne de recherche (`"%" + query.toLowerCase().trim() + "%"`) est effectuée dans le service Java uniquement si `query` est non-nul et non-vide, sinon `null` est transmis.
2. **Optimisation des requêtes JPQL :**
   - Remplacement de `LOWER(CONCAT('%', :query, '%'))` par `:queryPattern` direct :
     ```java
     // RecipeRepository.java
     @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN r.tags t WHERE " +
            "(:queryPattern IS NULL OR LOWER(r.name) LIKE :queryPattern OR LOWER(r.description) LIKE :queryPattern) AND " +
            "(:difficulty IS NULL OR r.difficulty = :difficulty) AND " +
            "(:maxPrepTime IS NULL OR r.preparationTimeMinutes <= :maxPrepTime) AND " +
            "(:tag IS NULL OR t = :tag)")
     Page<Recipe> searchRecipes(@Param("queryPattern") String queryPattern, ...);
     ```
     ```java
     // StockItemRepository.java
     @Query("SELECT s FROM StockItem s WHERE s.quantity > 0 AND " +
            "(:location IS NULL OR s.location = :location) AND " +
            "(:queryPattern IS NULL OR LOWER(s.product.name) LIKE :queryPattern)")
     Page<StockItem> searchAvailableStock(@Param("location") Location location, @Param("queryPattern") String queryPattern, Pageable pageable);
     ```
3. **Avantages supplémentaires :**
   - Élimination totale de l'incompatibilité de type PostgreSQL.
   - Amélioration des performances : la fonction `LOWER()` n'est plus appelée sur le paramètre pour chaque ligne de la table.

---

## 📁 4. Fichiers Modifiés

- [`RecipeRepository.java`](file:///var/home/trollgun/IdeaProjects/Optimiam/backend/src/main/java/fr/trollgun/optimiam/recipe/domain/RecipeRepository.java)
- [`RecipeService.java`](file:///var/home/trollgun/IdeaProjects/Optimiam/backend/src/main/java/fr/trollgun/optimiam/recipe/application/RecipeService.java)
- [`StockItemRepository.java`](file:///var/home/trollgun/IdeaProjects/Optimiam/backend/src/main/java/fr/trollgun/optimiam/stock/domain/StockItemRepository.java)
- [`StockService.java`](file:///var/home/trollgun/IdeaProjects/Optimiam/backend/src/main/java/fr/trollgun/optimiam/stock/application/StockService.java)

---

## ✅ 5. Validation

- Requêtes directes `GET /api/v1/recipes` et `GET /api/v1/stock` exécutées avec succès contre l'instance PostgreSQL réelle (réponse HTTP 200 OK avec le catalogue complet).
- Les 53 tests unitaires et d'intégration Spring Boot (`./mvnw test`) s'exécutent avec 0 échec et 0 erreur.
