-- =====================================================
-- Rollback Script
-- Deletes records from existing tables and restores from backup tables
-- =====================================================

-- Function to perform rollback for a specific backup
CREATE OR REPLACE FUNCTION data_correction.rollback_from_backup(backup_id_param VARCHAR(50))
RETURNS TEXT AS $$
DECLARE
    backup_record RECORD;
    rollback_message TEXT;
BEGIN
    -- Get backup information
    SELECT * INTO backup_record 
    FROM data_correction.backup_management 
    WHERE backup_id = backup_id_param;
    
    IF NOT FOUND THEN
        RETURN 'Backup not found: ' || backup_id_param;
    END IF;
    
    -- Delete all records from the original table
    EXECUTE format('DELETE FROM %I', backup_record.table_name);
    
    -- Restore data from backup table
    EXECUTE format('
        INSERT INTO %I 
        SELECT * FROM %I
    ', backup_record.table_name, backup_record.backup_table_name);
    
    -- Update backup management status
    UPDATE data_correction.backup_management 
    SET backup_status = 'ROLLED_BACK'
    WHERE backup_id = backup_id_param;
    
    rollback_message := 'Rollback completed for table: ' || backup_record.table_name || 
                       ' from backup: ' || backup_record.backup_table_name;
    
    RAISE NOTICE '%', rollback_message;
    RETURN rollback_message;
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 'Rollback failed: ' || SQLERRM;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- Rollback All Pending Changes
-- =====================================================

DO $$
DECLARE
    backup_record RECORD;
    rollback_result TEXT;
BEGIN
    -- Loop through all completed backups and perform rollback
    FOR backup_record IN 
        SELECT backup_id, table_name, backup_table_name, country
        FROM data_correction.backup_management 
        WHERE backup_status = 'COMPLETED'
        ORDER BY created_at DESC
    LOOP
        -- Perform rollback for this backup
        SELECT data_correction.rollback_from_backup(backup_record.backup_id) INTO rollback_result;
        
        RAISE NOTICE 'Rollback result for %: %', backup_record.backup_id, rollback_result;
    END LOOP;
    
    RAISE NOTICE 'All rollbacks completed';
END $$;

-- =====================================================
-- Cleanup Backup Tables (Optional)
-- Uncomment the section below if you want to drop backup tables after rollback
-- =====================================================

/*
DO $$
DECLARE
    backup_record RECORD;
BEGIN
    -- Drop all backup tables
    FOR backup_record IN 
        SELECT backup_table_name
        FROM data_correction.backup_management 
        WHERE backup_status = 'ROLLED_BACK'
    LOOP
        EXECUTE format('DROP TABLE IF EXISTS %I', backup_record.backup_table_name);
        RAISE NOTICE 'Dropped backup table: %', backup_record.backup_table_name;
    END LOOP;
    
    RAISE NOTICE 'All backup tables cleaned up';
END $$;
*/

-- =====================================================
-- Show Rollback Summary
-- =====================================================

SELECT 
    backup_id,
    table_name,
    backup_table_name,
    country,
    records_backed_up,
    backup_status,
    created_at,
    completed_at
FROM data_correction.backup_management 
ORDER BY created_at DESC; 