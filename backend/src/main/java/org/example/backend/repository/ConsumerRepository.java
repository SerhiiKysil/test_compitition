package org.example.backend.repository;

import org.example.backend.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, Long> {

    List<Consumer> findByLocationId(Long locationId);
}
