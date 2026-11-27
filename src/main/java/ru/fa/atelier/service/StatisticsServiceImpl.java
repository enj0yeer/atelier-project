package ru.fa.atelier.service;

import ru.fa.atelier.entity.OrderStatus;
import ru.fa.atelier.repository.OrderRepository;
import ru.fa.atelier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import ru.fa.atelier.entity.Order;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

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
        long issuedOrderCount = orderRepository.countByStatus(OrderStatus.ISSUED);
        List<Order> completedOrders = orderRepository.findAllByCompletedAtIsNotNull().stream()
                .filter(order -> order.getStatus() == OrderStatus.ISSUED)
                .toList();
        long issuedOrdersWithoutCompletionDate = Math.max(0, issuedOrderCount - completedOrders.size());
        BigDecimal averageCompletionDays = completedOrders.isEmpty() ? null : BigDecimal.valueOf(
                completedOrders.stream()
                        .mapToLong(order -> Duration.between(order.getOrderDate(), order.getCompletedAt()).toMinutes())
                        .average().orElse(0) / 1440.0
        ).setScale(1, RoundingMode.HALF_UP);
        return new StatisticsData(orderRepository.count(), userRepository.count(), revenue, issuedOrderCount,
                issuedOrdersWithoutCompletionDate, averageCompletionDays, labels, values);
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
