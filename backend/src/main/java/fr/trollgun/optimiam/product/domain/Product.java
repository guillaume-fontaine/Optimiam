package fr.trollgun.optimiam.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;
import fr.trollgun.optimiam.stock.domain.Location;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unit defaultUnit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Durée moyenne de conservation en jours
     */
    private Integer averageShelfLifeDays;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_location", length = 50)
    private Location defaultLocation;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;
}
