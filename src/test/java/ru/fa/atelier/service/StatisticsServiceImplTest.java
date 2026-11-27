package ru.fa.atelier.service;

import org.junit.jupiter.api.Test;
import ru.fa.atelier.entity.Order;
import ru.fa.atelier.entity.OrderStatus;
import ru.fa.atelier.repository.OrderRepository;
import ru.fa.atelier.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatisticsServiceImplTest {

    @Test
    void reportsIssuedOrdersWithoutCompletionDatesInsteadOfSayingThereAreNoIssuedOrders() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(orderRepository.countByStatus(OrderStatus.ISSUED)).thenReturn(2L);
        when(orderRepository.count()).thenReturn(4L);
        when(userRepository.count()).thenReturn(4L);
        when(orderRepository.calculateTotalRevenue()).thenReturn(new BigDecimal("10900.00"));
        when(orderRepository.findAllByCompletedAtIsNotNull()).thenReturn(List.of());

        StatisticsData statistics = new StatisticsServiceImpl(orderRepository, userRepository).getSummary();

        assertEquals(2, statistics.getTotalIssuedOrders());
        assertEquals(2, statistics.getIssuedOrdersWithoutCompletionDate());
        assertNull(statistics.getAverageCompletionDays());
    }

    @Test
    void calculatesAverageForIssuedOrdersWithRecordedCompletionDates() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        Order completedOrder = new Order();
        completedOrder.setStatus(OrderStatus.ISSUED);
        completedOrder.setOrderDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        completedOrder.setCompletedAt(LocalDateTime.of(2026, 1, 3, 10, 0));
        when(orderRepository.countByStatus(OrderStatus.ISSUED)).thenReturn(1L);
        when(orderRepository.calculateTotalRevenue()).thenReturn(BigDecimal.ZERO);
        when(orderRepository.findAllByCompletedAtIsNotNull()).thenReturn(List.of(completedOrder));

        StatisticsData statistics = new StatisticsServiceImpl(orderRepository, userRepository).getSummary();

        assertEquals(1, statistics.getTotalIssuedOrders());
        assertEquals(0, statistics.getIssuedOrdersWithoutCompletionDate());
        assertEquals(new BigDecimal("2.0"), statistics.getAverageCompletionDays());
    }
}
