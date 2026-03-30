package com.pharmacy.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OutboxService outboxService;

    @BeforeEach
    void wireMapper() {
        org.springframework.test.util.ReflectionTestUtils.setField(outboxService, "objectMapper", objectMapper);
    }

    @Test
    void saveEvent_serializesAndPersists() {
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> {
            OutboxEvent e = inv.getArgument(0);
            e.setId(99L);
            return e;
        });

        OutboxEvent saved = outboxService.saveEvent("Order", "1", "PLACED", java.util.Map.of("k", "v"));

        assertEquals(99L, saved.getId());
        ArgumentCaptor<OutboxEvent> cap = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(cap.capture());
        assertEquals("Order", cap.getValue().getAggregateType());
        assertEquals(OutboxEvent.OutboxStatus.PENDING, cap.getValue().getStatus());
        assertTrue(cap.getValue().getPayload().contains("k"));
    }

    @Test
    void saveEvent_invalidPayloadThrows() {
        class BadPayload {
            @SuppressWarnings("unused")
            public String getBroken() {
                throw new RuntimeException("no");
            }
        }
        BadPayload badPayload = new BadPayload();
        assertThrows(RuntimeException.class, () -> outboxService.saveEvent("A", "1", "E", badPayload));
    }

    @Test
    void markCompleted_updatesWhenPresent() {
        OutboxEvent e = OutboxEvent.builder().id(1L).eventId("e1").status(OutboxEvent.OutboxStatus.PENDING).build();
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(e));
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        outboxService.markCompleted(1L);

        verify(outboxEventRepository).save(any());
    }

    @Test
    void markCompleted_noopWhenMissing() {
        when(outboxEventRepository.findById(2L)).thenReturn(Optional.empty());
        outboxService.markCompleted(2L);
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void markFailed_updatesWhenPresent() {
        OutboxEvent e = OutboxEvent.builder()
                .id(3L)
                .eventId("e3")
                .status(OutboxEvent.OutboxStatus.PENDING)
                .retryCount(0)
                .build();
        when(outboxEventRepository.findById(3L)).thenReturn(Optional.of(e));
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        outboxService.markFailed(3L, "oops");

        verify(outboxEventRepository).save(any());
    }

    @Test
    void cleanupOldEvents_deletes() {
        when(outboxEventRepository.deleteOldCompletedEvents(any(LocalDateTime.class))).thenReturn(4);

        outboxService.cleanupOldEvents(30);

        verify(outboxEventRepository).deleteOldCompletedEvents(any(LocalDateTime.class));
    }
}
