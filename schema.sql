-- IgirePay Database Schema
-- Run this file once against your PostgreSQL igirepay_db database

CREATE TABLE IF NOT EXISTS customers (
    id              SERIAL PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    phone_number    VARCHAR(20)  UNIQUE NOT NULL,
    pin             VARCHAR(255) NOT NULL,
    role            VARCHAR(10)  NOT NULL DEFAULT 'USER',
    failed_attempts INT          NOT NULL DEFAULT 0,
    is_locked       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS accounts (
    id           SERIAL PRIMARY KEY,
    customer_id  INT            NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    account_type VARCHAR(20)    NOT NULL,
    balance      NUMERIC(15, 2) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transactions (
    id               SERIAL PRIMARY KEY,
    account_id       INT            NOT NULL REFERENCES accounts(id),
    reference_id     VARCHAR(100)   NOT NULL,
    transaction_type VARCHAR(30)    NOT NULL,
    amount           NUMERIC(15, 2) NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS processed_requests (
    id           SERIAL PRIMARY KEY,
    reference_id VARCHAR(100) UNIQUE NOT NULL,
    processed_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS loans (
    id           SERIAL PRIMARY KEY,
    customer_id  INT            NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    amount       NUMERIC(15, 2) NOT NULL,
    reason       TEXT           NOT NULL,
    status       VARCHAR(10)    NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP      NOT NULL DEFAULT NOW()
);
