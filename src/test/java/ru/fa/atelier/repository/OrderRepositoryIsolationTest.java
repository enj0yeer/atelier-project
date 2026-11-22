package ru.fa.atelier.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.fa.atelier.entity.Client;
import ru.fa.atelier.entity.Order;
import ru.fa.atelier.entity.OrderStatus;
import ru.fa.atelier.entity.Role;
import ru.fa.atelier.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:atelier-isolation-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderRepositoryIsolationTest {
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClientRepository clientRepository;

    @Test
    void searchForClientReturnsOnlyOrdersOwnedByThatUser() {
        Client firstClient = createClient("first@example.test", "Первый клиент");
        Client secondClient = createClient("second@example.test", "Второй клиент");
        orderRepository.saveAndFlush(createOrder(firstClient));
        orderRepository.saveAndFlush(createOrder(secondClient));

        var results = orderRepository.searchForClient("first@example.test", null, "");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getClient().getEmail()).isEqualTo("first@example.test");
    }

    private Client createClient(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("{noop}test-password");
        user.setFullName(name);
        user.setRole(Role.CLIENT);
        userRepository.saveAndFlush(user);

        Client client = new Client();
        client.setEmail(email);
        client.setFullName(name);
        client.setUser(user);
        return clientRepository.saveAndFlush(client);
    }

    private Order createOrder(Client client) {
        Order order = new Order();
        order.setClient(client);
        order.setDueDate(LocalDate.now().plusDays(2));
        order.setStatus(OrderStatus.NEW);
        order.setTotalPrice(new BigDecimal("500.00"));
        return order;
    }
}
