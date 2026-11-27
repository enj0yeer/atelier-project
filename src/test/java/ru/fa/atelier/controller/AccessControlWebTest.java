package ru.fa.atelier.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.fa.atelier.config.SecurityConfig;
import ru.fa.atelier.security.CustomUserDetailsService;
import ru.fa.atelier.service.OrderService;
import ru.fa.atelier.service.ServiceService;
import ru.fa.atelier.service.UserService;
import ru.fa.atelier.service.StatisticsService;
import ru.fa.atelier.service.StatisticsData;
import java.math.BigDecimal;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminController.class, OrderController.class, AboutController.class, ServiceController.class, StatisticsController.class, AccessDeniedController.class})
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class AccessControlWebTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private CustomUserDetailsService userDetailsService;
    @MockBean private UserService userService;
    @MockBean private ServiceService serviceService;
    @MockBean private OrderService orderService;
    @MockBean private StatisticsService statisticsService;

    @Test
    @WithMockUser(username = "client@example.test", roles = "CLIENT")
    void clientCannotOpenAdminUsersPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void administratorCanOpenAdminUsersPage() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());

        verify(userService).findAll();
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void missingServiceEditPageReturnsNotFound() throws Exception {
        when(serviceService.findById(999L)).thenThrow(new jakarta.persistence.EntityNotFoundException("Услуга не найдена"));

        mockMvc.perform(get("/admin/services/999/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "client@example.test", roles = "CLIENT")
    void clientOrdersPageUsesClientScopedQuery() throws Exception {
        when(orderService.findForClient("client@example.test", null, null, "date")).thenReturn(List.of());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());

        verify(orderService).findForClient("client@example.test", null, null, "date");
        verify(orderService, never()).findAll(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "client@example.test", roles = "CLIENT")
    void clientCanViewCatalogButCannotOpenAdminOrStatistics() throws Exception {
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/statistics"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(statisticsService);
    }

    @Test
    @WithMockUser(username = "master@example.test", roles = "MASTER")
    void masterCanOpenStatistics() throws Exception {
        when(statisticsService.getSummary()).thenReturn(new StatisticsData(0, 0, BigDecimal.ZERO, 0, 0, null,
                List.of("Новый", "В работе", "Готов", "Выдан"), List.of(0L, 0L, 0L, 0L)));

        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void administratorCanDeleteOrder() throws Exception {
        mockMvc.perform(post("/orders/7/delete").with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(orderService).delete(7L);
    }

    @Test
    @WithMockUser(username = "master@example.test", roles = "MASTER")
    void masterCannotDeleteOrder() throws Exception {
        mockMvc.perform(post("/orders/7/delete").with(csrf()))
                .andExpect(status().isForbidden());

        verify(orderService, never()).delete(anyLong());
    }

    @Test
    @WithMockUser(username = "client@example.test", roles = "CLIENT")
    void clientCannotDeleteOrder() throws Exception {
        mockMvc.perform(post("/orders/7/delete").with(csrf()))
                .andExpect(status().isForbidden());

        verify(orderService, never()).delete(anyLong());
    }

    @Test
    void anonymousVisitorCanOpenHomeButMustSignInForServiceCatalog() throws Exception {
        when(serviceService.findActive()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/services"))
                .andExpect(status().is3xxRedirection());
    }
}
