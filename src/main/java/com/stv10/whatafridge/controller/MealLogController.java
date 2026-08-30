package com.stv10.whatafridge.controller;

import com.stv10.whatafridge.dto.FoodDto;
import com.stv10.whatafridge.dto.MealLogDto;
import com.stv10.whatafridge.dto.UserMealDto;
import com.stv10.whatafridge.service.CurrentUserService;
import com.stv10.whatafridge.service.MealLogService;
import com.stv10.whatafridge.service.UserMealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
@CrossOrigin(origins = "*")
public class MealLogController {
    private final MealLogService mealLogService;
    private final UserMealService userMealService;
    private final CurrentUserService currentUserService;

    public MealLogController(MealLogService mealLogService,
            UserMealService userMealService,
            CurrentUserService currentUserService) {
        this.mealLogService = mealLogService;
        this.userMealService = userMealService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<MealLogDto> getDailyLogs(@RequestParam String date) {
        return mealLogService.getDailyLogs(
                currentUserService.getCurrentUserId(),
                LocalDate.parse(date));
    }

    public record LogMealRequest(
            FoodDto food,
            Double quantity,
            String mealName,
            Long userMealId,
            LocalDateTime consumedAt) {
    }

    @PostMapping
    public MealLogDto logMeal(@RequestBody LogMealRequest request) {
        return mealLogService.logMeal(
                currentUserService.getCurrentUserId(),
                request.food(),
                request.quantity(),
                request.mealName(),
                request.userMealId(),
                request.consumedAt());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMealLog(@PathVariable Long id) {
        mealLogService.deleteMealLog(currentUserService.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    // --- Meal Categories Configuration Endpoints ---

    @GetMapping("/config")
    public List<UserMealDto> getMealConfig() {
        return userMealService.getUserMeals(currentUserService.getCurrentUserId());
    }

    public record CreateUserMealRequest(String name, Integer displayOrder) {
    }

    @PostMapping("/config")
    public UserMealDto createMealConfig(@RequestBody CreateUserMealRequest request) {
        return userMealService.addMeal(
                currentUserService.getCurrentUserId(),
                request.name(),
                request.displayOrder());
    }

    public record UpdateUserMealRequest(String name, Integer displayOrder) {
    }

    @PutMapping("/config/{id}")
    public UserMealDto updateMealConfig(@PathVariable Long id, @RequestBody UpdateUserMealRequest request) {
        return userMealService.updateMeal(
                currentUserService.getCurrentUserId(),
                id,
                request.name(),
                request.displayOrder());
    }

    @DeleteMapping("/config/{id}")
    public ResponseEntity<Void> deleteMealConfig(@PathVariable Long id) {
        userMealService.deleteMeal(currentUserService.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    public record ReorderMealsRequest(List<Long> orderedMealIds) {
    }

    @PutMapping("/config/reorder")
    public List<UserMealDto> reorderMealConfigs(@RequestBody ReorderMealsRequest request) {
        return userMealService.reorderMeals(
                currentUserService.getCurrentUserId(),
                request.orderedMealIds());
    }
}
