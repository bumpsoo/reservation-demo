package com.reservation.demo.service;

import com.reservation.demo.api.organizer.OrganizerDtos.CreateConcertRequest;
import com.reservation.demo.api.organizer.OrganizerDtos.PerformanceTemplateRequest;
import com.reservation.demo.api.organizer.OrganizerDtos.SeatTemplateRequest;
import com.reservation.demo.api.reservation.ReservationDtos.CreateReservationRequest;
import com.reservation.demo.common.exception.BusinessException;
import com.reservation.demo.domain.user.Role;
import com.reservation.demo.domain.user.User;
import com.reservation.demo.repository.PerformanceRepository;
import com.reservation.demo.repository.SeatInventoryRepository;
import com.reservation.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Tag("integration")
class ReservationConcurrencyIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("reservation_demo_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrganizerService organizerService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private SeatInventoryRepository seatInventoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long userAId;
    private Long userBId;
    private Long performanceId;
    private Long targetSeatInventoryId;

    @BeforeEach
    void setUp() {
        User organizer = userRepository.save(User.builder()
                .email("organizer-" + System.nanoTime() + "@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("Organizer")
                .role(Role.ORGANIZER)
                .build());

        userAId = userRepository.save(User.builder()
                .email("user-a-" + System.nanoTime() + "@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("User A")
                .role(Role.USER)
                .build()).getId();

        userBId = userRepository.save(User.builder()
                .email("user-b-" + System.nanoTime() + "@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("User B")
                .role(Role.USER)
                .build()).getId();

        var concertResponse = organizerService.createConcert(organizer.getId(), new CreateConcertRequest(
                "Test Concert",
                "Test Hall",
                "Concurrency test",
                List.of(new PerformanceTemplateRequest(LocalDateTime.now().plusDays(7))),
                List.of(new SeatTemplateRequest("A", "1", 1))
        ));

        performanceId = performanceRepository.findAllByConcertId(concertResponse.concertId()).getFirst().getId();
        targetSeatInventoryId = seatInventoryRepository.findAllByPerformanceId(performanceId).getFirst().getId();
    }

    @Test
    void onlyOneReservationSucceedsForSameSeat() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            Long userId = i % 2 == 0 ? userAId : userBId;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    reservationService.createReservation(
                            userId,
                            new CreateReservationRequest(performanceId, List.of(targetSeatInventoryId))
                    );
                    successCount.incrementAndGet();
                } catch (BusinessException exception) {
                    conflictCount.incrementAndGet();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threadCount - 1);
    }
}
