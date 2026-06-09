package com.reservation.demo.repository;

import com.reservation.demo.domain.concert.Concert;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

    @Query("SELECT c FROM Concert c JOIN FETCH c.organizer ORDER BY c.createdAt DESC")
    List<Concert> findAllWithOrganizer();

    @Query("SELECT c FROM Concert c JOIN FETCH c.organizer WHERE c.id = :id")
    Optional<Concert> findByIdWithOrganizer(@Param("id") Long id);
}
