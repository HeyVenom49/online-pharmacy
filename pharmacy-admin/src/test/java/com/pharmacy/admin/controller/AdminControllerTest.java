package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.DashboardDTO;
import com.pharmacy.admin.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(dashboardService)).build();
    }

    @Test
    void getDashboard_returnsOk() throws Exception {
        when(dashboardService.getDashboard()).thenReturn(DashboardDTO.builder().build());

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk());
    }
}
