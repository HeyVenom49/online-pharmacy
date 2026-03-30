package com.pharmacy.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class ProbeController {
        @GetMapping("/nf")
        void nf() {
            throw new ResourceNotFoundException("missing");
        }

        @GetMapping("/bad")
        void bad() {
            throw new BadRequestException("bad");
        }

        @GetMapping("/unauth")
        void unauth() {
            throw new UnauthorizedException("no");
        }

        @GetMapping("/illegal")
        void illegal() {
            throw new IllegalArgumentException("ia");
        }

        @GetMapping("/stock")
        void stock() {
            throw new InsufficientStockException("low");
        }

        @GetMapping("/file")
        void file() {
            throw new InvalidFileException("bad file");
        }

        @GetMapping("/upload")
        void upload() {
            throw new MaxUploadSizeExceededException(1L);
        }

        @GetMapping("/integrity")
        void integrity() {
            throw new DataIntegrityViolationException("dup", new java.sql.SQLException("constraint"));
        }

        @GetMapping("/boom")
        void boom() {
            throw new RuntimeException("hidden");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFound() throws Exception {
        mockMvc.perform(get("/nf").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void badRequest() throws Exception {
        mockMvc.perform(get("/bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorizedCustom() throws Exception {
        mockMvc.perform(get("/unauth"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void illegalArgument() throws Exception {
        mockMvc.perform(get("/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ia"));
    }

    @Test
    void insufficientStock() throws Exception {
        mockMvc.perform(get("/stock"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient Stock"));
    }

    @Test
    void invalidFile() throws Exception {
        mockMvc.perform(get("/file"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void maxUpload() throws Exception {
        mockMvc.perform(get("/upload"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File Too Large"));
    }

    @Test
    void dataIntegrity() throws Exception {
        mockMvc.perform(get("/integrity"))
                .andExpect(status().isConflict());
    }

    @Test
    void genericException() throws Exception {
        mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
