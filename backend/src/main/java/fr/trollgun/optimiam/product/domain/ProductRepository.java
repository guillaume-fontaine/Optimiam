package fr.trollgun.optimiam.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByDeletedFalseAndCategoryId(UUID categoryId, Pageable pageable);

    Page<Product> findByDeletedFalseAndNameContainingIgnoreCase(String query, Pageable pageable);

    Page<Product> findByDeletedFalseAndCategoryIdAndNameContainingIgnoreCase(UUID categoryId, String query, Pageable pageable);

    Optional<Product> findByIdAndDeletedFalse(UUID id);

    Optional<Product> findByBarcodeAndDeletedFalse(String barcode);

    List<Product> findByDeletedFalse();
}
