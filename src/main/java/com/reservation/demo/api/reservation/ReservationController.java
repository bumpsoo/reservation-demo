package com.reservation.demo.api.reservation;

import com.reservation.demo.api.reservation.ReservationDtos.CreateReservationRequest;
import com.reservation.demo.api.reservation.ReservationDtos.ReservationResponse;
import com.reservation.demo.common.security.SecurityUtils;
import com.reservation.demo.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return reservationService.createReservation(userId, request);
    }

    @PostMapping("/{reservationId}/cancel")
    public ReservationResponse cancelReservation(@PathVariable Long reservationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return reservationService.cancelReservation(userId, reservationId);
    }

    @GetMapping("/me")
    public List<ReservationResponse> getMyReservations() {
        Long userId = SecurityUtils.getCurrentUserId();
        return reservationService.getMyReservations(userId);
    }
}
