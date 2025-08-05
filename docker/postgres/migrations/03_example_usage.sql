-- =====================================================
-- Basic Example Usage
-- =====================================================

-- =====================================================
-- STEP 1: CREATE SAMPLE TABLE
-- =====================================================

-- Create sample TEST table
CREATE TABLE IF NOT EXISTS test (
    id SERIAL PRIMARY KEY,
    std VARCHAR(100) NOT NULL,
    country VARCHAR(50) NOT NULL,
    description TEXT
);

-- Insert sample data
INSERT INTO test (std, country, description) VALUES
('OLD_STD_001', 'USA', 'Test record 1'),
('OLD_STD_002', 'USA', 'Test record 2'),
('OLD_STD_003', 'UK', 'Test record 3'),
('OLD_STD_004', 'UK', 'Test record 4');

-- =====================================================
-- STEP 2: ADD VALUE MAPPINGS (Country-specific)
-- =====================================================

-- Add value mappings for USA (country-specific)
SELECT data_correction.add_value_mapping('OLD_STD_001', 'NEW_STD_USA_001', 'USA');
SELECT data_correction.add_value_mapping('OLD_STD_002', 'NEW_STD_USA_002', 'USA');

-- Add value mappings for UK (country-specific)
SELECT data_correction.add_value_mapping('OLD_STD_003', 'NEW_STD_UK_001', 'UK');
SELECT data_correction.add_value_mapping('OLD_STD_004', 'NEW_STD_UK_002', 'UK');

-- =====================================================
-- STEP 3: ADD TABLE MAPPINGS (Links to specific tables using country join)
-- =====================================================

-- Add table mappings for USA
SELECT data_correction.add_table_mapping(
    'OLD_STD_001', 'NEW_STD_USA_001', 
    'test', 'std', 'country', 'USA', 'USA'
);
SELECT data_correction.add_table_mapping(
    'OLD_STD_002', 'NEW_STD_USA_002', 
    'test', 'std', 'country', 'USA', 'USA'
);

-- Add table mappings for UK
SELECT data_correction.add_table_mapping(
    'OLD_STD_003', 'NEW_STD_UK_001', 
    'test', 'std', 'country', 'UK', 'UK'
);
SELECT data_correction.add_table_mapping(
    'OLD_STD_004', 'NEW_STD_UK_002', 
    'test', 'std', 'country', 'UK', 'UK'
);

-- =====================================================
-- STEP 4: CREATE BACKUP BEFORE EXECUTING CORRECTIONS
-- =====================================================

-- Create backup for USA data before corrections
SELECT * FROM data_correction.create_backup('test', 'USA');

-- Create backup for UK data before corrections
SELECT * FROM data_correction.create_backup('test', 'UK');

-- Check backup list
SELECT * FROM data_correction.get_backup_list();

-- =====================================================
-- STEP 5: EXECUTE CORRECTIONS BY COUNTRY
-- =====================================================

-- Execute corrections for USA
SELECT * FROM data_correction.execute_corrections('USA');

-- Execute corrections for UK
SELECT * FROM data_correction.execute_corrections('UK');

-- =====================================================
-- STEP 6: CHECK RESULTS
-- =====================================================

-- Check the updated data
SELECT * FROM test ORDER BY country, std;

-- Check execution summary
SELECT * FROM data_correction.get_summary();

-- =====================================================
-- STEP 7: RESTORE EXAMPLE (if needed)
-- =====================================================

-- Get backup ID from the backup list
-- SELECT backup_id FROM data_correction.get_backup_list() WHERE country = 'USA' LIMIT 1;

-- Uncomment to restore USA data from backup (replace BACKUP_ID with actual backup ID)
-- SELECT * FROM data_correction.restore_from_backup('BACKUP_test_20241201_143022');

-- =====================================================
-- STEP 8: ROLLBACK EXAMPLE (if needed)
-- =====================================================

-- Uncomment to rollback USA changes
-- SELECT * FROM data_correction.rollback_corrections('USA');
-- SELECT * FROM data_correction.get_execution_summary('BATCH_001_20241201');

-- =====================================================
-- STEP 9: MONITORING QUERIES
-- =====================================================

-- Check value mappings by country
SELECT 
    old_value,
    new_value,
    country,
    created_at
FROM data_correction.value_mappings 
ORDER BY country, created_at;

-- Check table mappings by country
SELECT 
    tm.country,
    tm.table_name,
    tm.column_name,
    vm.old_value,
    vm.new_value,
    tm.status
FROM data_correction.table_mappings tm
JOIN data_correction.value_mappings vm ON tm.country = vm.country
ORDER BY tm.country, tm.table_name;

-- Check pending table mappings
SELECT 
    country,
    table_name,
    column_name,
    COUNT(*) as pending_count
FROM data_correction.table_mappings 
WHERE status = 'PENDING'
GROUP BY country, table_name, column_name;

-- Check execution logs
SELECT 
    table_name,
    column_name,
    country,
    records_affected,
    status,
    executed_at
FROM data_correction.execution_logs 
ORDER BY executed_at DESC;

-- Check backup status
SELECT 
    backup_id,
    table_name,
    country,
    records_backed_up,
    backup_status,
    created_at
FROM data_correction.backup_management 
ORDER BY created_at DESC;

DO $$
BEGIN
    RAISE NOTICE 'Example completed successfully';
    RAISE NOTICE 'Value mappings are now country-specific and joined using country column';
    RAISE NOTICE 'Backup functionality is now available for safe data corrections';
END $$; 