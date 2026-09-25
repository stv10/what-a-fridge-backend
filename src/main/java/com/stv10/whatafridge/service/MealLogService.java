package com.stv10.whatafridge.service;

import com.stv10.whatafridge.domain.Food;
import com.stv10.whatafridge.domain.MealLog;
import com.stv10.whatafridge.domain.User;
import com.stv10.whatafridge.domain.UserMeal;
import com.stv10.whatafridge.dto.FoodDto;
import com.stv10.whatafridge.dto.MealLogDto;
import com.stv10.whatafridge.repository.MealLogRepository;
import com.stv10.whatafridge.repository.UserMealRepository;
import com.stv10.whatafridge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MealLogService {
    private final MealLogRepository mealLogRepo;
    private final UserRepository userRepo;
    private final UserMealRepository userMealRepo;
    private final OpenFoodFactsService foodService;

    public MealLogService(MealLogRepository mealLogRepo,
                          UserRepository userRepo,
                          UserMealRepository userMealRepo,
                          OpenFoodFactsService foodService) {
        this.mealLogRepo = mealLogRepo;
        this.userRepo = userRepo;
        this.userMealRepo = userMealRepo;
        this.foodService = foodService;
    }

    @Transactional(readOnly = true)
    public List<MealLogDto> getDailyLogs(UUID userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<MealLog> logs = mealLogRepo.findByUserIdAndConsumedAtBetween(userId, start, end);
        return logs.stream()
                .map(MealLogDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MealLogDto logMeal(UUID userId, FoodDto foodDto, Double quantity, String mealName, Long userMealId, LocalDateTime consumedAt) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Food food = foodService.getOrCreateFromDto(foodDto, user);

        UserMeal userMeal = null;
        if (userMealId != null) {
            userMeal = userMealRepo.findByUserIdAndId(userId, userMealId).orElse(null);
        }

        if (userMeal == null && mealName != null && !mealName.isBlank()) {
            Optional<UserMeal> existing = userMealRepo.findByUserIdAndNameIgnoreCase(userId, mealName.trim());
            if (existing.isPresent()) {
                userMeal = existing.get();
            } else {
                List<UserMeal> current = userMealRepo.findByUserIdOrderByDisplayOrderAsc(userId);
                int nextOrder = current.stream().mapToInt(UserMeal::getDisplayOrder).max().orElse(-1) + 1;
                userMeal = userMealRepo.save(UserMeal.builder()
                        .user(user)
                        .name(mealName.trim())
                        .displayOrder(nextOrder)
                        .build());
            }
        }

        LocalDateTime timestamp = (consumedAt != null) ? consumedAt : LocalDateTime.now();

        MealLog log = MealLog.builder()
                .user(user)
                .food(food)
                .quantity(quantity)
                .userMeal(userMeal)
                .consumedAt(timestamp)
                .build();

        MealLog saved = mealLogRepo.save(log);
        return MealLogDto.fromEntity(saved);
    }

    @Transactional
    public void deleteMealLog(UUID userId, Long id) {
        MealLog log = mealLogRepo.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new IllegalArgumentException("Meal log not found: " + id));
        mealLogRepo.delete(log);
    }
}
