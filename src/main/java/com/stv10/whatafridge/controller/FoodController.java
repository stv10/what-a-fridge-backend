package com.stv10.whatafridge.controller;

import com.stv10.whatafridge.dto.FoodDto;
import com.stv10.whatafridge.service.OpenFoodFactsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*")
public class FoodController {
    private final OpenFoodFactsService foodService;

    public FoodController(OpenFoodFactsService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/search")
    public List<FoodDto> search(@RequestParam String query) {
        return foodService.searchByText(query);
    }

    @GetMapping("/barcode/{barcode}")
    public FoodDto getByBarcode(@PathVariable String barcode) {
        return foodService.fetchByBarcode(barcode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found in Open Food Facts"));
    }
}
