package org.example.backend.repository;

import org.example.backend.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByCountryIgnoreCase(String country);

    List<Location> findByHasPortTrue();

    List<Location> findByHasAirportTrue();

    List<Location> findByHasRailTerminalTrue();
}
