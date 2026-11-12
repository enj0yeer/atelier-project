package ru.fa.atelier.service;

import ru.fa.atelier.dto.RegistrationForm;
import ru.fa.atelier.entity.User;
import ru.fa.atelier.entity.Role;

import java.util.List;

public interface UserService {
    User register(RegistrationForm form);
    List<User> findAll();
    void changeRole(Long userId, Role role, String adminEmail);
}
