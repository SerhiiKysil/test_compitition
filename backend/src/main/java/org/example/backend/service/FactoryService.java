package org.example.backend.service;

import org.example.backend.dto.FactoryRequest;
import org.example.backend.entity.Factory;
import org.example.backend.entity.Location;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.FactoryRepository;
import org.example.backend.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FactoryService {

    private final FactoryRepository repository;
    private final LocationRepository locationRepository;

    public FactoryService(FactoryRepository repository, LocationRepository locationRepository) {
        this.repository = repository;
        this.locationRepository = locationRepository;
    }

    public List<Factory> getAll() { return repository.findAll(); }

    public Factory getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", id));
    }

    @Transactional
    public Factory create(FactoryRequest req) {
        Location loc = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        return repository.save(Factory.builder()
                .name(req.name()).location(loc).description(req.description())
                .contactName(req.contactName()).contactPhone(req.contactPhone())
                .contactEmail(req.contactEmail()).productionCapacity(req.productionCapacity())
                .build());
    }

    @Transactional
    public Factory update(Long id, FactoryRequest req) {
        Factory f = getById(id);
        Location loc = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        f.setName(req.name()); f.setLocation(loc); f.setDescription(req.description());
        f.setContactName(req.contactName()); f.setContactPhone(req.contactPhone());
        f.setContactEmail(req.contactEmail()); f.setProductionCapacity(req.productionCapacity());
        return repository.save(f);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }
}
