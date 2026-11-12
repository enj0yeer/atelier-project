package ru.fa.atelier.service;

import ru.fa.atelier.entity.OrderStatus;
import ru.fa.atelier.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public StatisticsData getSummary() {
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            labels.add(toRussian(status));
            values.add(orderRepository.countByStatus(status));
        }
        BigDecimal revenue = orderRepository.calculateTotalRevenue();
        return new StatisticsData(orderRepository.count(), revenue, labels, values);
    }

    private String toRussian(OrderStatus status) {
        return switch (status) {
            case NEW -> "Новый";
            case IN_PROGRESS -> "В работе";
            case READY -> "Готов";
            case ISSUED -> "Выдан";
        };
    }
}
