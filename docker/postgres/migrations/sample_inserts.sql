-- =====================================================
-- Sample Insert Scripts for Data Correction System
-- =====================================================

-- =====================================================
-- SAMPLE VALUE MAPPINGS (Country-specific)
-- =====================================================

-- Insert sample value mappings for USA
INSERT INTO data_correction.value_mappings (old_value, new_value, country) VALUES
('OLD_STD_USA_001', 'NEW_STD_USA_001', 'USA'),
('OLD_STD_USA_002', 'NEW_STD_USA_002', 'USA');

-- Insert sample value mappings for UK
INSERT INTO data_correction.value_mappings (old_value, new_value, country) VALUES
('OLD_STD_UK_001', 'NEW_STD_UK_001', 'UK'),
('OLD_STD_UK_002', 'NEW_STD_UK_002', 'UK');

-- =====================================================
-- SAMPLE TABLE MAPPINGS (Links to specific tables using country join)
-- =====================================================

-- Insert sample table mappings for USA
INSERT INTO data_correction.table_mappings (table_name, column_name, where_column, where_value, country) VALUES
('test_table', 'std_column', 'country', 'USA', 'USA'),
('sample_table', 'code_column', 'region', 'NORTH_AMERICA', 'USA');

-- Insert sample table mappings for UK
INSERT INTO data_correction.table_mappings (table_name, column_name, where_column, where_value, country) VALUES
('test_table', 'std_column', 'country', 'UK', 'UK'),
('sample_table', 'code_column', 'region', 'EUROPE', 'UK');

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check value mappings
SELECT 'Value Mappings' as table_name, old_value, new_value, country, created_at 
FROM data_correction.value_mappings 
ORDER BY country, old_value;

-- Check table mappings
SELECT 'Table Mappings' as table_name, table_name as target_table, column_name, where_column, where_value, country, status
FROM data_correction.table_mappings 
ORDER BY country, table_name;

-- Check combined view (how they would be joined)
SELECT 
    tm.table_name,
    tm.column_name,
    tm.where_column,
    tm.where_value,
    tm.country,
    vm.old_value,
    vm.new_value,
    tm.status
FROM data_correction.table_mappings tm
JOIN data_correction.value_mappings vm ON tm.country = vm.country
ORDER BY tm.country, tm.table_name;

DO $$
BEGIN
    RAISE NOTICE 'Sample data inserted successfully';
    RAISE NOTICE '2 value mappings for USA, 2 for UK';
    RAISE NOTICE '2 table mappings for USA, 2 for UK';
    RAISE NOTICE 'Tables are joined using country column';
END $$; 