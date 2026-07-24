package dev.codelitz.context.servicecatalog.repository;

import dev.codelitz.context.servicecatalog.model.EnvironmentEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<EnvironmentEntity, UUID> {
    Optional<EnvironmentEntity> findByNameIgnoreCase(String name);
    List<EnvironmentEntity> findAllByOrderByNameAsc();
}
