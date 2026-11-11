BEGIN;

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL CHECK (btrim(name) <> ''),
    email VARCHAR(254) NOT NULL UNIQUE CHECK (email = lower(email)),
    password_hash TEXT NOT NULL,
    role VARCHAR(10) NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS courts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL CHECK (btrim(name) <> ''),
    hourly_rate BIGINT NOT NULL CHECK (hourly_rate BETWEEN 1 AND 499999999),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (open_time < close_time AND close_time < TIME '24:00'),
    CHECK (EXTRACT(MINUTE FROM open_time) = 0 AND EXTRACT(SECOND FROM open_time) = 0),
    CHECK (EXTRACT(MINUTE FROM close_time) = 0 AND EXTRACT(SECOND FROM close_time) = 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS courts_name_key ON courts (lower(name));

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts (id),
    court_id BIGINT NOT NULL REFERENCES courts (id),
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    amount BIGINT NOT NULL CHECK (amount BETWEEN 1 AND 499999999),
    status VARCHAR(24) NOT NULL CHECK (status IN (
        'HELD', 'PENDING_CONFIRMATION', 'CONFIRMED', 'CHECKED_IN', 'REJECTED', 'CANCELLED', 'EXPIRED'
    )),
    expires_at TIMESTAMPTZ NOT NULL,
    ticket_token UUID UNIQUE,
    CHECK (start_time < TIME '23:00' AND EXTRACT(MINUTE FROM start_time) = 0
           AND EXTRACT(SECOND FROM start_time) = 0),
    CHECK ((status IN ('CONFIRMED', 'CHECKED_IN')) = (ticket_token IS NOT NULL))
);

-- Every slot is one hour and begins on the hour. Keep closed bookings for history.
CREATE UNIQUE INDEX IF NOT EXISTS bookings_active_slot_key
    ON bookings (court_id, booking_date, start_time)
    WHERE status IN ('HELD', 'PENDING_CONFIRMATION', 'CONFIRMED', 'CHECKED_IN');

CREATE INDEX IF NOT EXISTS bookings_account_history ON bookings (account_id, id DESC);
CREATE INDEX IF NOT EXISTS bookings_daily_schedule ON bookings (booking_date, court_id, start_time);

INSERT INTO courts (name, hourly_rate, open_time, close_time)
SELECT name, hourly_rate, open_time, close_time
FROM (VALUES ('Sân 1', 80000, TIME '06:00', TIME '23:00'),
             ('Sân 2', 100000, TIME '06:00', TIME '23:00'))
    AS sample_courts (name, hourly_rate, open_time, close_time)
WHERE NOT EXISTS (SELECT 1 FROM courts)
ON CONFLICT DO NOTHING;

COMMIT;
