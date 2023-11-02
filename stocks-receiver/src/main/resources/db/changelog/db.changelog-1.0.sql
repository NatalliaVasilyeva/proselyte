--liquibase formatted sql
--changeset natallia.vasilyeva:db.changelog-1.0 splitStatements:false logicalFilePath:classpath:/db/changelog/db.changelog-1.0.sql

CREATE TABLE IF NOT EXISTS company (
                    id                   SERIAL            PRIMARY KEY,
                    name                 VARCHAR(64)       NOT NULL UNIQUE,
                    symbol               VARCHAR(16)       NOT NULL UNIQUE,
                    enabled              BOOLEAN           NOT NULL DEFAULT FALSE,
                    created_at           TIMESTAMP,
                    updated_at           TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS stock (
                    id                   SERIAL            PRIMARY KEY,
                    symbol               VARCHAR(16)       NOT NULL,
                    company_name         VARCHAR(256)      NOT NULL,
                    latest_price         NUMERIC(42,2)     NOT NULL,
                    change               NUMERIC(42,2)     NOT NULL,
                    change_percent       NUMERIC(42,2)     NOT NULL,
                    latest_update        TIMESTAMP
    );