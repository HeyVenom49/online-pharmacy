package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineDTO;
import com.pharmacy.catalog.service.MedicineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MedicineControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MedicineService medicineService;

    @BeforeEach
    void setUp() {
        MedicineController controller = new MedicineController(medicineService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getAllMedicines_returnsOk() throws Exception {
        Page<MedicineDTO> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(medicineService.getAllMedicines(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/catalog/medicines"))
                .andExpect(status().isOk());
    }
}
