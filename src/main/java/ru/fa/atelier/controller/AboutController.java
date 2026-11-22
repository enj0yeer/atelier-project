package ru.fa.atelier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.fa.atelier.service.ServiceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AboutController {
    private final ServiceService serviceService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("services", serviceService.findActive().stream().limit(3).toList());
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
