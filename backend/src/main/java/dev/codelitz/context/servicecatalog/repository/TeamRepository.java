package dev.codelitz.context.servicecatalog.repository;

import dev.codelitz.context.servicecatalog.model.TeamEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
    boolean existsByNameIgnoreCase(String name);
    Optional<TeamEntity> findByNameIgnoreCase(String name);
    List<TeamEntity> findAllByOrderByNameAsc();
}
