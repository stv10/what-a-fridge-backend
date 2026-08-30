package com.stv10.whatafridge.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The Open Food Facts ID (barcode) or null if custom food
    @Column(name = "open_food_facts_id", unique = true)
    private String openFoodFactsId;

    @Column(nullable = false)
    private String name;

    // Macros are generally per 100g or 100ml
    @Column(nullable = false)
    private Double calories;

    @Column(nullable = false)
    private Double protein;

    @Column(nullable = false)
    private Double carbohydrates;

    @Column(nullable = false)
    private Double fat;

    @Column
    private Double fiber;

    @Column
    private Double sugar;

    @Column
    private Double sodium;

    @Column(name = "saturated_fat")
    private Double saturatedFat;

    @Column(nullable = false)
    private boolean isCustom;
}
