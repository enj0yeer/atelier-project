package ru.fa.atelier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {
    @GetMapping("/")
    public String index() {
        return "redirect:/services";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
