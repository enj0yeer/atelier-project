package ru.fa.atelier.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class StatisticsData {
    private long totalOrders;
    private BigDecimal totalRevenue;
    private List<String> statusLabels;
    private List<Long> statusValues;
}
