package com.reservation.demo.service;

import com.reservation.demo.api.concert.ConcertDtos.ConcertDetailResponse;
import com.reservation.demo.api.concert.ConcertDtos.ConcertSummaryResponse;
import com.reservation.demo.api.concert.ConcertDtos.PerformanceResponse;
import com.reservation.demo.api.concert.ConcertDtos.SeatMapItemResponse;
import com.reservation.demo.api.concert.ConcertDtos.SeatMapResponse;
import com.reservation.demo.common.exception.BusinessException;
import com.reservation.demo.common.exception.ErrorCode;
import com.reservation.demo.domain.seat.SeatInventory;
import com.reservation.demo.repository.ConcertRepository;
import com.reservation.demo.repository.PerformanceRepository;
import com.reservation.demo.repository.SeatInventoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final PerformanceRepository performanceRepository;
    private final SeatInventoryRepository seatInventoryRepository;

    public ConcertService(
            ConcertRepository concertRepository,
            PerformanceRepository performanceRepository,
            SeatInventoryRepository seatInventoryRepository
    ) {
        this.concertRepository = concertRepository;
        this.performanceRepository = performanceRepository;
        this.seatInventoryRepository = seatInventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ConcertSummaryResponse> getConcerts() {
        return concertRepository.findAllWithOrganizer().stream()
                .map(concert -> new ConcertSummaryResponse(
                        concert.getId(),
                        concert.getTitle(),
                        concert.getVenue(),
                        concert.getOrganizer().getName()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConcertDetailResponse getConcertDetail(Long concertId) {
        var concert = concertRepository.findByIdWithOrganizer(concertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다."));

        List<PerformanceResponse> performances = performanceRepository.findAllByConcertId(concertId).stream()
                .map(performance -> new PerformanceResponse(
                        performance.getId(),
                        performance.getPerformanceAt()
                ))
                .toList();

        return new ConcertDetailResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getVenue(),
                concert.getDescription(),
                concert.getOrganizer().getName(),
                performances
        );
    }

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long performanceId) {
        var performance = performanceRepository.findByIdWithConcert(performanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회차를 찾을 수 없습니다."));

        List<SeatMapItemResponse> seats = seatInventoryRepository.findAllByPerformanceIdWithSeat(performanceId).stream()
                .map(this::toSeatMapItem)
                .toList();

        var concert = performance.getConcert();
        return new SeatMapResponse(
                performance.getId(),
                concert.getId(),
                concert.getTitle(),
                performance.getPerformanceAt(),
                seats
        );
    }

    private SeatMapItemResponse toSeatMapItem(SeatInventory inventory) {
        var seat = inventory.getSeat();
        return new SeatMapItemResponse(
                inventory.getId(),
                seat.getId(),
                seat.getSection(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                inventory.getStatus().name()
        );
    }
}
