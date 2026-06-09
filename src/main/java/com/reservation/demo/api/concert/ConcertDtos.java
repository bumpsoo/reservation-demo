package com.reservation.demo.api.concert;

import java.time.LocalDateTime;
import java.util.List;

public final class ConcertDtos {

    private ConcertDtos() {
    }

    public record ConcertSummaryResponse(
            Long id,
            String title,
            String venue,
            String organizerName
    ) {
    }

    public record PerformanceResponse(
            Long id,
            LocalDateTime performanceAt
    ) {
    }

    public record ConcertDetailResponse(
            Long id,
            String title,
            String venue,
            String description,
            String organizerName,
            List<PerformanceResponse> performances
    ) {
    }

    public record SeatMapItemResponse(
            Long seatInventoryId,
            Long seatId,
            String section,
            String rowLabel,
            Integer seatNumber,
            String status
    ) {
    }

    public record SeatMapResponse(
            Long performanceId,
            Long concertId,
            String concertTitle,
            LocalDateTime performanceAt,
            List<SeatMapItemResponse> seats
    ) {
    }
}
