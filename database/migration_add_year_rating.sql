-- Standalone Migration: Add published_year and rating columns to books table
-- PageMind Database Migration

USE pagemind;

ALTER TABLE books
ADD COLUMN published_year INT NULL,
ADD COLUMN rating DECIMAL(2,1) NULL CHECK (rating >= 0.0 AND rating <= 5.0);
