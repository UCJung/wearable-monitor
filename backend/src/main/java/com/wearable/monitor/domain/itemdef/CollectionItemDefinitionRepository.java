package com.wearable.monitor.domain.itemdef;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionItemDefinitionRepository extends JpaRepository<CollectionItemDefinition, Long> {

    List<CollectionItemDefinition> findByIsActiveTrueOrderByDisplayOrder();

    Optional<CollectionItemDefinition> findByItemCode(String itemCode);
}
