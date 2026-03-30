package com.pharmacy.common.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseErrorResponseTest {

    @Test
    void pageResponseBuilder() {
        PageResponse<String> p = PageResponse.<String>builder()
                .content(List.of("a"))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        assertEquals(1, p.getContent().size());
        assertEquals(0, p.getPage());
        assertEquals(10, p.getSize());
        assertEquals(1, p.getTotalElements());
        assertTrue(p.isFirst());
        assertTrue(p.isLast());
    }

    @Test
    void pageResponseSetters() {
        PageResponse<Integer> p = new PageResponse<>();
        p.setContent(List.of(1));
        p.setPage(2);
        p.setSize(5);
        p.setTotalElements(100);
        p.setTotalPages(20);
        p.setFirst(false);
        p.setLast(false);
        assertEquals(2, p.getPage());
    }

    @Test
    void errorResponseBuilder() {
        ErrorResponse e = ErrorResponse.builder()
                .status(400)
                .error("Bad")
                .message("msg")
                .path("/p")
                .fieldErrors(List.of(ErrorResponse.FieldError.builder()
                        .field("f")
                        .message("m")
                        .build()))
                .build();
        assertEquals(400, e.getStatus());
        assertEquals(1, e.getFieldErrors().size());
    }

    @Test
    void fieldErrorBuilder() {
        ErrorResponse.FieldError fe = ErrorResponse.FieldError.builder()
                .field("email")
                .message("invalid")
                .build();
        assertEquals("email", fe.getField());
    }
}
