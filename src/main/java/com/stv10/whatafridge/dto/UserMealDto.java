package com.stv10.whatafridge.dto;

import com.stv10.whatafridge.domain.UserMeal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMealDto {
    private Long id;
    private String name;
    private Integer displayOrder;

    public static UserMealDto fromEntity(UserMeal userMeal) {
        if (userMeal == null) return null;
        return UserMealDto.builder()
                .id(userMeal.getId())
                .name(userMeal.getName())
                .displayOrder(userMeal.getDisplayOrder())
                .build();
    }
}
