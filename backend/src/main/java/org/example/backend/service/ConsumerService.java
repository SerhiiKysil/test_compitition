package org.example.backend.service;

import org.example.backend.dto.ConsumerRequest;
import org.example.backend.entity.Consumer;
import org.example.backend.entity.Location;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.ConsumerRepository;
import org.example.backend.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsumerService {

    private final ConsumerRepository repository;
    private final LocationRepository locationRepository;

    public ConsumerService(ConsumerRepository repository, LocationRepository locationRepository) {
        this.repository = repository;
        this.locationRepository = locationRepository;
    }

    public List<Consumer> getAll() { return repository.findAll(); }

    public Consumer getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consumer", id));
    }

    @Transactional
    public Consumer create(ConsumerRequest req) {
        Location loc = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        return repository.save(Consumer.builder()
                .name(req.name()).location(loc).contactName(req.contactName())
                .contactPhone(req.contactPhone()).contactEmail(req.contactEmail())
                .description(req.description()).build());
    }

    @Transactional
    public Consumer update(Long id, ConsumerRequest req) {
        Consumer c = getById(id);
        Location loc = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        c.setName(req.name()); c.setLocation(loc);
        c.setContactName(req.contactName()); c.setContactPhone(req.contactPhone());
        c.setContactEmail(req.contactEmail()); c.setDescription(req.description());
        return repository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }
}
