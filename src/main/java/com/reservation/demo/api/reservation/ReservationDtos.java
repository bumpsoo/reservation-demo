package com.reservation.demo.api.reservation;

import com.reservation.demo.domain.reservation.ReservationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public final class ReservationDtos {

    private ReservationDtos() {
    }

    public record CreateReservationRequest(
            @NotNull Long performanceId,
            @NotEmpty List<Long> seatInventoryIds
    ) {
    }

    public record ReservationSeatResponse(
            Long seatInventoryId,
            String section,
            String rowLabel,
            Integer seatNumber,
            Long price
    ) {
    }

    public record ReservationResponse(
            Long reservationId,
            Long performanceId,
            String concertTitle,
            LocalDateTime performanceAt,
            ReservationStatus status,
            Long totalAmount,
            LocalDateTime createdAt,
            List<ReservationSeatResponse> seats
    ) {
    }
}
