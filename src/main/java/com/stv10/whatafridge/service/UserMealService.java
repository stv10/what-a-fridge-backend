package com.stv10.whatafridge.service;

import com.stv10.whatafridge.domain.User;
import com.stv10.whatafridge.domain.UserMeal;
import com.stv10.whatafridge.dto.UserMealDto;
import com.stv10.whatafridge.repository.MealLogRepository;
import com.stv10.whatafridge.repository.UserMealRepository;
import com.stv10.whatafridge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserMealService {

    private static final String[] DEFAULT_MEAL_NAMES = {"Desayuno", "Almuerzo", "Merienda", "Cena"};

    private final UserMealRepository userMealRepository;
    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;

    public UserMealService(UserMealRepository userMealRepository, UserRepository userRepository, MealLogRepository mealLogRepository) {
        this.userMealRepository = userMealRepository;
        this.userRepository = userRepository;
        this.mealLogRepository = mealLogRepository;
    }

    @Transactional
    public List<UserMeal> seedDefaultMeals(User user) {
        List<UserMeal> existing = userMealRepository.findByUserIdOrderByDisplayOrderAsc(user.getId());
        if (!existing.isEmpty()) {
            return existing;
        }

        List<UserMeal> created = new ArrayList<>();
        for (int i = 0; i < DEFAULT_MEAL_NAMES.length; i++) {
            UserMeal meal = UserMeal.builder()
                    .user(user)
                    .name(DEFAULT_MEAL_NAMES[i])
                    .displayOrder(i)
                    .build();
            created.add(userMealRepository.save(meal));
        }
        return created;
    }

    @Transactional
    public List<UserMealDto> getUserMeals(UUID userId) {
        List<UserMeal> meals = userMealRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        if (meals.isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            meals = seedDefaultMeals(user);
        }
        return meals.stream()
                .map(UserMealDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserMealDto addMeal(UUID userId, String name, Integer displayOrder) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        int order = (displayOrder != null) ? displayOrder :
                userMealRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                        .mapToInt(UserMeal::getDisplayOrder)
                        .max()
                        .orElse(-1) + 1;

        UserMeal meal = UserMeal.builder()
                .user(user)
                .name(name.trim())
                .displayOrder(order)
                .build();

        return UserMealDto.fromEntity(userMealRepository.save(meal));
    }

    @Transactional
    public UserMealDto updateMeal(UUID userId, Long mealId, String name, Integer displayOrder) {
        UserMeal meal = userMealRepository.findByUserIdAndId(userId, mealId)
                .orElseThrow(() -> new IllegalArgumentException("Meal category not found: " + mealId));

        if (name != null && !name.isBlank()) {
            meal.setName(name.trim());
        }
        if (displayOrder != null) {
            meal.setDisplayOrder(displayOrder);
        }

        return UserMealDto.fromEntity(userMealRepository.save(meal));
    }

    @Transactional
    public void deleteMeal(UUID userId, Long mealId) {
        UserMeal meal = userMealRepository.findByUserIdAndId(userId, mealId)
                .orElseThrow(() -> new IllegalArgumentException("Meal category not found: " + mealId));

        // Detach existing logs referencing this category so historical data isn't deleted
        mealLogRepository.detachUserMeal(meal.getId());
        userMealRepository.delete(meal);
    }

    @Transactional
    public List<UserMealDto> reorderMeals(UUID userId, List<Long> orderedMealIds) {
        for (int i = 0; i < orderedMealIds.size(); i++) {
            Long mealId = orderedMealIds.get(i);
            UserMeal meal = userMealRepository.findByUserIdAndId(userId, mealId).orElse(null);
            if (meal != null) {
                meal.setDisplayOrder(i);
                userMealRepository.save(meal);
            }
        }
        return getUserMeals(userId);
    }
}
