package com.reservation.demo.repository;

import com.reservation.demo.domain.seat.SeatInventory;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {

    List<SeatInventory> findAllByPerformanceId(Long performanceId);

    @Query("""
            SELECT si FROM SeatInventory si
            JOIN FETCH si.seat s
            WHERE si.performance.id = :performanceId
            ORDER BY s.section, s.rowLabel, s.seatNumber
            """)
    List<SeatInventory> findAllByPerformanceIdWithSeat(@Param("performanceId") Long performanceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT si FROM SeatInventory si
            JOIN FETCH si.seat
            WHERE si.id IN :ids
            ORDER BY si.id
            """)
    List<SeatInventory> findAllByIdInForUpdate(@Param("ids") List<Long> ids);
}
