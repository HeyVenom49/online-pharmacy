package com.pharmacy.common.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' OR (e.status = 'FAILED' AND e.retryCount < :maxRetries) ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEvents(@Param("maxRetries") int maxRetries);

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findAllPending();

    List<OutboxEvent> findByAggregateTypeAndAggregateId(String aggregateType, String aggregateId);

    boolean existsByEventId(String eventId);

    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.status = 'COMPLETED' AND e.publishedAt < :before")
    int deleteOldCompletedEvents(@Param("before") LocalDateTime before);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'PENDING' WHERE e.status = 'PROCESSING' AND e.createdAt < :timeout")
    int resetStaleProcessingEvents(@Param("timeout") LocalDateTime timeout);
}
