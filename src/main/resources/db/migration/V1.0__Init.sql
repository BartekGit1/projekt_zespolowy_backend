CREATE TABLE IF NOT EXISTS users
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username       VARCHAR(255) NOT NULL UNIQUE,
    email          VARCHAR(255) NOT NULL UNIQUE,
    account_status VARCHAR(50)  NOT NULL,
    role           VARCHAR(50)  NOT NULL
);

INSERT INTO users (username, email, account_status, role)
VALUES ('mariasilva', 'bartoszzuchora@o2.pl', 'ACTIVE', 'CUSTOMER'),
       ('johndoe', 'clerniseq@gmail.com', 'ACTIVE', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

CREATE TABLE IF NOT EXISTS payments
(
    id                      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    amount                  DECIMAL(10, 2) NOT NULL,
    status                  VARCHAR(20)    NOT NULL,
    created_at              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id                 BIGINT         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS auctions
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    version        bigint         not null,
    title          VARCHAR(255)   NOT NULL,
    description    TEXT           NOT NULL,
    starting_price DECIMAL(10, 2) NOT NULL,
    end_date       TIMESTAMP      NOT NULL,
    is_promoted    BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished       boolean        NOT NULL,
    paid           BOOLEAN        NOT NULL DEFAULT FALSE,
    uri            varchar(500),
    user_id        BIGINT         NOT NULL,
    payment_id     BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (payment_id) REFERENCES payments (id)
);

CREATE TABLE IF NOT EXISTS bids
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    amount     DECIMAL(10, 2) NOT NULL,
    bid_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id    BIGINT         NOT NULL,
    auction_id BIGINT         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (auction_id) REFERENCES auctions (id)
);
CREATE INDEX IF NOT EXISTS idx_auction_user ON bids (auction_id, user_id);

CREATE TABLE IF NOT EXISTS premium_subscriptions
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    start_date TIMESTAMP    NOT NULL,
    end_date   TIMESTAMP    NOT NULL,
    user_id    BIGINT       NOT NULL,
    payment_id BIGINT       NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (payment_id) REFERENCES payments (id)
);
CREATE INDEX IF NOT EXISTS premium_subscriptions_payment_id_unique ON premium_subscriptions (payment_id) WHERE payment_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS watchlist
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id    BIGINT NOT NULL,
    auction_id BIGINT NOT NULL,
    CONSTRAINT unique_user_auction UNIQUE (user_id, auction_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (auction_id) REFERENCES auctions (id)
);
CREATE INDEX IF NOT EXISTS idx_user_auction ON watchlist (user_id, auction_id);

CREATE TABLE IF NOT EXISTS auto_bids
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    max_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active     BOOLEAN        NOT NULL DEFAULT TRUE,
    user_id    BIGINT         NOT NULL,
    auction_id BIGINT         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (auction_id) REFERENCES auctions (id)
);