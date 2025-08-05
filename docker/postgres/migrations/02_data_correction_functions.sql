-- =====================================================
-- Basic Data Correction Functions
-- =====================================================

-- =====================================================
-- 1. FUNCTION TO EXECUTE VALUE CORRECTIONS BY COUNTRY
-- =====================================================
CREATE OR REPLACE FUNCTION data_correction.execute_corrections(
    p_country VARCHAR(50)
)
RETURNS TABLE(
    table_name VARCHAR(100),
    column_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    country VARCHAR(50),
    records_affected INTEGER,
    status VARCHAR(20),
    error_message TEXT
) AS $$
DECLARE
    v_mapping RECORD;
    v_sql TEXT;
    v_records_affected INTEGER;
BEGIN
    -- Loop through pending table mappings for the country
    FOR v_mapping IN 
        SELECT 
            tm.id,
            tm.table_name,
            tm.column_name,
            tm.where_column,
            tm.where_value,
            tm.country,
            vm.old_value,
            vm.new_value
        FROM data_correction.table_mappings tm
        JOIN data_correction.value_mappings vm ON tm.country = vm.country
        WHERE tm.country = p_country AND tm.status = 'PENDING'
        ORDER BY tm.table_name, tm.column_name
    LOOP
        BEGIN
            -- Build dynamic SQL for the update
            v_sql := format(
                'UPDATE %I SET %I = %L WHERE %I = %L AND %I = %L',
                v_mapping.table_name,
                v_mapping.column_name,
                v_mapping.new_value,
                v_mapping.where_column,
                v_mapping.where_value,
                v_mapping.column_name,
                v_mapping.old_value
            );

            -- Execute the update
            EXECUTE v_sql;
            GET DIAGNOSTICS v_records_affected = ROW_COUNT;

            -- Only log success if records were affected
            IF v_records_affected > 0 THEN
                -- Update table mapping status to completed
                UPDATE data_correction.table_mappings 
                SET status = 'COMPLETED'
                WHERE id = v_mapping.id;

                -- Log the successful execution
                INSERT INTO data_correction.execution_logs (
                    table_mapping_id, table_name, column_name, old_value, new_value, 
                    country, records_affected, status
                ) VALUES (
                    v_mapping.id, v_mapping.table_name, v_mapping.column_name, 
                    v_mapping.old_value, v_mapping.new_value,
                    v_mapping.country, v_records_affected, 'SUCCESS'
                );

                -- Return success result
                RETURN QUERY SELECT 
                    v_mapping.table_name,
                    v_mapping.column_name,
                    v_mapping.old_value,
                    v_mapping.new_value,
                    v_mapping.country,
                    v_records_affected,
                    'SUCCESS'::VARCHAR(20),
                    NULL::TEXT;
            END IF;

        EXCEPTION WHEN OTHERS THEN
            -- Update table mapping status to failed
            UPDATE data_correction.table_mappings 
            SET status = 'FAILED'
            WHERE id = v_mapping.id;

            -- Log the failure
            INSERT INTO data_correction.execution_logs (
                table_mapping_id, table_name, column_name, old_value, new_value, 
                country, records_affected, status, error_message
            ) VALUES (
                v_mapping.id, v_mapping.table_name, v_mapping.column_name, 
                v_mapping.old_value, v_mapping.new_value,
                v_mapping.country, 0, 'FAILED', SQLERRM
            );

            -- Return failure result
            RETURN QUERY SELECT 
                v_mapping.table_name,
                v_mapping.column_name,
                v_mapping.old_value,
                v_mapping.new_value,
                v_mapping.country,
                0,
                'FAILED'::VARCHAR(20),
                SQLERRM::TEXT;
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 2. FUNCTION TO ROLLBACK CORRECTIONS BY COUNTRY
-- =====================================================
CREATE OR REPLACE FUNCTION data_correction.rollback_corrections(
    p_country VARCHAR(50)
)
RETURNS TABLE(
    table_name VARCHAR(100),
    column_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    country VARCHAR(50),
    records_rolled_back INTEGER,
    status VARCHAR(20),
    error_message TEXT
) AS $$
DECLARE
    v_execution_log RECORD;
    v_sql TEXT;
    v_records_rolled_back INTEGER;
