package org.example.backend.repository;

import org.example.backend.entity.Transport;
import org.example.backend.enums.TransportStatus;
import org.example.backend.enums.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {

    List<Transport> findByStatus(TransportStatus status);

    List<Transport> findByType(TransportType type);

    List<Transport> findByStatusAndType(TransportStatus status, TransportType type);

    List<Transport> findByCurrentLocationId(Long locationId);

    List<Transport> findByStatusAndMaxCargoWeightTonsGreaterThanEqual(TransportStatus status, Double weight);
}
