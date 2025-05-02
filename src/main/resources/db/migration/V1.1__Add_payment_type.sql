alter table payments
    add column payment_type varchar(30);

INSERT INTO users (username, email, account_status, role)
VALUES ('janekowalski', 'bartoszzuchora1@o2.pl', 'ACTIVE', 'PREMIUM') ON CONFLICT (username) DO NOTHING;