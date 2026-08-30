package com.stv10.whatafridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stv10.whatafridge.config.OpenFoodFactsProperties;
import com.stv10.whatafridge.domain.Food;
import com.stv10.whatafridge.dto.FoodDto;
import com.stv10.whatafridge.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class OpenFoodFactsService {

    private final RestClient restClient;
    private final FoodRepository foodRepository;
    private final OpenFoodFactsProperties properties;
    private final ObjectMapper objectMapper;

    public OpenFoodFactsService(FoodRepository foodRepository, OpenFoodFactsProperties properties) {
        this.foodRepository = foodRepository;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();

        if (properties.getContactEmail() == null || properties.getContactEmail().isBlank()) {
            throw new IllegalStateException("OpenFoodFacts contact email (openfoodfacts.contact-email) must be configured.");
        }

        String userAgent = String.format("%s/%s (%s)",
                properties.getAppName(),
                properties.getAppVersion(),
                properties.getContactEmail()
        );

        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    public List<FoodDto> searchByText(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        
        List<Food> localFoods = foodRepository.findByNameContainingIgnoreCase(query);
        List<FoodDto> results = new ArrayList<>();
        for (Food food : localFoods) {
            results.add(FoodDto.fromEntity(food));
        }
        return results;
    }

    public Optional<FoodDto> fetchByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return Optional.empty();
        }

        // 1. Check local database cache
        Optional<Food> cached = foodRepository.findByOpenFoodFactsId(barcode);
        if (cached.isPresent()) {
            return Optional.of(FoodDto.fromEntity(cached.get()));
        }

        // 2. Fetch from Open Food Facts API v3
        try {
            String url = String.format("%s/api/v3/product/%s.json?fields=code,product_name,nutriments",
                    properties.getBaseUrl(), barcode);

            String responseStr = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            if (responseStr != null) {
                JsonNode response = objectMapper.readTree(responseStr);
                if (response.has("product") && !response.get("product").isNull()) {
                    JsonNode product = response.get("product");
                    String productName = product.has("product_name") ? product.get("product_name").asText() : "Unknown Product";
                    JsonNode nutriments = product.get("nutriments");

                    double calories = 0.0;
                    double protein = 0.0;
                    double carbs = 0.0;
                    double fat = 0.0;
                    Double fiber = null;
                    Double sugar = null;
                    Double sodium = null;
                    Double saturatedFat = null;

                    if (nutriments != null) {
                        calories = nutriments.has("energy-kcal_100g") ? nutriments.get("energy-kcal_100g").asDouble() : 0.0;
                        protein = nutriments.has("proteins_100g") ? nutriments.get("proteins_100g").asDouble() : 0.0;
                        carbs = nutriments.has("carbohydrates_100g") ? nutriments.get("carbohydrates_100g").asDouble() : 0.0;
                        fat = nutriments.has("fat_100g") ? nutriments.get("fat_100g").asDouble() : 0.0;

                        if (nutriments.has("fiber_100g")) fiber = nutriments.get("fiber_100g").asDouble();
                        if (nutriments.has("sugars_100g")) sugar = nutriments.get("sugars_100g").asDouble();
                        // sodium is in grams per 100g in OFF, convert to mg
                        if (nutriments.has("sodium_100g")) sodium = nutriments.get("sodium_100g").asDouble() * 1000.0;
                        if (nutriments.has("saturated-fat_100g")) saturatedFat = nutriments.get("saturated-fat_100g").asDouble();
                    }

                    // Save to local cache database
                    Food saved = foodRepository.save(Food.builder()
                            .openFoodFactsId(barcode)
                            .name(productName)
                            .calories(calories)
                            .protein(protein)
                            .carbohydrates(carbs)
                            .fat(fat)
                            .fiber(fiber)
                            .sugar(sugar)
                            .sodium(sodium)
                            .saturatedFat(saturatedFat)
                            .isCustom(false)
                            .build());

                    return Optional.of(FoodDto.fromEntity(saved));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Food getOrCreateFromDto(FoodDto dto) {
        if (dto.getOpenFoodFactsId() == null) {
            // It's a custom food, just save it
            return foodRepository.save(Food.builder()
                    .name(dto.getName())
                    .calories(dto.getCalories() != null ? dto.getCalories() : 0.0)
                    .protein(dto.getProtein() != null ? dto.getProtein() : 0.0)
                    .carbohydrates(dto.getCarbohydrates() != null ? dto.getCarbohydrates() : 0.0)
                    .fat(dto.getFat() != null ? dto.getFat() : 0.0)
                    .fiber(dto.getFiber())
                    .sugar(dto.getSugar())
                    .sodium(dto.getSodium())
                    .saturatedFat(dto.getSaturatedFat())
                    .isCustom(true)
                    .build());
        }

        Optional<Food> existing = foodRepository.findByOpenFoodFactsId(dto.getOpenFoodFactsId());
        return existing.orElseGet(() -> foodRepository.save(Food.builder()
                .openFoodFactsId(dto.getOpenFoodFactsId())
                .name(dto.getName())
                .calories(dto.getCalories() != null ? dto.getCalories() : 0.0)
                .protein(dto.getProtein() != null ? dto.getProtein() : 0.0)
                .carbohydrates(dto.getCarbohydrates() != null ? dto.getCarbohydrates() : 0.0)
                .fat(dto.getFat() != null ? dto.getFat() : 0.0)
                .fiber(dto.getFiber())
                .sugar(dto.getSugar())
                .sodium(dto.getSodium())
                .saturatedFat(dto.getSaturatedFat())
                .isCustom(dto.getIsCustom() != null ? dto.getIsCustom() : false)
                .build()));
    }
}
