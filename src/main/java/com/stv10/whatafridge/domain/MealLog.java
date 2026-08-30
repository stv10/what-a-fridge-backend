package com.stv10.whatafridge.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_meal_id")
    private UserMeal userMeal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "food_id")
    private Food food;

    // Consumed quantity in grams/ml
    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private LocalDateTime consumedAt;
}
