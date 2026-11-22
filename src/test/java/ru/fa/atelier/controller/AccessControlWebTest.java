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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminController.class, OrderController.class, AboutController.class, ServiceController.class, AccessDeniedController.class})
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class AccessControlWebTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private CustomUserDetailsService userDetailsService;
    @MockBean private UserService userService;
    @MockBean private ServiceService serviceService;
    @MockBean private OrderService orderService;

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
    @WithMockUser(username = "client@example.test", roles = "CLIENT")
    void clientOrdersPageUsesClientScopedQuery() throws Exception {
        when(orderService.findForClient("client@example.test", null, null)).thenReturn(List.of());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());

        verify(orderService).findForClient("client@example.test", null, null);
        verify(orderService, never()).findAll(any(), any());
    }

    @Test
    void anonymousVisitorCanOpenHomePageAndServiceCatalog() throws Exception {
        when(serviceService.findActive()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk());
    }
}
