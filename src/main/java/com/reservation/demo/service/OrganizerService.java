package com.reservation.demo.service;

import com.reservation.demo.api.organizer.OrganizerDtos.CreateConcertRequest;
import com.reservation.demo.api.organizer.OrganizerDtos.CreateConcertResponse;
import com.reservation.demo.common.exception.BusinessException;
import com.reservation.demo.common.exception.ErrorCode;
import com.reservation.demo.domain.concert.Concert;
import com.reservation.demo.domain.concert.Performance;
import com.reservation.demo.domain.seat.Seat;
import com.reservation.demo.domain.seat.SeatInventory;
import com.reservation.demo.domain.seat.SeatInventoryStatus;
import com.reservation.demo.domain.user.User;
import com.reservation.demo.repository.ConcertRepository;
import com.reservation.demo.repository.PerformanceRepository;
import com.reservation.demo.repository.SeatInventoryRepository;
import com.reservation.demo.repository.SeatRepository;
import com.reservation.demo.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizerService {

    private final UserRepository userRepository;
    private final ConcertRepository concertRepository;
    private final PerformanceRepository performanceRepository;
    private final SeatRepository seatRepository;
    private final SeatInventoryRepository seatInventoryRepository;

    public OrganizerService(
            UserRepository userRepository,
            ConcertRepository concertRepository,
            PerformanceRepository performanceRepository,
            SeatRepository seatRepository,
            SeatInventoryRepository seatInventoryRepository
    ) {
        this.userRepository = userRepository;
        this.concertRepository = concertRepository;
        this.performanceRepository = performanceRepository;
        this.seatRepository = seatRepository;
        this.seatInventoryRepository = seatInventoryRepository;
    }

    @Transactional
    public CreateConcertResponse createConcert(Long organizerId, CreateConcertRequest request) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "주최사를 찾을 수 없습니다."));

        Concert concert = Concert.builder()
                .organizer(organizer)
                .title(request.title())
                .venue(request.venue())
                .description(request.description())
                .build();
        concertRepository.save(concert);

        List<Seat> seats = new ArrayList<>();
        for (var seatTemplate : request.seats()) {
            Seat seat = Seat.builder()
                    .concert(concert)
                    .section(seatTemplate.section())
                    .rowLabel(seatTemplate.rowLabel())
                    .seatNumber(seatTemplate.seatNumber())
                    .build();
            seats.add(seatRepository.save(seat));
        }

        List<Performance> performances = new ArrayList<>();
        for (var performanceTemplate : request.performances()) {
            Performance performance = Performance.builder()
                    .concert(concert)
                    .performanceAt(performanceTemplate.performanceAt())
                    .build();
            performances.add(performanceRepository.save(performance));
        }

        for (Performance performance : performances) {
            for (Seat seat : seats) {
                SeatInventory inventory = SeatInventory.builder()
                        .performance(performance)
                        .seat(seat)
                        .status(SeatInventoryStatus.AVAILABLE)
                        .build();
                seatInventoryRepository.save(inventory);
            }
        }

        return new CreateConcertResponse(concert.getId(), performances.size(), seats.size());
    }
}
