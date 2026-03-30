package com.pharmacy.common.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void successWithData() {
        ApiResponse<String> r = ApiResponse.success("x");
        assertTrue(r.isSuccess());
        assertEquals("Success", r.getMessage());
        assertEquals("x", r.getData());
    }

    @Test
    void successWithMessageAndData() {
        ApiResponse<List<String>> r = ApiResponse.success("ok", List.of("a"));
        assertTrue(r.isSuccess());
        assertEquals("ok", r.getMessage());
        assertEquals(1, r.getData().size());
    }

    @Test
    void errorStatic() {
        ApiResponse<Void> r = ApiResponse.error("bad");
        assertFalse(r.isSuccess());
        assertEquals("bad", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void builderAndSetters() {
        ApiResponse<Integer> r = ApiResponse.<Integer>builder()
                .success(true)
                .message("m")
                .data(7)
                .build();
        assertEquals(7, r.getData());
        r.setSuccess(false);
        r.setMessage("n");
        r.setData(null);
        assertFalse(r.isSuccess());
    }
}
