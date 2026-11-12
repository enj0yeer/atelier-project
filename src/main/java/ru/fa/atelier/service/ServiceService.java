package ru.fa.atelier.service;

import ru.fa.atelier.dto.ServiceForm;
import ru.fa.atelier.entity.Service;

import java.util.List;

public interface ServiceService {
    List<Service> findActive();
    Service findById(Long id);
    Service create(ServiceForm form);
    Service update(Long id, ServiceForm form);
    void deactivate(Long id);
}
