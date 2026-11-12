package ru.fa.atelier.service;

import ru.fa.atelier.dto.OrderForm;
import ru.fa.atelier.entity.Order;
import ru.fa.atelier.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createForClient(String email, OrderForm form);
    List<Order> findForClient(String email, OrderStatus status, String search);
    List<Order> findAll(OrderStatus status, String search);
    void changeStatus(Long orderId, OrderStatus status, String actorEmail);
}
