package fr.trollgun.optimiam.shopping.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shopping_lists")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ShoppingListStatus status = ShoppingListStatus.ACTIVE;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ShoppingListItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public void addItem(ShoppingListItem item) {
        items.add(item);
        item.setShoppingList(this);
    }

    public void removeItem(ShoppingListItem item) {
        try {
            items.remove(item);
        } catch (UnsupportedOperationException exception) {
            items = new ArrayList<>(items);
            items.remove(item);
        }
        item.setShoppingList(null);
    }
}
