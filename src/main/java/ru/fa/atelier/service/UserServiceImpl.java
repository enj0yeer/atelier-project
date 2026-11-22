package ru.fa.atelier.service;

import ru.fa.atelier.dto.RegistrationForm;
import ru.fa.atelier.entity.Client;
import ru.fa.atelier.entity.Employee;
import ru.fa.atelier.entity.Role;
import ru.fa.atelier.entity.User;
import ru.fa.atelier.repository.ClientRepository;
import ru.fa.atelier.repository.EmployeeRepository;
import ru.fa.atelier.repository.OrderRepository;
import ru.fa.atelier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegistrationForm form) {
        if (form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Пароль слишком длинный: не более 72 байт в кодировке UTF-8");
        }
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с такой электронной почтой уже зарегистрирован");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setFullName(form.getFullName().trim());
        user.setRole(Role.CLIENT);
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Пользователь с такой электронной почтой уже зарегистрирован", exception);
        }

        Client client = new Client();
        client.setFullName(user.getFullName());
        client.setEmail(user.getEmail());
        client.setPhone(form.getPhone());
        client.setUser(user);
        clientRepository.save(client);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void changeRole(Long userId, Role role, String adminEmail) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (target.getEmail().equalsIgnoreCase(adminEmail) && role != Role.ADMIN) {
            throw new IllegalArgumentException("Нельзя снять роль администратора с собственной учётной записи");
        }
        target.setRole(role);
        if (role == Role.CLIENT) {
            ensureClientProfile(target);
        } else if (role == Role.MASTER) {
            ensureEmployeeProfile(target);
        }
        userRepository.save(target);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, String adminEmail) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (target.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException("Нельзя удалить собственную учётную запись");
        }
        if (target.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("Нельзя удалить последнего администратора");
        }

        clientRepository.findByUser_Email(target.getEmail()).ifPresent(client -> {
            if (orderRepository.existsByClient_Id(client.getId())) {
                throw new IllegalArgumentException("У пользователя есть заказы. Удалить его нельзя, чтобы сохранить историю.");
            }
            clientRepository.delete(client);
        });
        employeeRepository.findByUser_Email(target.getEmail()).ifPresent(employee -> {
            if (orderRepository.existsByEmployee_Id(employee.getId())) {
                throw new IllegalArgumentException("За пользователем закреплены заказы. Удалить его нельзя, чтобы сохранить историю.");
            }
            employeeRepository.delete(employee);
        });
        userRepository.delete(target);
    }

    private void ensureClientProfile(User user) {
        if (clientRepository.findByUser_Email(user.getEmail()).isEmpty()) {
            Client client = new Client();
            client.setFullName(user.getFullName());
            client.setEmail(user.getEmail());
            client.setUser(user);
            clientRepository.save(client);
        }
    }

    private void ensureEmployeeProfile(User user) {
        if (employeeRepository.findByUser_Email(user.getEmail()).isEmpty()) {
            Employee employee = new Employee();
            employee.setFullName(user.getFullName());
            employee.setPosition("Мастер");
            employee.setActive(true);
            employee.setUser(user);
            employeeRepository.save(employee);
        }
    }
}
