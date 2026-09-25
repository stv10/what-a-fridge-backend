package com.stv10.whatafridge.controller;

import com.stv10.whatafridge.dto.FoodDto;
import com.stv10.whatafridge.service.OpenFoodFactsService;
import com.stv10.whatafridge.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*")
public class FoodController {
    private final OpenFoodFactsService foodService;
    private final CurrentUserService currentUserService;

    public FoodController(OpenFoodFactsService foodService, CurrentUserService currentUserService) {
        this.foodService = foodService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/search")
    public List<FoodDto> search(@RequestParam String query) {
        UUID userId = null;
        try {
            userId = currentUserService.getCurrentUserId();
        } catch (Exception ignored) {
        }
        return foodService.searchByText(query, userId);
    }

    @GetMapping("/barcode/{barcode}")
    public FoodDto getByBarcode(@PathVariable String barcode) {
        return foodService.fetchByBarcode(barcode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found in Open Food Facts"));
    }
}
