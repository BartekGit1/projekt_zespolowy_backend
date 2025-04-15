CREATE TABLE users
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username       VARCHAR(255) NOT NULL UNIQUE,
    email          VARCHAR(255) NOT NULL UNIQUE,
    account_status VARCHAR(50)  NOT NULL,
    role           VARCHAR(50)  NOT NULL
);

INSERT INTO users (username, email, account_status, role)
VALUES ('mariasilva', 'bartoszzuchora@o2.pl', 'ACTIVE', 'CUSTOMER'),
       ('johndoe', 'clerniseq@gmail.com', 'ACTIVE', 'ADMIN');

CREATE TABLE auctions
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    version        bigint         not null,
    title          VARCHAR(255)   NOT NULL,
    description    TEXT           NOT NULL,
    starting_price DECIMAL(10, 2) NOT NULL,
    current_price  DECIMAL(10, 2) NOT NULL,
    end_date       TIMESTAMP      NOT NULL,
    user_id        BIGINT         NOT NULL,
    is_promoted    BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished       boolean        NOT NULL,
    paid           BOOLEAN        NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE bids
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    auction_id BIGINT         NOT NULL,
    user_id    BIGINT         NOT NULL,
    amount     DECIMAL(10, 2) NOT NULL,
    bid_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE premium_subscriptions
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id    BIGINT       NOT NULL,
    start_date TIMESTAMP    NOT NULL,
    end_date   TIMESTAMP    NOT NULL,
    payment_id VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE watchlist
(
    user_id    BIGINT NOT NULL,
    auction_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, auction_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (auction_id) REFERENCES auctions (id)
);

CREATE TABLE auto_bids
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id    BIGINT         NOT NULL,
    auction_id BIGINT         NOT NULL,
    max_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active     BOOLEAN        NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (auction_id) REFERENCES auctions (id)
);

CREATE TABLE payments
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id    BIGINT         NOT NULL,
    amount     DECIMAL(10, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE auction_payments
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    auction_id BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    payment_id BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (payment_id) REFERENCES payments (id)
);