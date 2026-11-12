package ru.fa.atelier.controller;

import ru.fa.atelier.service.StatisticsData;
import ru.fa.atelier.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('MASTER', 'ADMIN')")
    public String statistics(Model model) {
        StatisticsData data = statisticsService.getSummary();
        model.addAttribute("statistics", data);
        model.addAttribute("statusLabels", data.getStatusLabels());
        model.addAttribute("statusValues", data.getStatusValues());
        return "statistics";
    }
}
