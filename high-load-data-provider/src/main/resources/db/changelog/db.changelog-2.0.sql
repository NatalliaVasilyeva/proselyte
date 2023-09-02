--liquibase formatted sql
--changeset natallia.vasilyeva:db.changelog-2.0 splitStatements:false logicalFilePath:classpath:/db/changelog/db.changelog-2.0.sql

CREATE TABLE IF NOT EXISTS stock (
                        id                  SERIAL            PRIMARY KEY,
                        symbol              VARCHAR(128)      NOT NULL,
                        price               NUMERIC           NOT NULL,
                        is_privilege        BOOLEAN           NOT NULL,
                        company_id          NUMERIC           NOT NULL,
                        created_at          TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS company (
                       id                   SERIAL            PRIMARY KEY,
                       name                 VARCHAR(64)       NOT NULL UNIQUE,
                       enabled              BOOLEAN           NOT NULL DEFAULT FALSE,
                       symbol               VARCHAR(16)       NOT NULL UNIQUE,
                       created_at           TIMESTAMP,
                       updated_at           TIMESTAMP
);