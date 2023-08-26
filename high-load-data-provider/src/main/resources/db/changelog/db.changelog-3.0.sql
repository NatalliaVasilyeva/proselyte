--liquibase formatted sql
--changeset natallia.vasilyeva:db.changelog-3.0 splitStatements:false logicalFilePath:classpath:/db/changelog/db.changelog-3.0.sql

CREATE INDEX IF NOT EXISTS index_symbol ON stock (symbol);