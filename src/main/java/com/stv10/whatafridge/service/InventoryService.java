package com.stv10.whatafridge.service;

import com.stv10.whatafridge.domain.InventoryItem;
import com.stv10.whatafridge.domain.User;
import com.stv10.whatafridge.domain.Food;
import com.stv10.whatafridge.dto.FoodDto;
import com.stv10.whatafridge.repository.InventoryItemRepository;
import com.stv10.whatafridge.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {
    private final InventoryItemRepository inventoryRepo;
    private final UserRepository userRepo;
    private final OpenFoodFactsService foodService;

    public InventoryService(InventoryItemRepository inventoryRepo, UserRepository userRepo, OpenFoodFactsService foodService) {
        this.inventoryRepo = inventoryRepo;
        this.userRepo = userRepo;
        this.foodService = foodService;
    }

    public List<InventoryItem> getUserInventory(UUID userId) {
        return inventoryRepo.findByUserId(userId);
    }

    public InventoryItem addItem(UUID userId, FoodDto foodDto, Double quantity, LocalDate expirationDate) {
        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Food food = foodService.getOrCreateFromDto(foodDto);

        InventoryItem item = InventoryItem.builder()
                .user(user)
                .food(food)
                .quantity(quantity)
                .expirationDate(expirationDate)
                .build();
        return inventoryRepo.save(item);
    }
    
    public void deleteItem(Long itemId) {
        inventoryRepo.deleteById(itemId);
    }
}
