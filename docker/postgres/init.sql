-- PostgreSQL initialization script for sample_db
-- This script runs automatically when the container starts for the first time

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Set timezone
SET timezone = 'UTC';

-- Create additional schemas if needed
-- CREATE SCHEMA IF NOT EXISTS app_schema;

-- Example: Create a simple health check table for connection pool testing
CREATE TABLE IF NOT EXISTS health_check (
    id SERIAL PRIMARY KEY,
    status VARCHAR(20) DEFAULT 'OK',
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial health check record
INSERT INTO health_check (status) VALUES ('INITIALIZED') ON CONFLICT DO NOTHING;

-- Create action_items table with uniqueId constraint
CREATE TABLE IF NOT EXISTS action_items (
    id VARCHAR(255) PRIMARY KEY,
    unique_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assignee VARCHAR(255),
    category VARCHAR(100) NOT NULL,
    type_code VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create index on unique_id for better performance
CREATE INDEX IF NOT EXISTS idx_action_items_unique_id ON action_items(unique_id);

-- Create index on category and type_code for validation queries
CREATE INDEX IF NOT EXISTS idx_action_items_category_type ON action_items(category, type_code);

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE sample_db TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'Database sample_db initialized successfully';
END $$; 