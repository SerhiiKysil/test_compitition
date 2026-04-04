package org.example.backend.repository;

import org.example.backend.entity.Contract;
import org.example.backend.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByContractNumber(String contractNumber);

    List<Contract> findByStatus(ContractStatus status);

    List<Contract> findByFactoryId(Long factoryId);

    List<Contract> findByConsumerId(Long consumerId);
}
