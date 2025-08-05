-- =====================================================
-- Basic Data Correction System - Infrastructure
-- =====================================================

-- Create schema for data correction
CREATE SCHEMA IF NOT EXISTS data_correction;

-- =====================================================
-- 1. VALUE MAPPINGS TABLE (Country-specific)
-- Stores old value to new value mappings per country
-- =====================================================
CREATE TABLE IF NOT EXISTS data_correction.value_mappings (
    id SERIAL PRIMARY KEY,
    old_value TEXT NOT NULL,
    new_value TEXT NOT NULL,
    country VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure unique old value to new value mapping per country
    UNIQUE(old_value, new_value, country)
);

-- =====================================================
-- 2. TABLE MAPPINGS TABLE
-- Links value mappings to specific tables/columns using country join
-- =====================================================
CREATE TABLE IF NOT EXISTS data_correction.table_mappings (
    id SERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    column_name VARCHAR(100) NOT NULL,
    where_column VARCHAR(100) NOT NULL,
    where_value TEXT NOT NULL,
    country VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. EXECUTION LOG TABLE
-- Tracks what was updated and how many records
-- =====================================================
CREATE TABLE IF NOT EXISTS data_correction.execution_logs (
    id SERIAL PRIMARY KEY,
    table_mapping_id INTEGER, -- No foreign key constraint
    table_name VARCHAR(100) NOT NULL,
    column_name VARCHAR(100) NOT NULL,
    old_value TEXT NOT NULL,
    new_value TEXT NOT NULL,
    country VARCHAR(50) NOT NULL,
    records_affected INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'SUCCESS' CHECK (status IN ('SUCCESS', 'FAILED')),
    error_message TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 4. BACKUP MANAGEMENT TABLE
-- Tracks backup operations and restore points
-- =====================================================
CREATE TABLE IF NOT EXISTS data_correction.backup_management (
    id SERIAL PRIMARY KEY,
    backup_id VARCHAR(50) UNIQUE NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    backup_table_name VARCHAR(100) NOT NULL,
    backup_type VARCHAR(20) NOT NULL CHECK (backup_type IN ('BEFORE_UPDATE', 'FULL_BACKUP')),
    country VARCHAR(50),
    records_backed_up INTEGER DEFAULT 0,
    backup_status VARCHAR(20) DEFAULT 'PENDING' CHECK (backup_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by VARCHAR(100) DEFAULT CURRENT_USER
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_value_mappings_old_new_country ON data_correction.value_mappings(old_value, new_value, country);
CREATE INDEX IF NOT EXISTS idx_value_mappings_country ON data_correction.value_mappings(country);
CREATE INDEX IF NOT EXISTS idx_table_mappings_country ON data_correction.table_mappings(country);
CREATE INDEX IF NOT EXISTS idx_table_mappings_status ON data_correction.table_mappings(status);
CREATE INDEX IF NOT EXISTS idx_table_mappings_table_column ON data_correction.table_mappings(table_name, column_name);
CREATE INDEX IF NOT EXISTS idx_execution_logs_country ON data_correction.execution_logs(country);
CREATE INDEX IF NOT EXISTS idx_backup_management_table ON data_correction.backup_management(table_name);
CREATE INDEX IF NOT EXISTS idx_backup_management_status ON data_correction.backup_management(backup_status);

-- =====================================================
-- GRANT PERMISSIONS
-- =====================================================
GRANT ALL PRIVILEGES ON SCHEMA data_correction TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA data_correction TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA data_correction TO postgres;

DO $$
BEGIN
    RAISE NOTICE 'Basic data correction tables created successfully';
    RAISE NOTICE 'value_mappings: stores old->new value pairs per country';
    RAISE NOTICE 'table_mappings: links to specific tables/columns using country join';
    RAISE NOTICE 'execution_logs: tracks execution results';
    RAISE NOTICE 'backup_management: tracks backup operations and restore points';
    RAISE NOTICE 'No foreign key constraints - tables can operate independently';
END $$; 