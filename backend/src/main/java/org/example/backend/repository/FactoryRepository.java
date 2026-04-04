package org.example.backend.repository;

import org.example.backend.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {

    List<Factory> findByLocationId(Long locationId);
}