BEGIN
    -- Loop through successful executions for rollback
    FOR v_execution_log IN 
        SELECT * FROM data_correction.execution_logs 
        WHERE country = p_country AND status = 'SUCCESS'
        ORDER BY executed_at DESC
    LOOP
        BEGIN
            -- Build dynamic SQL for the rollback (reverse the update)
            v_sql := format(
                'UPDATE %I SET %I = %L WHERE %I = %L',
                v_execution_log.table_name,
                v_execution_log.column_name,
                v_execution_log.old_value,
                v_execution_log.column_name,
                v_execution_log.new_value
            );

            -- Execute the rollback
            EXECUTE v_sql;
            GET DIAGNOSTICS v_records_rolled_back = ROW_COUNT;

            -- Update execution log status
            UPDATE data_correction.execution_logs 
            SET status = 'ROLLED_BACK'
            WHERE id = v_execution_log.id;

            -- Return success result
            RETURN QUERY SELECT 
                v_execution_log.table_name,
                v_execution_log.column_name,
                v_execution_log.old_value,
                v_execution_log.new_value,
                v_execution_log.country,
                v_records_rolled_back,
                'SUCCESS'::VARCHAR(20),
                NULL::TEXT;

        EXCEPTION WHEN OTHERS THEN
            -- Return failure result
            RETURN QUERY SELECT 
                v_execution_log.table_name,
                v_execution_log.column_name,
                v_execution_log.old_value,
                v_execution_log.new_value,
                v_execution_log.country,
                0,
                'FAILED'::VARCHAR(20),
                SQLERRM::TEXT;
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 3. FUNCTION TO GET EXECUTION SUMMARY
-- =====================================================
CREATE OR REPLACE FUNCTION data_correction.get_summary()
RETURNS TABLE(
    country VARCHAR(50),
    total_mappings INTEGER,
    completed_mappings INTEGER,
    failed_mappings INTEGER,
    total_records_affected INTEGER
) AS $$
BEGIN
    RETURN QUERY SELECT 
        tm.country,
        COUNT(*) as total_mappings,
        COUNT(CASE WHEN tm.status = 'COMPLETED' THEN 1 END) as completed_mappings,
        COUNT(CASE WHEN tm.status = 'FAILED' THEN 1 END) as failed_mappings,
        COALESCE(SUM(el.records_affected), 0) as total_records_affected
    FROM data_correction.table_mappings tm
    LEFT JOIN data_correction.execution_logs el ON tm.id = el.table_mapping_id
    GROUP BY tm.country
    ORDER BY tm.country;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 4. HELPER FUNCTION TO ADD VALUE MAPPING
-- =====================================================
CREATE OR REPLACE FUNCTION data_correction.add_value_mapping(
    p_old_value TEXT,
    p_new_value TEXT,
    p_country VARCHAR(50)
)
RETURNS INTEGER AS $$
DECLARE
    v_mapping_id INTEGER;
BEGIN
    -- Insert value mapping (will ignore if already exists due to UNIQUE constraint)
    INSERT INTO data_correction.value_mappings (old_value, new_value, country)
    VALUES (p_old_value, p_new_value, p_country)
    ON CONFLICT (old_value, new_value, country) DO NOTHING
    RETURNING id INTO v_mapping_id;
    
    -- If no new record was inserted, get the existing ID
    IF v_mapping_id IS NULL THEN
        SELECT id INTO v_mapping_id 
        FROM data_correction.value_mappings 
        WHERE old_value = p_old_value AND new_value = p_new_value AND country = p_country;
    END IF;
    
    RETURN v_mapping_id;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 5. HELPER FUNCTION TO ADD TABLE MAPPING
