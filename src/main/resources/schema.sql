
-- This file is the canonical schema. At startup the application also calls
-- DBConnection.createTables(), which creates these same tables idempotently
-- (CREATE TABLE IF NOT EXISTS) and runs small migrations for older databases.
-- You can apply this file directly with:  psql -d <db> -f schema.sql

-- students
-- One row per registered student. Login is by student_id + pin.
-- The student↔wallet link is held ONLY by wallets.student_id (1:1),
-- so there is intentionally no wallet_id column here.

CREATE TABLE IF NOT EXISTS students (
    student_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) UNIQUE,
    phone      VARCHAR(20)  UNIQUE,
    pin        INT
);

-- wallets
-- Exactly one wallet per student (student_id is UNIQUE → 1:1).
-- balance_cap          : maximum balance the wallet may hold
-- daily_transfer_limit : max total that may be transferred per day
-- today_transferred    : running total transferred today

CREATE TABLE IF NOT EXISTS wallets (
    wallet_id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id           INT NOT NULL UNIQUE,
    balance              DOUBLE PRECISION NOT NULL,
    daily_transfer_limit DOUBLE PRECISION NOT NULL,
    balance_cap          DOUBLE PRECISION NOT NULL,
    today_transferred    DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);

--
-- transactions
-- A complete record of every wallet/payment movement.
-- txn_id      : business UUID for the transaction — the primary key
-- sender_id   : student whose wallet is debited
-- receiver_id : student credited; 0 for campus payments (no payee)
-- type        : TRANSFER | DEPOSIT | WITHDRAW | CAMPUS_PAYMENT | SPLIT_SETTLEMENT
-- category    : campus-payment label (CANTEEN, LIBRARY, HOSTEL, ...); NULL otherwise
-- No FKs on sender/receiver because campus payments use receiver_id = 0.

CREATE TABLE IF NOT EXISTS transactions (
    txn_id      VARCHAR(36) PRIMARY KEY,
    sender_id   INT,
    receiver_id INT,
    amount      DOUBLE PRECISION NOT NULL,
    type        VARCHAR(50) NOT NULL,
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20) NOT NULL,
    category    VARCHAR(50)
);

-- dues
-- Splitwise-style pending dues. Each row is one member owing the payer
-- a share of a split expense (grouped by expense_id, a UUID string).
-- status = TRUE once the due has been settled.
-
CREATE TABLE IF NOT EXISTS dues (
    due_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    expense_id VARCHAR(36) NOT NULL,
    payer_id   INT NOT NULL,
    payee_id   INT NOT NULL,
    amount     DOUBLE PRECISION NOT NULL,
    status     BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (payer_id) REFERENCES students(student_id),
    FOREIGN KEY (payee_id) REFERENCES students(student_id)
);
