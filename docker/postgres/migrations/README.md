# Basic Data Correction System for PostgreSQL

## Overview

A simple SQL solution for correcting wrong values across multiple tables in PostgreSQL. Value mappings are stored per country and joined using the country column, providing a clean and efficient approach to data corrections. Includes automatic backup and restore functionality for safe data corrections.

## Features

- ✅ **Country-specific Value Mappings**: Old→new value pairs stored per country
- ✅ **Country-based Execution**: Run corrections by specific countries
- ✅ **Automatic Backup**: Create backups before executing corrections
- ✅ **Auto Restore**: Restore data from backup tables if needed
- ✅ **Simple Monitoring**: Track what was updated and how many records
- ✅ **Basic Rollback**: Rollback corrections if needed
- ✅ **Error Handling**: Log errors and failed operations

## Quick Start

### 1. Setup Infrastructure
```sql
\i docker/postgres/migrations/01_create_value_mapping_tables.sql
```

### 2. Install Functions
```sql
\i docker/postgres/migrations/02_data_correction_functions.sql
```

### 3. Test with Example
```sql
\i docker/postgres/migrations/03_example_usage.sql
```

## How to Use

### Step 1: Add Value Mappings (Country-specific)
```sql
-- Add value mappings for specific country
SELECT data_correction.add_value_mapping('OLD_VALUE', 'NEW_VALUE', 'COUNTRY');
```

### Step 2: Add Table Mappings (Links values to specific tables)
```sql
-- Link value mapping to specific table/column/country
SELECT data_correction.add_table_mapping(
    'OLD_VALUE', 'NEW_VALUE', 
    'table_name', 'column_name', 'where_column', 'where_value', 'country'
);
```

### Step 3: Create Backup (Recommended)
```sql
-- Create backup before executing corrections
SELECT * FROM data_correction.create_backup('table_name', 'country');
```

### Step 4: Execute Corrections
```sql
-- Execute for specific country
SELECT * FROM data_correction.execute_corrections('USA');
```

### Step 5: Check Results
```sql
-- Get summary
SELECT * FROM data_correction.get_summary();

-- Check execution logs
SELECT * FROM data_correction.execution_logs ORDER BY executed_at DESC;
```

### Step 6: Restore from Backup (if needed)
```sql
-- List available backups
SELECT * FROM data_correction.get_backup_list();

-- Restore from specific backup
SELECT * FROM data_correction.restore_from_backup('BACKUP_ID');
```

### Step 7: Rollback if Needed
```sql
-- Rollback specific country
SELECT * FROM data_correction.rollback_corrections('USA');
```

## Tables

1. **`value_mappings`** - Country-specific old→new value pairs
2. **`table_mappings`** - Links to specific tables/columns using country join
3. **`execution_logs`** - Tracks what was updated and how many records
4. **`backup_management`** - Tracks backup operations and restore points

## Functions

1. **`add_value_mapping(old_value, new_value, country)`** - Add country-specific value mapping
2. **`add_table_mapping(...)`** - Link value mapping to specific table
3. **`create_backup(table_name, country)`** - Create backup before corrections
4. **`restore_from_backup(backup_id)`** - Restore data from backup
5. **`get_backup_list()`** - List all available backups
6. **`execute_corrections(country)`** - Execute corrections for a country
7. **`rollback_corrections(country)`** - Rollback corrections for a country
8. **`get_summary()`** - Get summary of all operations

## Example

```sql
-- 1. Add country-specific value mapping
SELECT data_correction.add_value_mapping('OLD_STD_001', 'NEW_STD_001', 'USA');

-- 2. Link to specific table
SELECT data_correction.add_table_mapping(
    'OLD_STD_001', 'NEW_STD_001', 
    'test', 'std', 'country', 'USA', 'USA'
);

-- 3. Create backup
SELECT * FROM data_correction.create_backup('test', 'USA');

-- 4. Execute corrections
SELECT * FROM data_correction.execute_corrections('USA');

-- 5. Check results
SELECT * FROM data_correction.get_summary();
```

## Backup and Restore

### Creating Backups
```sql
-- Full table backup
SELECT * FROM data_correction.create_backup('table_name');

-- Country-specific backup
SELECT * FROM data_correction.create_backup('table_name', 'USA');
```

### Restoring from Backups
```sql
-- List available backups
SELECT * FROM data_correction.get_backup_list();

-- Restore from backup
SELECT * FROM data_correction.restore_from_backup('BACKUP_test_20241201_143022');
```

## Benefits of New Structure

- **Country-specific**: Value mappings are organized by country for better management
- **Simple Joins**: Tables are joined using country column instead of foreign keys
- **Efficient**: Direct country-based lookups without complex joins
- **Flexible**: Same old→new value can be applied to different tables within a country
- **Maintainable**: Update value mapping per country, affects all related table mappings
- **Safe**: Automatic backup and restore functionality
- **Auditable**: Complete tracking of all operations

## Monitoring

```sql
-- Check value mappings by country
SELECT old_value, new_value, country, created_at 
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
SELECT country, COUNT(*) as pending 
FROM data_correction.table_mappings 
WHERE status = 'PENDING' 
GROUP BY country;

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
```

## Best Practices

1. **Always Create Backups**: Create backups before executing corrections
2. **Test on Small Data**: Test corrections on a subset before running on large datasets
3. **Execute by Country**: Run corrections by country to minimize risk
4. **Monitor Progress**: Check execution logs and backup status regularly
5. **Keep Backups**: Don't delete backup tables immediately after successful execution

## File Structure

```
docker/postgres/migrations/
├── 01_create_value_mapping_tables.sql    # Setup tables
├── 02_data_correction_functions.sql      # Core functions
├── 03_example_usage.sql                  # Example usage
└── README.md                             # This documentation
```

---

**Note**: Always test in a non-production environment first. 