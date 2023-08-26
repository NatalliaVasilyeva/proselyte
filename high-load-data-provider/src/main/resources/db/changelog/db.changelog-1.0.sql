--liquibase formatted sql
--changeset natallia.vasilyeva:db.changelog-1.0 splitStatements:false logicalFilePath:classpath:/db/changelog/db.changelog-1.0.sql

CREATE TABLE IF NOT EXISTS users (
                       id         SERIAL PRIMARY KEY,
                       username   VARCHAR(64)   NOT NULL UNIQUE,
                       password   VARCHAR(2048) NOT NULL,
                       role       VARCHAR(32)   NOT NULL,
                       enabled    BOOLEAN       NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP
);