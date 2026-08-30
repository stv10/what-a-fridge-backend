package com.stv10.whatafridge.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "food_id")
    private Food food;

    // Quantity in grams/ml
    @Column(nullable = false)
    private Double quantity;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;
}
