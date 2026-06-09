package com.reservation.demo.api.organizer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

public final class OrganizerDtos {

    private OrganizerDtos() {
    }

    public record SeatTemplateRequest(
            @NotBlank String section,
            @NotBlank String rowLabel,
            @NotNull @Positive Integer seatNumber
    ) {
    }

    public record PerformanceTemplateRequest(
            @NotNull @Future LocalDateTime performanceAt
    ) {
    }

    public record CreateConcertRequest(
            @NotBlank String title,
            @NotBlank String venue,
            String description,
            @NotEmpty @Valid List<PerformanceTemplateRequest> performances,
            @NotEmpty @Valid List<SeatTemplateRequest> seats
    ) {
    }

    public record CreateConcertResponse(
            Long concertId,
            int performanceCount,
            int seatCount
    ) {
    }
}
