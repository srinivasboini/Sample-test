#!/bin/bash

# Database Health Monitor Test Script
# This script demonstrates the database health monitoring system

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_URL="http://localhost:18080"
HEALTH_ENDPOINT="$APP_URL/api/health"
LOG_FILE="health-monitor-test.log"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to check if application is running
check_app_running() {
    if curl -s "$APP_URL/api/health/ping" > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to get health status
get_health_status() {
    curl -s "$HEALTH_ENDPOINT/status" | jq .
}

# Function to get database health
get_database_health() {
    curl -s "$HEALTH_ENDPOINT/database" | jq .
}

# Function to get consumer status
get_consumer_status() {
    curl -s "$HEALTH_ENDPOINT/consumers" | jq .
}

# Function to trigger manual health check
trigger_health_check() {
    curl -s -X POST "$HEALTH_ENDPOINT/check" | jq .
}

# Function to wait for condition
wait_for_condition() {
    local condition=$1
    local timeout=$2
    local interval=$3
    local elapsed=0
    
    print_status "Waiting for condition: $condition (timeout: ${timeout}s)"
    
    while [ $elapsed -lt $timeout ]; do
        if eval "$condition"; then
            return 0
        fi
        sleep $interval
        elapsed=$((elapsed + interval))
    done
    
    return 1
}

# Function to check if database is healthy
is_database_healthy() {
    local health=$(curl -s "$HEALTH_ENDPOINT/database" | jq -r '.healthy')
    [ "$health" = "true" ]
}

# Function to check if consumers are paused
are_consumers_paused() {
    local paused=$(curl -s "$HEALTH_ENDPOINT/consumers" | jq -r '.paused')
    [ "$paused" = "true" ]
}

# Function to check if consumers are running
are_consumers_running() {
    local paused=$(curl -s "$HEALTH_ENDPOINT/consumers" | jq -r '.paused')
    [ "$paused" = "false" ]
}

# Main test function
run_tests() {
    print_status "Starting Database Health Monitor Tests"
    print_status "========================================"
    
    # Check if application is running
    print_status "Checking if application is running..."
    if ! check_app_running; then
        print_error "Application is not running. Please start the application first."
        print_status "Run: mvn spring-boot:run"
        exit 1
    fi
    print_success "Application is running"
    
    # Initial health check
    print_status "Performing initial health check..."
    initial_status=$(get_health_status)
    echo "$initial_status" | jq .
    
    # Check initial state
    if is_database_healthy; then
        print_success "Database is healthy"
    else
        print_warning "Database is not healthy"
    fi
    
    if are_consumers_running; then
        print_success "Consumers are running"
    else
        print_warning "Consumers are paused"
    fi
    
    print_status "Initial state established"
    print_status "=========================="
    
    # Test 1: Manual health check trigger
    print_status "Test 1: Manual Health Check Trigger"
    print_status "-----------------------------------"
    manual_check=$(trigger_health_check)
    echo "$manual_check" | jq .
    
    if echo "$manual_check" | jq -r '.success' | grep -q "true"; then
        print_success "Manual health check successful"
    else
        print_error "Manual health check failed"
    fi
    
    # Test 2: Database health monitoring
    print_status "Test 2: Database Health Monitoring"
    print_status "-----------------------------------"
    for i in {1..5}; do
        print_status "Health check iteration $i/5"
        db_health=$(get_database_health)
        echo "$db_health" | jq .
        sleep 2
    done
    
    # Test 3: Consumer status monitoring
    print_status "Test 3: Consumer Status Monitoring"
    print_status "-----------------------------------"
    for i in {1..5}; do
        print_status "Consumer check iteration $i/5"
        consumer_status=$(get_consumer_status)
        echo "$consumer_status" | jq .
        sleep 2
    done
    
    # Test 4: Comprehensive status
    print_status "Test 4: Comprehensive Status"
    print_status "----------------------------"
    comprehensive_status=$(get_health_status)
    echo "$comprehensive_status" | jq .
    
    # Test 5: Metrics endpoint
    print_status "Test 5: Metrics Endpoint"
    print_status "------------------------"
    metrics=$(curl -s "$HEALTH_ENDPOINT/metrics" | jq .)
    echo "$metrics" | jq .
    
    print_status "All tests completed successfully!"
    print_status "================================"
}

# Function to simulate database downtime test
simulate_database_downtime() {
    print_status "Database Downtime Simulation Test"
    print_status "================================="
    print_warning "This test will simulate database downtime"
    print_warning "Make sure you have a way to restart your database"
    
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_status "Skipping database downtime simulation"
        return
    fi
    
    print_status "Starting database downtime simulation..."
    
    # Get initial state
    print_status "Initial state:"
    initial_db_health=$(get_database_health)
    initial_consumer_status=$(get_consumer_status)
    echo "Database: $initial_db_health" | jq .
    echo "Consumers: $initial_consumer_status" | jq .
    
    print_warning "Please stop your database now (e.g., docker stop postgres)"
    print_warning "Waiting for database to go down..."
    
    # Wait for database to go down
    if wait_for_condition "! is_database_healthy" 60 5; then
        print_success "Database is now down"
        
        print_status "Waiting for consumers to pause..."
        if wait_for_condition "are_consumers_paused" 30 5; then
            print_success "Consumers have been paused"
        else
            print_error "Consumers did not pause within expected time"
        fi
        
        print_warning "Please restart your database now"
        print_warning "Waiting for database to recover..."
        
        # Wait for database to recover
        if wait_for_condition "is_database_healthy" 120 5; then
            print_success "Database has recovered"
            
            print_status "Waiting for consumers to resume..."
            if wait_for_condition "are_consumers_running" 30 5; then
                print_success "Consumers have resumed"
            else
                print_error "Consumers did not resume within expected time"
            fi
        else
            print_error "Database did not recover within expected time"
        fi
    else
        print_error "Database did not go down within expected time"
    fi
    
    # Final state
    print_status "Final state:"
    final_db_health=$(get_database_health)
    final_consumer_status=$(get_consumer_status)
    echo "Database: $final_db_health" | jq .
    echo "Consumers: $final_consumer_status" | jq .
}

# Function to show help
show_help() {
    echo "Database Health Monitor Test Script"
    echo "==================================="
    echo ""
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  --basic-tests     Run basic health monitoring tests"
    echo "  --downtime-test   Run database downtime simulation test"
    echo "  --all-tests       Run all tests (default)"
    echo "  --help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                    # Run all tests"
    echo "  $0 --basic-tests      # Run only basic tests"
    echo "  $0 --downtime-test    # Run only downtime simulation"
    echo ""
}

# Main script logic
main() {
    case "${1:---all-tests}" in
        --basic-tests)
            run_tests
            ;;
        --downtime-test)
            simulate_database_downtime
            ;;
        --all-tests)
            run_tests
            echo
            simulate_database_downtime
            ;;
        --help)
            show_help
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
}

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    print_error "jq is required but not installed. Please install jq first."
    print_status "Install on macOS: brew install jq"
    print_status "Install on Ubuntu: sudo apt-get install jq"
    exit 1
fi

# Run main function
main "$@" 