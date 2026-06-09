package com.reservation.demo.repository;

import com.reservation.demo.domain.reservation.Reservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.performance p
            JOIN FETCH p.concert
            WHERE r.user.id = :userId
            ORDER BY r.createdAt DESC
            """)
    List<Reservation> findAllByUserIdWithDetails(@Param("userId") Long userId);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.performance p
            JOIN FETCH p.concert
            JOIN FETCH r.items i
            JOIN FETCH i.seatInventory si
            JOIN FETCH si.seat
            WHERE r.id = :id AND r.user.id = :userId
            """)
    Optional<Reservation> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);
}
