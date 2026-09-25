package com.stv10.whatafridge.repository;

import com.stv10.whatafridge.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByUserId(UUID userId);
    Optional<InventoryItem> findByIdAndUserId(Long id, UUID userId);
    void deleteByIdAndUserId(Long id, UUID userId);
}
