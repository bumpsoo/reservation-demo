CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE concerts (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    organizer_id  BIGINT       NOT NULL,
    title         VARCHAR(200) NOT NULL,
    venue         VARCHAR(200) NOT NULL,
    description   TEXT,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_concerts_organizer_id (organizer_id),
    CONSTRAINT fk_concerts_organizer FOREIGN KEY (organizer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE performances (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    concert_id      BIGINT      NOT NULL,
    performance_at  DATETIME(6) NOT NULL,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_performances_concert_id (concert_id),
    KEY idx_performances_performance_at (performance_at),
    CONSTRAINT fk_performances_concert FOREIGN KEY (concert_id) REFERENCES concerts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE seats (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    concert_id  BIGINT      NOT NULL,
    section     VARCHAR(50) NOT NULL,
    row_label   VARCHAR(10) NOT NULL,
    seat_number INT         NOT NULL,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_seats_concert_section_row_seat (concert_id, section, row_label, seat_number),
    KEY idx_seats_concert_id (concert_id),
    CONSTRAINT fk_seats_concert FOREIGN KEY (concert_id) REFERENCES concerts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE seat_inventories (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    performance_id  BIGINT      NOT NULL,
    seat_id         BIGINT      NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_seat_inventories_performance_seat (performance_id, seat_id),
    KEY idx_seat_inventories_status (status),
    CONSTRAINT fk_seat_inventories_performance FOREIGN KEY (performance_id) REFERENCES performances (id),
    CONSTRAINT fk_seat_inventories_seat FOREIGN KEY (seat_id) REFERENCES seats (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reservations (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    performance_id   BIGINT       NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    total_amount     BIGINT       NOT NULL DEFAULT 0,
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_reservations_user_id (user_id),
    KEY idx_reservations_performance_id (performance_id),
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reservations_performance FOREIGN KEY (performance_id) REFERENCES performances (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reservation_items (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id    BIGINT NOT NULL,
    seat_inventory_id BIGINT NOT NULL,
    price             BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservation_items_inventory (seat_inventory_id),
    KEY idx_reservation_items_reservation_id (reservation_id),
    CONSTRAINT fk_reservation_items_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
    CONSTRAINT fk_reservation_items_inventory FOREIGN KEY (seat_inventory_id) REFERENCES seat_inventories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
