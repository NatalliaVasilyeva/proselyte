--liquibase formatted sql
--changeset natallia.vasilyeva:db.changelog-4.0 splitStatements:false logicalFilePath:classpath:/db/changelog/db.changelog-4.0.sql

CREATE TABLE IF NOT EXISTS apikey (
                        id                  SERIAL            PRIMARY KEY,
                        username            VARCHAR(64)       NOT NULL UNIQUE,
                        apikey              VARCHAR(64)       NOT NULL,
                        created_at          TIMESTAMP
    );