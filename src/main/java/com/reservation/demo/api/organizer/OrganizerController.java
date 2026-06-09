package com.reservation.demo.api.organizer;

import com.reservation.demo.api.organizer.OrganizerDtos.CreateConcertRequest;
import com.reservation.demo.api.organizer.OrganizerDtos.CreateConcertResponse;
import com.reservation.demo.common.security.SecurityUtils;
import com.reservation.demo.service.OrganizerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizer")
public class OrganizerController {

    private final OrganizerService organizerService;

    public OrganizerController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @PostMapping("/concerts")
    public CreateConcertResponse createConcert(@Valid @RequestBody CreateConcertRequest request) {
        Long organizerId = SecurityUtils.getCurrentUserId();
        return organizerService.createConcert(organizerId, request);
    }
}
