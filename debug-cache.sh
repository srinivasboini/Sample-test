#!/bin/bash

# Cache Debugging Script for Master Configuration
# This script helps test and validate the cache behavior

echo "=== Master Configuration Cache Debug Script ==="
echo ""

# Check if application is running
APP_URL="http://localhost:8080"

echo "1. Testing Master Configuration Validation..."
echo "   Validating TASK/NORMAL combination:"
curl -s "$APP_URL/api/master-configurations/validate?category=TASK&typeCode=NORMAL"
echo ""
echo ""

echo "2. Fetching all active master configurations..."
curl -s "$APP_URL/api/master-configurations" | jq '.'
echo ""
echo ""

echo "3. Finding specific TASK/NORMAL configuration..."
curl -s "$APP_URL/api/master-configurations/find?category=TASK&typeCode=NORMAL" | jq '.'
echo ""
echo ""

echo "4. Creating a test configuration (if needed)..."
curl -s -X POST "$APP_URL/api/master-configurations" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "TASK",
    "typeCode": "NORMAL", 
    "description": "Normal task category",
    "active": true
  }' | jq '.'
echo ""
echo ""

echo "5. Re-testing validation after creation..."
curl -s "$APP_URL/api/master-configurations/validate?category=TASK&typeCode=NORMAL"
echo ""
echo ""

echo "=== Debug Script Complete ==="
echo ""
echo "Expected behavior:"
echo "- Step 1 should return 'Valid category-type combination' if cache is working"
echo "- Step 2 should show TASK/NORMAL in the list if configuration exists"
echo "- Step 3 should return the specific configuration details"
echo "- Step 4 will create the config if it doesn't exist (may fail if duplicate)"
echo "- Step 5 should return 'Valid category-type combination' after creation" 