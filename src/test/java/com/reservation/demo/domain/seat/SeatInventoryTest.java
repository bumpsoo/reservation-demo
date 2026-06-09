package com.reservation.demo.domain.seat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeatInventoryTest {

    @Test
    void marksSoldAndAvailable() {
        SeatInventory inventory = SeatInventory.builder()
                .performance(null)
                .seat(null)
                .status(SeatInventoryStatus.AVAILABLE)
                .build();

        inventory.markSold();
        assertThat(inventory.getStatus()).isEqualTo(SeatInventoryStatus.SOLD);
        assertThat(inventory.isAvailable()).isFalse();

        inventory.markAvailable();
        assertThat(inventory.getStatus()).isEqualTo(SeatInventoryStatus.AVAILABLE);
        assertThat(inventory.isAvailable()).isTrue();
    }
}
