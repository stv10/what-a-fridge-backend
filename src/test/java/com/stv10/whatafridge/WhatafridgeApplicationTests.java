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

	@Autowired
	private com.stv10.whatafridge.service.InventoryService inventoryService;

	@Autowired
	private com.stv10.whatafridge.controller.AuthController authController;

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

	@Test
	@Transactional
	void testUserRegistrationAndDefaultMealsSeeding() {
		String testUsername = "new_user_" + UUID.randomUUID();
		com.stv10.whatafridge.dto.RegisterRequest req = new com.stv10.whatafridge.dto.RegisterRequest(testUsername, "password123");
		
		org.springframework.http.ResponseEntity<?> res = authController.register(req);
		assertEquals(org.springframework.http.HttpStatus.CREATED, res.getStatusCode());
		assertNotNull(res.getBody());

		// Verify duplicate registration rejection (409 Conflict)
		org.springframework.http.ResponseEntity<?> dupRes = authController.register(req);
		assertEquals(org.springframework.http.HttpStatus.CONFLICT, dupRes.getStatusCode());

		// Verify default meals were auto-seeded for this user
		User createdUser = userRepository.findByUsername(testUsername).orElseThrow();
		List<UserMealDto> meals = userMealService.getUserMeals(createdUser.getId());
		assertEquals(4, meals.size());
	}

	@Test
	@Transactional
	void testMultiTenantInventoryIsolationAndAntiIdor() {
		User userA = userRepository.save(User.builder()
				.username("tenant_a_" + UUID.randomUUID())
				.password("secret123")
				.build());

		User userB = userRepository.save(User.builder()
				.username("tenant_b_" + UUID.randomUUID())
				.password("secret123")
				.build());

		FoodDto foodA = FoodDto.builder().name("Yogurt A").calories(60.0).protein(3.0).carbohydrates(5.0).fat(1.0).isCustom(true).build();
		FoodDto foodB = FoodDto.builder().name("Queso B").calories(100.0).protein(7.0).carbohydrates(1.0).fat(8.0).isCustom(true).build();

		var itemA = inventoryService.addItem(userA.getId(), foodA, 200.0, LocalDate.now().plusDays(5));
		var itemB = inventoryService.addItem(userB.getId(), foodB, 150.0, LocalDate.now().plusDays(10));

		// Verify user A only sees item A
		var userAInventory = inventoryService.getUserInventory(userA.getId());
		assertEquals(1, userAInventory.size());
		assertEquals("Yogurt A", userAInventory.get(0).getFood().getName());

		// Verify user B only sees item B
		var userBInventory = inventoryService.getUserInventory(userB.getId());
		assertEquals(1, userBInventory.size());
		assertEquals("Queso B", userBInventory.get(0).getFood().getName());

		// User B attempts to delete User A's item -> must throw IllegalArgumentException (anti-IDOR)
		assertThrows(IllegalArgumentException.class, () -> {
			inventoryService.deleteItem(userB.getId(), itemA.getId());
		});

		// User A deleting own item succeeds
		assertDoesNotThrow(() -> {
			inventoryService.deleteItem(userA.getId(), itemA.getId());
		});
		assertTrue(inventoryService.getUserInventory(userA.getId()).isEmpty());
	}

	@Test
	@Transactional
	void testCustomFoodIsolationBetweenTenants() {
		User userA = userRepository.save(User.builder()
				.username("chef_a_" + UUID.randomUUID())
				.password("secret123")
				.build());

		User userB = userRepository.save(User.builder()
				.username("chef_b_" + UUID.randomUUID())
				.password("secret123")
				.build());

		String customFoodName = "Receta Secreta " + UUID.randomUUID();
		FoodDto customFood = FoodDto.builder()
				.name(customFoodName)
				.calories(250.0)
				.protein(12.0)
				.carbohydrates(20.0)
				.fat(10.0)
				.isCustom(true)
				.build();

		// User A logs this custom food
		mealLogService.logMeal(userA.getId(), customFood, 100.0, "Almuerzo", null, LocalDateTime.now());

		// User B searches -> must NOT find User A's custom food
		List<FoodDto> userBSearch = openFoodFactsService.searchByText(customFoodName, userB.getId());
		assertTrue(userBSearch.isEmpty(), "User B should not see User A's custom food");

		// User A searches -> must find User A's custom food
		List<FoodDto> userASearch = openFoodFactsService.searchByText(customFoodName, userA.getId());
		assertEquals(1, userASearch.size());
		assertEquals(customFoodName, userASearch.get(0).getName());
	}
}
