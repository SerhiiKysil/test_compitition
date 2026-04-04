package org.example.backend.repository;

import org.example.backend.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByDeliveryOrderIdOrderByStopOrderAsc(Long deliveryOrderId);
}
