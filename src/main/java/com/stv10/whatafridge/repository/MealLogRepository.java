package com.stv10.whatafridge.repository;

import com.stv10.whatafridge.domain.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    List<MealLog> findByUserIdAndConsumedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);
    Optional<MealLog> findByUserIdAndId(UUID userId, Long id);

    @Modifying
    @Query("UPDATE MealLog m SET m.userMeal = null WHERE m.userMeal.id = :userMealId")
    void detachUserMeal(@Param("userMealId") Long userMealId);
}