-- =====================================================
CREATE OR REPLACE FUNCTION data_correction.add_table_mapping(
    p_old_value TEXT,
    p_new_value TEXT,
    p_table_name VARCHAR(100),
    p_column_name VARCHAR(100),
    p_where_column VARCHAR(100),
    p_where_value TEXT,
    p_country VARCHAR(50)
)
RETURNS INTEGER AS $$
DECLARE
    v_table_mapping_id INTEGER;
BEGIN
    -- Add value mapping first
    PERFORM data_correction.add_value_mapping(p_old_value, p_new_value, p_country);
    
    -- Create table mapping
    INSERT INTO data_correction.table_mappings (
        table_name, column_name, 
        where_column, where_value, country
    ) VALUES (
        p_table_name, p_column_name, 
        p_where_column, p_where_value, p_country
    ) RETURNING id INTO v_table_mapping_id;
    
    RETURN v_table_mapping_id;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 6. FUNCTION TO CREATE BACKUP BEFORE UPDATES
-- =====================================================
CREATE OR REPLACE FUNCTION data_correction.create_backup(
    p_table_name VARCHAR(100),
    p_country VARCHAR(50) DEFAULT NULL
)
RETURNS TABLE(
    backup_id VARCHAR(50),
    table_name VARCHAR(100),
    backup_table_name VARCHAR(100),
    records_backed_up INTEGER,
    status VARCHAR(20),
    error_message TEXT
) AS $$
DECLARE
    v_backup_id VARCHAR(50);
    v_backup_table_name VARCHAR(100);
    v_sql TEXT;
    v_records_backed_up INTEGER;
    v_backup_management_id INTEGER;
BEGIN
    -- Generate backup ID
    v_backup_id := 'BACKUP_' || p_table_name || '_' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD_HH24MISS');
    v_backup_table_name := 'backup_' || p_table_name || '_' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD_HH24MISS');
    
    -- Create backup management record
    INSERT INTO data_correction.backup_management (
        backup_id, table_name, backup_table_name, backup_type, country, backup_status
    ) VALUES (
        v_backup_id, p_table_name, v_backup_table_name, 'BEFORE_UPDATE', p_country, 'IN_PROGRESS'
    ) RETURNING id INTO v_backup_management_id;
    
    BEGIN
        -- Create backup table with same structure
        v_sql := format('CREATE TABLE data_correction.%I AS SELECT * FROM %I WHERE 1=0', 
                       v_backup_table_name, p_table_name);
        EXECUTE v_sql;
        
        -- Copy data to backup table
        IF p_country IS NULL THEN
            -- Full table backup
            v_sql := format('INSERT INTO data_correction.%I SELECT * FROM %I', 
                           v_backup_table_name, p_table_name);
        ELSE
            -- Country-specific backup
            v_sql := format('INSERT INTO data_correction.%I SELECT * FROM %I WHERE country = %L', 
                           v_backup_table_name, p_table_name, p_country);
        END IF;
        
        EXECUTE v_sql;
        GET DIAGNOSTICS v_records_backed_up = ROW_COUNT;
        
        -- Update backup management record
        UPDATE data_correction.backup_management 
        SET backup_status = 'COMPLETED',
            records_backed_up = v_records_backed_up,
            completed_at = CURRENT_TIMESTAMP
        WHERE id = v_backup_management_id;
        
        -- Return success result
        RETURN QUERY SELECT 
            v_backup_id,
            p_table_name,
            v_backup_table_name,
            v_records_backed_up,
            'COMPLETED'::VARCHAR(20),
            NULL::TEXT;
            
    EXCEPTION WHEN OTHERS THEN
        -- Update backup management record with failure
        UPDATE data_correction.backup_management 
        SET backup_status = 'FAILED',
            completed_at = CURRENT_TIMESTAMP
        WHERE id = v_backup_management_id;
        
        -- Return failure result
        RETURN QUERY SELECT 
            v_backup_id,
            p_table_name,
            v_backup_table_name,
            0,
            'FAILED'::VARCHAR(20),
            SQLERRM::TEXT;
    END;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 7. FUNCTION TO RESTORE FROM BACKUP
