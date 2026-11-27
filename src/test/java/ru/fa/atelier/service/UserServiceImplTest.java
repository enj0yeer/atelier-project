package ru.fa.atelier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.fa.atelier.entity.Role;
import ru.fa.atelier.entity.Client;
import ru.fa.atelier.entity.User;
import ru.fa.atelier.repository.ClientRepository;
import ru.fa.atelier.repository.EmployeeRepository;
import ru.fa.atelier.repository.OrderRepository;
import ru.fa.atelier.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, clientRepository, employeeRepository, orderRepository, passwordEncoder);
    }

    @Test
    void administratorCannotRemoveOwnAdministratorRole() {
        User admin = new User();
        admin.setEmail("admin@example.test");
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class,
                () -> service.changeRole(1L, Role.CLIENT, "ADMIN@example.test"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changingUserToMasterCreatesEmployeeProfileWhenMissing() {
        User user = new User();
        user.setEmail("new-master@example.test");
        user.setFullName("Новый мастер");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUser_Email(user.getEmail())).thenReturn(Optional.empty());

        service.changeRole(2L, Role.MASTER, "admin@example.test");

        assertEquals(Role.MASTER, user.getRole());
        verify(employeeRepository).save(argThat(employee -> employee.getUser() == user
                && employee.isActive() && "Мастер".equals(employee.getPosition())));
        verify(userRepository).save(user);
    }

    @Test
    void cannotDemoteLastAdministrator() {
        User admin = new User();
        admin.setEmail("admin@example.test");
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> service.changeRole(1L, Role.MASTER, "another-admin@example.test"));
        assertEquals(Role.ADMIN, admin.getRole());
        verify(userRepository, never()).save(any());
    }

    @Test
    void administratorCannotDeleteOwnAccount() {
        User admin = new User();
        admin.setEmail("admin@example.test");
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteUser(1L, "ADMIN@example.test"));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void userWithOrdersCannotBeDeleted() {
        User clientUser = new User();
        clientUser.setId(2L);
        clientUser.setEmail("client@example.test");
        clientUser.setRole(Role.CLIENT);
        Client client = new Client();
        client.setId(12L);
        client.setUser(clientUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(clientUser));
        when(clientRepository.findByUser_Email(clientUser.getEmail())).thenReturn(Optional.of(client));
        when(orderRepository.existsByClient_Id(12L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteUser(2L, "admin@example.test"));
        verify(clientRepository, never()).delete(any(Client.class));
        verify(userRepository, never()).delete(any(User.class));
    }
}
