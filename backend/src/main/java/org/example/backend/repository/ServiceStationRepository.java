package org.example.backend.repository;

import org.example.backend.entity.ServiceStation;
import org.example.backend.enums.ServiceStationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceStationRepository extends JpaRepository<ServiceStation, Long> {

    List<ServiceStation> findByType(ServiceStationType type);

    List<ServiceStation> findByLocationId(Long locationId);
}
