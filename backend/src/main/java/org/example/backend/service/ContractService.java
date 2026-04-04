package org.example.backend.service;

import org.example.backend.dto.ContractRequest;
import org.example.backend.entity.*;
import org.example.backend.enums.ContractStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContractService {

    private final ContractRepository repository;
    private final FactoryRepository factoryRepository;
    private final ConsumerRepository consumerRepository;
    private final ProductRepository productRepository;

    public ContractService(ContractRepository repository, FactoryRepository factoryRepository,
                           ConsumerRepository consumerRepository, ProductRepository productRepository) {
        this.repository = repository;
        this.factoryRepository = factoryRepository;
        this.consumerRepository = consumerRepository;
        this.productRepository = productRepository;
    }

    public List<Contract> getAll() { return repository.findAll(); }

    public Contract getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
    }

    @Transactional
    public Contract create(ContractRequest req) {
        Factory factory = factoryRepository.findById(req.factoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory", req.factoryId()));
        Consumer consumer = consumerRepository.findById(req.consumerId())
                .orElseThrow(() -> new ResourceNotFoundException("Consumer", req.consumerId()));
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.productId()));
        return repository.save(Contract.builder()
                .contractNumber(req.contractNumber()).factory(factory).consumer(consumer)
                .product(product).quantity(req.quantity()).unitPrice(req.unitPrice())
                .totalValue(req.totalValue()).startDate(req.startDate()).endDate(req.endDate())
                .status(req.status() != null ? req.status() : ContractStatus.ACTIVE)
                .description(req.description()).build());
    }

    @Transactional
    public Contract update(Long id, ContractRequest req) {
        Contract c = getById(id);
        Factory factory = factoryRepository.findById(req.factoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory", req.factoryId()));
        Consumer consumer = consumerRepository.findById(req.consumerId())
                .orElseThrow(() -> new ResourceNotFoundException("Consumer", req.consumerId()));
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.productId()));
        c.setContractNumber(req.contractNumber()); c.setFactory(factory);
        c.setConsumer(consumer); c.setProduct(product); c.setQuantity(req.quantity());
        c.setUnitPrice(req.unitPrice()); c.setTotalValue(req.totalValue());
        c.setStartDate(req.startDate()); c.setEndDate(req.endDate());
        if (req.status() != null) c.setStatus(req.status());
        c.setDescription(req.description());
        return repository.save(c);
    }

    @Transactional
    public Contract updateStatus(Long id, ContractStatus status) {
        Contract c = getById(id);
        c.setStatus(status);
        return repository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }
}
