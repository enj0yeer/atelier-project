package ru.fa.atelier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fa.atelier.dto.OrderForm;
import ru.fa.atelier.entity.*;
import ru.fa.atelier.repository.ClientRepository;
import ru.fa.atelier.repository.EmployeeRepository;
import ru.fa.atelier.repository.OrderRepository;
import ru.fa.atelier.repository.ServiceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock private OrderRepository orderRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private EmployeeRepository employeeRepository;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orderRepository, clientRepository, serviceRepository, employeeRepository);
    }

    @Test
    void createForClientCalculatesTotalFromCurrentServicePrice() {
        Client client = new Client();
        ru.fa.atelier.entity.Service serviceItem = new ru.fa.atelier.entity.Service();
        serviceItem.setActive(true);
        serviceItem.setPrice(new BigDecimal("1250.50"));
        when(clientRepository.findByUser_Email("client@example.test")).thenReturn(Optional.of(client));
        when(serviceRepository.findById(7L)).thenReturn(Optional.of(serviceItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderForm form = new OrderForm();
        form.setServiceId(7L);
        form.setQuantity(3);
        form.setDueDate(LocalDate.now().plusDays(5));

        Order order = service.createForClient("client@example.test", form);

        assertEquals(new BigDecimal("3751.50"), order.getTotalPrice());
        assertEquals(1, order.getItems().size());
        assertEquals(new BigDecimal("1250.50"), order.getItems().get(0).getUnitPrice());
        assertEquals(3, order.getItems().get(0).getQuantity());
        assertSame(order, order.getItems().get(0).getOrder());
    }

    @Test
    void createForClientRejectsInactiveService() {
        Client client = new Client();
        ru.fa.atelier.entity.Service serviceItem = new ru.fa.atelier.entity.Service();
        serviceItem.setActive(false);
        when(clientRepository.findByUser_Email("client@example.test")).thenReturn(Optional.of(client));
        when(serviceRepository.findById(7L)).thenReturn(Optional.of(serviceItem));

        OrderForm form = new OrderForm();
        form.setServiceId(7L);
        form.setQuantity(1);
        form.setDueDate(LocalDate.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> service.createForClient("client@example.test", form));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void changeStatusAssignsFreeOrderToMasterAndAdvancesOneStep() {
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        Employee employee = new Employee();
        employee.setActive(true);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByUser_Email("master@example.test")).thenReturn(Optional.of(employee));

        service.changeStatus(1L, OrderStatus.IN_PROGRESS, "master@example.test");

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertSame(employee, order.getEmployee());
        verify(orderRepository).save(order);
    }

    @Test
    void changeStatusRejectsSkippingStates() {
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByUser_Email("admin@example.test")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.changeStatus(1L, OrderStatus.READY, "admin@example.test"));
        assertEquals(OrderStatus.NEW, order.getStatus());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void changeStatusRejectsOrderAssignedToAnotherMaster() {
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        Employee assigned = new Employee();
        assigned.setId(10L);
        order.setEmployee(assigned);
        Employee other = new Employee();
        other.setId(11L);
        other.setActive(true);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByUser_Email("other@example.test")).thenReturn(Optional.of(other));

        assertThrows(IllegalArgumentException.class,
                () -> service.changeStatus(1L, OrderStatus.IN_PROGRESS, "other@example.test"));
        verify(orderRepository, never()).save(any());
    }
}
