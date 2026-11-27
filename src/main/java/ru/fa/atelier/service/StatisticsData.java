package ru.fa.atelier.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class StatisticsData {
    private long totalOrders;
    private long totalUsers;
    private BigDecimal totalRevenue;
    private long totalIssuedOrders;
    private long issuedOrdersWithoutCompletionDate;
    private BigDecimal averageCompletionDays;
    private List<String> statusLabels;
    private List<Long> statusValues;
}
