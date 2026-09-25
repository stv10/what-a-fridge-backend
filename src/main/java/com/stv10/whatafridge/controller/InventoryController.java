package com.stv10.whatafridge.controller;

import com.stv10.whatafridge.domain.InventoryItem;
import com.stv10.whatafridge.dto.FoodDto;
import com.stv10.whatafridge.service.CurrentUserService;
import com.stv10.whatafridge.service.InventoryService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {
    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;

    public InventoryController(InventoryService inventoryService, CurrentUserService currentUserService) {
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<InventoryItem> getInventory() {
        return inventoryService.getUserInventory(currentUserService.getCurrentUserId());
    }

    public record AddItemRequest(FoodDto food, Double quantity, LocalDate expirationDate) {}

    @PostMapping
    public InventoryItem addItem(@RequestBody AddItemRequest request) {
        return inventoryService.addItem(
            currentUserService.getCurrentUserId(), 
            request.food(), 
            request.quantity(), 
            request.expirationDate()
        );
    }

    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(currentUserService.getCurrentUserId(), id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
