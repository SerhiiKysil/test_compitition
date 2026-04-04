package org.example.backend.repository;

import org.example.backend.entity.DeliveryOrder;
import org.example.backend.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {

    Optional<DeliveryOrder> findByOrderNumber(String orderNumber);

    List<DeliveryOrder> findByStatus(DeliveryStatus status);

    List<DeliveryOrder> findByContractId(Long contractId);

    List<DeliveryOrder> findByTransportId(Long transportId);

    List<DeliveryOrder> findByFactoryId(Long factoryId);

    List<DeliveryOrder> findByConsumerId(Long consumerId);
}
