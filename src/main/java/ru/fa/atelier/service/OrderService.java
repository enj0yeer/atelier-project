package ru.fa.atelier.service;

import ru.fa.atelier.dto.OrderForm;
import ru.fa.atelier.entity.Order;
import ru.fa.atelier.entity.OrderStatus;
import ru.fa.atelier.entity.Role;

import java.util.List;

public interface OrderService {
    Order createForClient(String email, OrderForm form);
    List<Order> findForClient(String email, OrderStatus status, String search, String sortBy);
    List<Order> findAll(OrderStatus status, String search, String sortBy);
    void changeStatus(Long orderId, OrderStatus status, String actorEmail, Role actorRole);
    void delete(Long orderId);
}
