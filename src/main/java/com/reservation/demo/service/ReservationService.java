package com.reservation.demo.service;

import com.reservation.demo.api.reservation.ReservationDtos.CreateReservationRequest;
import com.reservation.demo.api.reservation.ReservationDtos.ReservationResponse;
import com.reservation.demo.api.reservation.ReservationDtos.ReservationSeatResponse;
import com.reservation.demo.common.exception.BusinessException;
import com.reservation.demo.common.exception.ErrorCode;
import com.reservation.demo.domain.reservation.Reservation;
import com.reservation.demo.domain.reservation.ReservationItem;
import com.reservation.demo.domain.reservation.ReservationStatus;
import com.reservation.demo.domain.seat.SeatInventory;
import com.reservation.demo.domain.user.User;
import com.reservation.demo.repository.PerformanceRepository;
import com.reservation.demo.repository.ReservationRepository;
import com.reservation.demo.repository.SeatInventoryRepository;
import com.reservation.demo.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private static final long SEAT_PRICE = 80_000L;

    private final UserRepository userRepository;
    private final PerformanceRepository performanceRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(
            UserRepository userRepository,
            PerformanceRepository performanceRepository,
            SeatInventoryRepository seatInventoryRepository,
            ReservationRepository reservationRepository
    ) {
        this.userRepository = userRepository;
        this.performanceRepository = performanceRepository;
        this.seatInventoryRepository = seatInventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse createReservation(Long userId, CreateReservationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        var performance = performanceRepository.findByIdWithConcert(request.performanceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회차를 찾을 수 없습니다."));

        List<Long> sortedIds = request.seatInventoryIds().stream()
                .distinct()
                .sorted()
                .toList();

        if (sortedIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "예약할 좌석을 선택해 주세요.");
        }

        List<SeatInventory> inventories = seatInventoryRepository.findAllByIdInForUpdate(sortedIds);

        if (inventories.size() != sortedIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 좌석이 포함되어 있습니다.");
        }

        inventories.sort(Comparator.comparing(SeatInventory::getId));

        for (SeatInventory inventory : inventories) {
            if (!inventory.getPerformance().getId().equals(performance.getId())) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "다른 회차의 좌석이 포함되어 있습니다.");
            }
            if (!inventory.isAvailable()) {
                throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
            }
        }

        long totalAmount = SEAT_PRICE * inventories.size();

        Reservation reservation = Reservation.builder()
                .user(user)
                .performance(performance)
                .status(ReservationStatus.CONFIRMED)
                .totalAmount(totalAmount)
                .build();

        for (SeatInventory inventory : inventories) {
            inventory.markSold();
            ReservationItem item = ReservationItem.builder()
                    .reservation(reservation)
                    .seatInventory(inventory)
                    .price(SEAT_PRICE)
                    .build();
            reservation.addItem(item);
        }

        Reservation saved = reservationRepository.save(reservation);
        return toResponse(saved);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUserIdWithDetails(reservationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "예약을 찾을 수 없습니다."));

        if (!reservation.isConfirmed()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 취소된 예약입니다.");
        }

        reservation.cancel();
        for (ReservationItem item : List.copyOf(reservation.getItems())) {
            item.getSeatInventory().markAvailable();
        }

        ReservationResponse response = toResponse(reservation);
        reservation.getItems().clear();

        return response;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findAllByUserIdWithDetails(userId).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    private ReservationResponse toSummaryResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getPerformance().getId(),
                reservation.getPerformance().getConcert().getTitle(),
                reservation.getPerformance().getPerformanceAt(),
                reservation.getStatus(),
                reservation.getTotalAmount(),
                reservation.getCreatedAt(),
                List.of()
        );
    }

    private ReservationResponse toResponse(Reservation reservation) {
        List<ReservationSeatResponse> seats = reservation.getItems().stream()
                .map(item -> {
                    var seat = item.getSeatInventory().getSeat();
                    return new ReservationSeatResponse(
                            item.getSeatInventory().getId(),
                            seat.getSection(),
                            seat.getRowLabel(),
                            seat.getSeatNumber(),
                            item.getPrice()
                    );
                })
                .toList();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getPerformance().getId(),
                reservation.getPerformance().getConcert().getTitle(),
                reservation.getPerformance().getPerformanceAt(),
                reservation.getStatus(),
                reservation.getTotalAmount(),
                reservation.getCreatedAt(),
                seats
        );
    }
}
