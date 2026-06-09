package com.reservation.demo.repository;

import com.reservation.demo.domain.seat.Seat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByConcertId(Long concertId);
}
