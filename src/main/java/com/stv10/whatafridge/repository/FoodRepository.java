package com.stv10.whatafridge.repository;

import com.stv10.whatafridge.domain.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    Optional<Food> findByOpenFoodFactsId(String openFoodFactsId);
    List<Food> findByNameContainingIgnoreCase(String name);
    List<Food> findByNameContainingIgnoreCaseAndUserIsNull(String name);

    @Query("SELECT f FROM Food f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%')) AND (f.user IS NULL OR f.user.id = :userId)")
    List<Food> searchByNameAndUser(@Param("name") String name, @Param("userId") UUID userId);
}

