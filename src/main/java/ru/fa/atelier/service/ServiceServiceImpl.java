package ru.fa.atelier.service;

import ru.fa.atelier.dto.ServiceForm;
import ru.fa.atelier.entity.Service;
import ru.fa.atelier.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {
    private final ServiceRepository serviceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Service> findActive() {
        return serviceRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Service findById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена"));
    }

    @Override
    @Transactional
    public Service create(ServiceForm form) {
        String name = requireName(form);
        if (serviceRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Услуга с таким названием уже существует");
        }
        Service service = new Service();
        applyForm(service, form);
        service.setActive(true);
        return serviceRepository.save(service);
    }

    @Override
    @Transactional
    public Service update(Long id, ServiceForm form) {
        Service service = findById(id);
        String name = requireName(form);
        if (serviceRepository.existsByNameIgnoreCase(name) && !name.equalsIgnoreCase(service.getName())) {
            throw new IllegalArgumentException("Услуга с таким названием уже существует");
        }
        applyForm(service, form);
        return serviceRepository.save(service);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Service service = findById(id);
        service.setActive(false);
        serviceRepository.save(service);
    }

    private String requireName(ServiceForm form) {
        String name = form.getName() == null ? "" : form.getName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Название услуги не может быть пустым");
        }
        return name;
    }

    private void applyForm(Service service, ServiceForm form) {
        service.setName(requireName(form));
        service.setDescription(form.getDescription());
        service.setPrice(form.getPrice());
    }
}
