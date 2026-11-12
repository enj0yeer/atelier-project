package ru.fa.atelier.controller;

import ru.fa.atelier.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/services")
public class ServiceController {
    private final ServiceService serviceService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("services", serviceService.findActive());
        return "services/list";
    }
}
