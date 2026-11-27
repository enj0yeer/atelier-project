package ru.fa.atelier.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import ru.fa.atelier.entity.Client;
import ru.fa.atelier.entity.Employee;
import ru.fa.atelier.entity.Order;
import ru.fa.atelier.entity.OrderItem;
import ru.fa.atelier.entity.OrderStatus;
import ru.fa.atelier.entity.Role;
import ru.fa.atelier.entity.Service;
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
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ServiceRepository serviceRepository;

    @Test
    void searchForClientReturnsOnlyOrdersOwnedByThatUser() {
        Client firstClient = createClient("first@example.test", "Первый клиент");
        Client secondClient = createClient("second@example.test", "Второй клиент");
        orderRepository.saveAndFlush(createOrder(firstClient));
        orderRepository.saveAndFlush(createOrder(secondClient));

        var results = orderRepository.searchForClient("first@example.test", null, "", org.springframework.data.domain.Sort.unsorted());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getClient().getEmail()).isEqualTo("first@example.test");
    }

    @Test
    void searchAllAppliesRequestedSortToDatabaseResults() {
        Client client = createClient("sort@example.test", "Клиент для сортировки");
        Order laterOrder = createOrder(client);
        laterOrder.setDueDate(LocalDate.now().plusDays(5));
        orderRepository.saveAndFlush(laterOrder);

        Order earlierOrder = createOrder(client);
        earlierOrder.setDueDate(LocalDate.now().plusDays(1));
        orderRepository.saveAndFlush(earlierOrder);

        var results = orderRepository.searchAll(null, "", Sort.by(Sort.Direction.ASC, "dueDate"));

        assertThat(results).extracting(Order::getDueDate)
                .containsExactly(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
    }

    @Test
    void deletingOrderAlsoDeletesItemsButKeepsClientEmployeeAndService() {
        Client client = createClient("delete-client@example.test", "Клиент для удаления заказа");
        User masterUser = new User();
        masterUser.setEmail("delete-master@example.test");
        masterUser.setPassword("{noop}test-password");
        masterUser.setFullName("Мастер для удаления заказа");
        masterUser.setRole(Role.MASTER);
        userRepository.saveAndFlush(masterUser);

        Employee employee = new Employee();
        employee.setFullName(masterUser.getFullName());
        employee.setPosition("Мастер");
        employee.setUser(masterUser);
        employee = employeeRepository.saveAndFlush(employee);

        Service service = new Service();
        service.setName("Услуга для проверки каскада");
        service.setPrice(new BigDecimal("500.00"));
        service = serviceRepository.saveAndFlush(service);

        Order order = createOrder(client);
        order.setEmployee(employee);
        OrderItem item = new OrderItem();
        item.setService(service);
        item.setQuantity(1);
        item.setUnitPrice(service.getPrice());
        item.setLineTotal(service.getPrice());
        order.addItem(item);
        order = orderRepository.saveAndFlush(order);
        Long orderId = order.getId();
        Long clientId = client.getId();
        Long employeeId = employee.getId();
        Long serviceId = service.getId();

        orderRepository.deleteById(orderId);
        orderRepository.flush();

        assertThat(orderRepository.findById(orderId)).isEmpty();
        assertThat(orderItemRepository.count()).isZero();
        assertThat(clientRepository.findById(clientId)).isPresent();
        assertThat(employeeRepository.findById(employeeId)).isPresent();
        assertThat(serviceRepository.findById(serviceId)).isPresent();
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
