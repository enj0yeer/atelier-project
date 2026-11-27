package ru.fa.atelier.config;

import ru.fa.atelier.entity.*;
import ru.fa.atelier.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initializeDemoData() {
        return args -> {
            User admin = userRepository.findByEmail("admin@atelier.local").orElseGet(() ->
                    createUser("admin@atelier.local", "admin123", "Администратор ателье", Role.ADMIN));
            User master = userRepository.findByEmail("master@atelier.local").orElseGet(() ->
                    createUser("master@atelier.local", "master123", "Иванова Мария Сергеевна", Role.MASTER));
            User clientUser = userRepository.findByEmail("client@atelier.local").orElseGet(() ->
                    createUser("client@atelier.local", "client123", "Петрова Анна Андреевна", Role.CLIENT));

            if (clientRepository.findByUser_Email(clientUser.getEmail()).isEmpty()) {
                Client client = new Client();
                client.setFullName(clientUser.getFullName());
                client.setEmail(clientUser.getEmail());
                client.setPhone("+7 900 000-00-01");
                client.setUser(clientUser);
                clientRepository.save(client);
            }

            if (employeeRepository.findByUser_Email(master.getEmail()).isEmpty()) {
                Employee employee = new Employee();
                employee.setFullName(master.getFullName());
                employee.setPosition("Мастер-портной");
                employee.setActive(true);
                employee.setUser(master);
                employeeRepository.save(employee);
            }

            if (serviceRepository.count() == 0) {
                addService("Подгонка изделия по фигуре", "Корректировка посадки готовой одежды", "1200.00");
                addService("Ремонт одежды", "Замена молнии, ремонт швов и мелкие исправления", "900.00");
                addService("Пошив юбки", "Индивидуальный пошив юбки по меркам клиента", "4500.00");
                addService("Пошив платья", "Разработка и пошив платья по согласованной модели", "8500.00");
                addService("Укорочение брюк", "Укорочение низа брюк с обработкой края", "700.00");
            }

            if (orderRepository.count() == 0) {
                Client client = clientRepository.findByUser_Email(clientUser.getEmail()).orElse(null);
                if (client == null) {
                    return;
                }
                ru.fa.atelier.entity.Service service = serviceRepository.findAllByActiveTrueOrderByNameAsc()
                        .stream().findFirst().orElse(null);
                if (service != null) {
                    Order order = new Order();
                    order.setClient(client);
                    order.setDueDate(LocalDate.now().plusDays(7));
                    order.setStatus(OrderStatus.NEW);
                    order.setTotalPrice(service.getPrice());
                    OrderItem item = new OrderItem();
                    item.setService(service);
                    item.setQuantity(1);
                    item.setUnitPrice(service.getPrice());
                    item.setLineTotal(service.getPrice());
                    order.addItem(item);
                    orderRepository.save(order);
                }
            }
        };
    }

    private User createUser(String email, String rawPassword, String fullName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        return userRepository.save(user);
    }

    private void addService(String name, String description, String price) {
        ru.fa.atelier.entity.Service service = new ru.fa.atelier.entity.Service();
        service.setName(name);
        service.setDescription(description);
        service.setPrice(new BigDecimal(price));
        service.setActive(true);
        serviceRepository.save(service);
    }
}
