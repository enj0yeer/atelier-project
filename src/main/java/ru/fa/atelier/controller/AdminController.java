package ru.fa.atelier.controller;

import ru.fa.atelier.dto.ServiceForm;
import ru.fa.atelier.entity.Service;
import ru.fa.atelier.entity.Role;
import ru.fa.atelier.service.ServiceService;
import ru.fa.atelier.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final ServiceService serviceService;

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id,
                                 @RequestParam Role role,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        userService.changeRole(id, role, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Роль пользователя обновлена");
        return "redirect:/admin/users";
    }

    @GetMapping("/services/new")
    public String newService(Model model) {
        model.addAttribute("serviceForm", new ServiceForm());
        model.addAttribute("pageTitle", "Добавление услуги");
        return "admin/services/form";
    }

    @PostMapping("/services")
    public String createService(@Valid @ModelAttribute ServiceForm serviceForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Добавление услуги");
            return "admin/services/form";
        }
        serviceService.create(serviceForm);
        redirectAttributes.addFlashAttribute("successMessage", "Услуга добавлена");
        return "redirect:/services";
    }

    @GetMapping("/services/{id}/edit")
    public String editService(@PathVariable Long id, Model model) {
        Service service = serviceService.findById(id);
        ServiceForm form = new ServiceForm();
        form.setName(service.getName());
        form.setDescription(service.getDescription());
        form.setPrice(service.getPrice());
        model.addAttribute("serviceForm", form);
        model.addAttribute("pageTitle", "Редактирование услуги");
        model.addAttribute("serviceId", id);
        return "admin/services/form";
    }

    @PostMapping("/services/{id}")
    public String updateService(@PathVariable Long id,
                                @Valid @ModelAttribute ServiceForm serviceForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Редактирование услуги");
            model.addAttribute("serviceId", id);
            return "admin/services/form";
        }
        serviceService.update(id, serviceForm);
        redirectAttributes.addFlashAttribute("successMessage", "Изменения сохранены");
        return "redirect:/services";
    }

    @PostMapping("/services/{id}/deactivate")
    public String deactivateService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        serviceService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Услуга скрыта из каталога");
        return "redirect:/services";
    }
}
