package org.example.backend.service;

import org.example.backend.dto.ServiceStationRequest;
import org.example.backend.entity.Location;
import org.example.backend.entity.ServiceStation;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.LocationRepository;
import org.example.backend.repository.ServiceStationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServiceStationService {

    private final ServiceStationRepository repository;
    private final LocationRepository locationRepository;

    public ServiceStationService(ServiceStationRepository repository,
                                 LocationRepository locationRepository) {
        this.repository = repository;
        this.locationRepository = locationRepository;
    }

    public List<ServiceStation> getAll() { return repository.findAll(); }

    public ServiceStation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceStation", id));
    }

    @Transactional
    public ServiceStation create(ServiceStationRequest req) {
        Location loc = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        return repository.save(ServiceStation.builder()
                .name(req.name()).location(loc).type(req.type()).fuelTypes(req.fuelTypes())
                .pricePerLiter(req.pricePerLiter())
                .hasHeavyLift(req.hasHeavyLift() != null ? req.hasHeavyLift() : false)
                .maxRepairWeightTons(req.maxRepairWeightTons())
                .description(req.description()).contactPhone(req.contactPhone())
                .build());
    }

    @Transactional
    public ServiceStation update(Long id, ServiceStationRequest req) {
        ServiceStation s = getById(id);
        Location loc = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        s.setName(req.name()); s.setLocation(loc); s.setType(req.type());
        s.setFuelTypes(req.fuelTypes()); s.setPricePerLiter(req.pricePerLiter());
        if (req.hasHeavyLift() != null) s.setHasHeavyLift(req.hasHeavyLift());
        s.setMaxRepairWeightTons(req.maxRepairWeightTons());
        s.setDescription(req.description()); s.setContactPhone(req.contactPhone());
        return repository.save(s);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }
}
