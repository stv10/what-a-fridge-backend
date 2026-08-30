package com.stv10.whatafridge;

import com.stv10.whatafridge.domain.User;
import com.stv10.whatafridge.dto.FoodDto;
import com.stv10.whatafridge.dto.MealLogDto;
import com.stv10.whatafridge.dto.UserMealDto;
import com.stv10.whatafridge.repository.UserRepository;
import com.stv10.whatafridge.service.MealLogService;
import com.stv10.whatafridge.service.OpenFoodFactsService;
import com.stv10.whatafridge.service.UserMealService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WhatafridgeApplicationTests {

	@Autowired
	private OpenFoodFactsService openFoodFactsService;

	@Autowired
	private UserMealService userMealService;

	@Autowired
	private MealLogService mealLogService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void testBarcodeFetchSuccess() {
		// Using a standard known barcode (e.g. 737628064502 - Rice Noodles)
		String barcode = "737628064502";
		Optional<FoodDto> resultOpt = openFoodFactsService.fetchByBarcode(barcode);

		assertTrue(resultOpt.isPresent(), "Should fetch the product successfully");
		FoodDto dto = resultOpt.get();
		assertEquals(barcode, dto.getOpenFoodFactsId());
		assertNotNull(dto.getName());
		assertNotNull(dto.getCalories());
		assertFalse(dto.getIsCustom());

		// Verify database cache (fetching again should hit cache, which returns the same data quickly)
		Optional<FoodDto> cachedResultOpt = openFoodFactsService.fetchByBarcode(barcode);
		assertTrue(cachedResultOpt.isPresent());
		assertEquals(dto.getName(), cachedResultOpt.get().getName());
	}

	@Test
	void testBarcodeFetchNotFound() {
		// Using an invalid barcode that should not exist in OFF
		String barcode = "999999999999999";
		Optional<FoodDto> resultOpt = openFoodFactsService.fetchByBarcode(barcode);

		assertFalse(resultOpt.isPresent(), "Should return empty for non-existent barcode");
	}

	@Test
	@Transactional
	void testUserMealServiceAndMealLogFlow() {
		// Create a test user
		User testUser = userRepository.save(User.builder()
				.username("meal_tester_" + UUID.randomUUID())
				.password("secret123")
				.build());

		// Test lazy seeding of default meals
		List<UserMealDto> meals = userMealService.getUserMeals(testUser.getId());
		assertNotNull(meals);
		assertEquals(4, meals.size());
		assertEquals("Desayuno", meals.get(0).getName());

		// Test adding a custom meal category
		UserMealDto snack = userMealService.addMeal(testUser.getId(), "Colación Nocturna", null);
		assertNotNull(snack.getId());
		assertEquals("Colación Nocturna", snack.getName());

		// Test logging a food item
		FoodDto food = FoodDto.builder()
				.name("Manzana Roja")
				.calories(52.0)
				.protein(0.3)
				.carbohydrates(14.0)
				.fat(0.2)
				.fiber(2.4)
				.sugar(10.0)
				.sodium(1.0)
				.saturatedFat(0.0)
				.isCustom(true)
				.build();

		MealLogDto log = mealLogService.logMeal(
				testUser.getId(),
				food,
				150.0,
				"Desayuno",
				null,
				LocalDateTime.now()
		);

		assertNotNull(log.getId());
		assertEquals("Desayuno", log.getMeal());
		assertEquals(150.0, log.getQuantity());
		assertEquals("Manzana Roja", log.getFood().getName());
		assertEquals(2.4, log.getFood().getFiber());

		// Retrieve daily logs
		List<MealLogDto> dailyLogs = mealLogService.getDailyLogs(testUser.getId(), LocalDate.now());
		assertFalse(dailyLogs.isEmpty());
		assertEquals(1, dailyLogs.size());

		// Delete log
		mealLogService.deleteMealLog(testUser.getId(), log.getId());
		List<MealLogDto> logsAfterDelete = mealLogService.getDailyLogs(testUser.getId(), LocalDate.now());
		assertTrue(logsAfterDelete.isEmpty());
	}
}
