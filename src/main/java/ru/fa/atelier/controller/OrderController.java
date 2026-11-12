package ru.fa.atelier.controller;

import ru.fa.atelier.dto.OrderForm;
import ru.fa.atelier.entity.OrderStatus;
import ru.fa.atelier.service.OrderService;
import ru.fa.atelier.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final ServiceService serviceService;

    @GetMapping
    public String list(Authentication authentication,
                       @RequestParam(required = false) OrderStatus status,
                       @RequestParam(required = false) String q,
                       Model model) {
        boolean isClient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        model.addAttribute("orders", isClient
                ? orderService.findForClient(authentication.getName(), status, q)
                : orderService.findAll(status, q));
        model.addAttribute("canManage", !isClient);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status == null ? "" : status.name());
        model.addAttribute("searchQuery", q == null ? "" : q);
        return "orders/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('CLIENT')")
    public String createForm(Model model) {
        model.addAttribute("orderForm", new OrderForm());
        model.addAttribute("services", serviceService.findActive());
        return "orders/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public String create(@Valid @ModelAttribute OrderForm orderForm,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("services", serviceService.findActive());
            return "orders/form";
        }
        try {
            orderService.createForClient(authentication.getName(), orderForm);
            redirectAttributes.addFlashAttribute("successMessage", "Заказ успешно создан");
            return "redirect:/orders";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("services", serviceService.findActive());
            model.addAttribute("formError", exception.getMessage());
            return "orders/form";
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MASTER', 'ADMIN')")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        orderService.changeStatus(id, status, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Статус заказа обновлён");
        return "redirect:/orders";
    }
}