-- =====================================================
CREATE OR REPLACE FUNCTION data_correction.restore_from_backup(
    p_backup_id VARCHAR(50)
)
RETURNS TABLE(
    backup_id VARCHAR(50),
    table_name VARCHAR(100),
    backup_table_name VARCHAR(100),
    records_restored INTEGER,
    status VARCHAR(20),
    error_message TEXT
) AS $$
DECLARE
    v_backup_record RECORD;
    v_sql TEXT;
    v_records_restored INTEGER;
BEGIN
    -- Get backup information
    SELECT * INTO v_backup_record 
    FROM data_correction.backup_management 
    WHERE backup_id = p_backup_id;
    
    IF v_backup_record IS NULL THEN
        RAISE EXCEPTION 'Backup ID % not found', p_backup_id;
    END IF;
    
    IF v_backup_record.backup_status != 'COMPLETED' THEN
        RAISE EXCEPTION 'Backup % is not in completed status', p_backup_id;
    END IF;
    
    BEGIN
        -- Check if backup table exists
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.tables 
            WHERE table_schema = 'data_correction' 
            AND table_name = v_backup_record.backup_table_name
        ) THEN
            RAISE EXCEPTION 'Backup table % does not exist', v_backup_record.backup_table_name;
        END IF;
        
        -- Restore data from backup
        v_sql := format('UPDATE %I SET %I = backup.%I FROM data_correction.%I backup WHERE %I.id = backup.id', 
                       v_backup_record.table_name, 
                       (SELECT column_name FROM data_correction.table_mappings WHERE table_name = v_backup_record.table_name LIMIT 1),
                       (SELECT column_name FROM data_correction.table_mappings WHERE table_name = v_backup_record.table_name LIMIT 1),
                       v_backup_record.backup_table_name,
                       v_backup_record.table_name);
        
        EXECUTE v_sql;
        GET DIAGNOSTICS v_records_restored = ROW_COUNT;
        
        -- Return success result
        RETURN QUERY SELECT 
            v_backup_record.backup_id,
            v_backup_record.table_name,
            v_backup_record.backup_table_name,
            v_records_restored,
            'RESTORED'::VARCHAR(20),
            NULL::TEXT;
            
    EXCEPTION WHEN OTHERS THEN
        -- Return failure result
        RETURN QUERY SELECT 
            v_backup_record.backup_id,
            v_backup_record.table_name,
            v_backup_record.backup_table_name,
            0,
            'FAILED'::VARCHAR(20),
            SQLERRM::TEXT;
    END;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 8. FUNCTION TO GET BACKUP LIST
-- =====================================================
CREATE OR REPLACE FUNCTION data_correction.get_backup_list()
RETURNS TABLE(
    backup_id VARCHAR(50),
    table_name VARCHAR(100),
    backup_table_name VARCHAR(100),
    backup_type VARCHAR(20),
    country VARCHAR(50),
    records_backed_up INTEGER,
    backup_status VARCHAR(20),
    created_at TIMESTAMP,
    completed_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY SELECT 
        bm.backup_id,
        bm.table_name,
        bm.backup_table_name,
        bm.backup_type,
        bm.country,
        bm.records_backed_up,
        bm.backup_status,
        bm.created_at,
        bm.completed_at
    FROM data_correction.backup_management bm
    ORDER BY bm.created_at DESC;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
    RAISE NOTICE 'Basic data correction functions created successfully';
    RAISE NOTICE 'Functions: execute_corrections, rollback_corrections, get_summary, add_value_mapping, add_table_mapping';
    RAISE NOTICE 'Backup functions: create_backup, restore_from_backup, get_backup_list';
    RAISE NOTICE 'Execution logic: Only logs SUCCESS when records_affected > 0, nothing when zero';
    RAISE NOTICE 'Value mappings now include country - join using country column';
END $$; 