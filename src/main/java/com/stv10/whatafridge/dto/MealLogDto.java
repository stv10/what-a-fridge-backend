package com.stv10.whatafridge.dto;

import com.stv10.whatafridge.domain.MealLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealLogDto {
    private Long id;
    private String meal;
    private Long userMealId;
    private Double quantity;
    private LocalDateTime consumedAt;
    private FoodDto food;

    public static MealLogDto fromEntity(MealLog log) {
        if (log == null) return null;
        return MealLogDto.builder()
                .id(log.getId())
                .meal(log.getUserMeal() != null ? log.getUserMeal().getName() : "Otros")
                .userMealId(log.getUserMeal() != null ? log.getUserMeal().getId() : null)
                .quantity(log.getQuantity())
                .consumedAt(log.getConsumedAt())
                .food(FoodDto.fromEntity(log.getFood()))
                .build();
    }
}
