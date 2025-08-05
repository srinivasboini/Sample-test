-- =====================================================
-- Create Backup Tables Script
-- Creates backup tables for all distinct table names in table_mappings
-- =====================================================

DO $$
DECLARE
    table_record RECORD;
    backup_table_name VARCHAR(100);
    backup_id VARCHAR(50);
    backup_management_id INTEGER;
BEGIN
    -- Loop through all distinct table names in table_mappings
    FOR table_record IN 
        SELECT DISTINCT table_name, country 
        FROM data_correction.table_mappings 
        WHERE status = 'PENDING'
    LOOP
        -- Generate backup table name
        backup_table_name := 'backup_' || table_record.table_name || '_' || 
                           REPLACE(REPLACE(REPLACE(REPLACE(NOW()::TEXT, ' ', '_'), ':', ''), '-', ''), '.', '');
        
        -- Generate backup ID
        backup_id := 'BACKUP_' || table_record.table_name || '_' || 
                    EXTRACT(EPOCH FROM NOW())::BIGINT;
        
        -- Insert backup management record
        INSERT INTO data_correction.backup_management 
        (backup_id, table_name, backup_table_name, backup_type, country, backup_status, created_at)
        VALUES 
        (backup_id, table_record.table_name, backup_table_name, 'BEFORE_UPDATE', table_record.country, 'IN_PROGRESS', NOW())
        RETURNING id INTO backup_management_id;
        
        -- Create backup table with same structure as original table
        EXECUTE format('
            CREATE TABLE IF NOT EXISTS %I AS 
            SELECT * FROM %I WHERE 1=0
        ', backup_table_name, table_record.table_name);
        
        -- Copy data to backup table
        EXECUTE format('
            INSERT INTO %I 
            SELECT * FROM %I
        ', backup_table_name, table_record.table_name);
        
        -- Get count of backed up records
        EXECUTE format('
            UPDATE data_correction.backup_management 
            SET records_backed_up = (SELECT COUNT(*) FROM %I),
                backup_status = ''COMPLETED'',
                completed_at = NOW()
            WHERE id = %s
        ', backup_table_name, backup_management_id);
        
        RAISE NOTICE 'Created backup table: % for table: % (Country: %)', 
                    backup_table_name, table_record.table_name, table_record.country;
    END LOOP;
    
    RAISE NOTICE 'Backup tables created successfully for all pending table mappings';
END $$;

-- Show summary of created backups
SELECT 
    backup_id,
    table_name,
    backup_table_name,
    country,
    records_backed_up,
    backup_status,
    created_at
FROM data_correction.backup_management 
WHERE backup_status = 'COMPLETED' 
ORDER BY created_at DESC; 