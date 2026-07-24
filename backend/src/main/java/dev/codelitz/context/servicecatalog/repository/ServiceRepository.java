package dev.codelitz.context.servicecatalog.repository;

import dev.codelitz.context.servicecatalog.model.ServiceEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    @Query("select distinct s from ServiceEntity s left join s.owners o where " +
        "lower(s.name) like lower(concat('%', :query, '%')) or lower(s.description) like lower(concat('%', :query, '%')) " +
        "or lower(o) like lower(concat('%', :query, '%'))")
    Page<ServiceEntity> search(String query, Pageable pageable);
    java.util.Optional<ServiceEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    boolean existsByTeams_Id(UUID teamId);
}
