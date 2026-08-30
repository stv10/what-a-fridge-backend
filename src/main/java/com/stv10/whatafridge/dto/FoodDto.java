package com.stv10.whatafridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.stv10.whatafridge.domain.Food;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDto {
    private String openFoodFactsId;
    private String name;
    private Double calories;
    private Double protein;
    private Double carbohydrates;
    private Double fat;
    private Double fiber;
    private Double sugar;
    private Double sodium;
    private Double saturatedFat;
    private Boolean isCustom;

    public static FoodDto fromEntity(Food food) {
        if (food == null) return null;
        return FoodDto.builder()
                .openFoodFactsId(food.getOpenFoodFactsId())
                .name(food.getName())
                .calories(food.getCalories())
                .protein(food.getProtein())
                .carbohydrates(food.getCarbohydrates())
                .fat(food.getFat())
                .fiber(food.getFiber())
                .sugar(food.getSugar())
                .sodium(food.getSodium())
                .saturatedFat(food.getSaturatedFat())
                .isCustom(food.isCustom())
                .build();
    }

}
