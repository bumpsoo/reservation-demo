package com.reservation.demo.api.concert;

import com.reservation.demo.api.concert.ConcertDtos.ConcertDetailResponse;
import com.reservation.demo.api.concert.ConcertDtos.ConcertSummaryResponse;
import com.reservation.demo.api.concert.ConcertDtos.SeatMapResponse;
import com.reservation.demo.service.ConcertService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/concerts")
public class ConcertController {

    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    @GetMapping
    public List<ConcertSummaryResponse> getConcerts() {
        return concertService.getConcerts();
    }

    @GetMapping("/{concertId}")
    public ConcertDetailResponse getConcert(@PathVariable Long concertId) {
        return concertService.getConcertDetail(concertId);
    }

    @GetMapping("/performances/{performanceId}/seats")
    public SeatMapResponse getSeatMap(@PathVariable Long performanceId) {
        return concertService.getSeatMap(performanceId);
    }
}
