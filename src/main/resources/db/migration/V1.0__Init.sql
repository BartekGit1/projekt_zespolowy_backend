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