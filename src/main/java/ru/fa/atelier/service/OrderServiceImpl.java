package ru.fa.atelier.service;

import ru.fa.atelier.dto.OrderForm;
import ru.fa.atelier.entity.*;
import ru.fa.atelier.repository.ClientRepository;
import ru.fa.atelier.repository.EmployeeRepository;
import ru.fa.atelier.repository.OrderRepository;
import ru.fa.atelier.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final BigDecimal MAX_ORDER_TOTAL = new BigDecimal("9999999999.99");

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public Order createForClient(String email, OrderForm form) {
        Client client = clientRepository.findByUser_Email(email)
                .orElseThrow(() -> new IllegalArgumentException("Профиль клиента не найден"));
        ru.fa.atelier.entity.Service service = serviceRepository.findById(form.getServiceId())
                .filter(ru.fa.atelier.entity.Service::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Выбранная услуга недоступна"));

        BigDecimal quantity = BigDecimal.valueOf(form.getQuantity());
        BigDecimal total = service.getPrice().multiply(quantity);
        if (total.compareTo(MAX_ORDER_TOTAL) > 0) {
            throw new IllegalArgumentException("Итоговая стоимость заказа не должна превышать 9999999999.99");
        }

        Order order = new Order();
        order.setClient(client);
        order.setDueDate(form.getDueDate());
        order.setStatus(OrderStatus.NEW);
        order.setTotalPrice(total);
        order.setNote(form.getNote());

        OrderItem item = new OrderItem();
        item.setService(service);
        item.setQuantity(form.getQuantity());
        item.setUnitPrice(service.getPrice());
        item.setLineTotal(total);
        order.addItem(item);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findForClient(String email, OrderStatus status, String search) {
        return orderRepository.searchForClient(email, status, normalizeSearch(search));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll(OrderStatus status, String search) {
        return orderRepository.searchAll(status, normalizeSearch(search));
    }

    private String normalizeSearch(String search) {
        return search == null || search.isBlank() ? "" : search.trim();
    }

    @Override
    @Transactional
    public void changeStatus(Long orderId, OrderStatus status, String actorEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
        OrderStatus current = order.getStatus();
        if (current == status) {
            return;
        }
        Employee employee = employeeRepository.findByUser_Email(actorEmail).orElse(null);
        if (employee != null) {
            if (!employee.isActive()) {
                throw new IllegalArgumentException("Профиль мастера неактивен");
            }
            if (order.getEmployee() != null && !order.getEmployee().getId().equals(employee.getId())) {
                throw new IllegalArgumentException("Заказ закреплён за другим мастером");
            }
            if (order.getEmployee() == null) {
                order.setEmployee(employee);
            }
        }
        if (!isValidTransition(current, status)) {
            throw new IllegalArgumentException("Недопустимый переход статуса: " + current + " → " + status);
        }
        order.setStatus(status);
        orderRepository.save(order);
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus status) {
        return switch (current) {
            case NEW -> status == OrderStatus.IN_PROGRESS;
            case IN_PROGRESS -> status == OrderStatus.READY;
            case READY -> status == OrderStatus.ISSUED;
            case ISSUED -> false;
        };
    }
}
