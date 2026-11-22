package ru.fa.atelier.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class AccessDeniedController {
    @GetMapping("/error/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden(Model model) {
        model.addAttribute("errorTitle", "Доступ запрещён");
        model.addAttribute("errorMessage", "У вашей учётной записи нет прав для этой страницы.");
        return "error/general";
    }
}
