-- Standalone Migration: Add unique constraint on (title, author) to books table
-- Prevents duplicate book entries in the PageMind database

USE pagemind;

ALTER TABLE books ADD CONSTRAINT uq_title_author UNIQUE (title, author);
