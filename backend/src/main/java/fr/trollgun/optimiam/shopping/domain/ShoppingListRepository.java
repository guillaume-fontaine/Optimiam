package fr.trollgun.optimiam.shopping.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID> {

    List<ShoppingList> findByStatusOrderByCreatedAtDesc(ShoppingListStatus status);

    Optional<ShoppingList> findFirstByStatusOrderByCreatedAtDesc(ShoppingListStatus status);
}
