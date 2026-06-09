package com.reservation.demo.repository;

import com.reservation.demo.domain.concert.Performance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    List<Performance> findAllByConcertId(Long concertId);

    @Query("SELECT p FROM Performance p JOIN FETCH p.concert WHERE p.id = :id")
    Optional<Performance> findByIdWithConcert(@Param("id") Long id);
}
