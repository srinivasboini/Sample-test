#!/bin/bash

# Multi-Repository Branch Creator
# Usage: ./create_branches.sh <branch_name> [base_branch]

set -e  # Exit on any error

# Configuration
BRANCH_NAME="$1"
BASE_BRANCH="${2:-main}"  # Default to 'main' if not specified

# List of repository paths (modify these paths to match your repositories)
REPOSITORIES=(
    "/path/to/repo1"
    "/path/to/repo2" 
    "/path/to/repo3"
    # Add more repository paths here
)

# Alternative: Read repositories from a file
# You can create a file called 'repos.txt' with one repository path per line
# and uncomment the following lines:
# REPOSITORIES=()
# while IFS= read -r line; do
#     [[ -n "$line" && ! "$line" =~ ^[[:space:]]*# ]] && REPOSITORIES+=("$line")
# done < repos.txt

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if branch exists
branch_exists() {
    local repo_path="$1"
    local branch="$2"
    
    cd "$repo_path"
    git branch -a | grep -q "^[[:space:]]*${branch}$\|^[[:space:]]*remotes/origin/${branch}$"
}

# Function to create branch in a single repository
create_branch_in_repo() {
    local repo_path="$1"
    local repo_name=$(basename "$repo_path")
    
    print_status "Processing repository: $repo_name"
    
    # Check if directory exists and is a git repository
    if [[ ! -d "$repo_path" ]]; then
        print_error "Directory does not exist: $repo_path"
        return 1
    fi
    
    cd "$repo_path"
    
    if [[ ! -d ".git" ]]; then
        print_error "Not a git repository: $repo_path"
        return 1
    fi
    
    # Check if branch already exists
    if branch_exists "$repo_path" "$BRANCH_NAME"; then
        print_warning "Branch '$BRANCH_NAME' already exists in $repo_name"
        return 0
    fi
    
    # Fetch latest changes
    print_status "Fetching latest changes in $repo_name..."
    git fetch origin
    
    # Check if base branch exists
    if ! branch_exists "$repo_path" "$BASE_BRANCH"; then
        print_error "Base branch '$BASE_BRANCH' does not exist in $repo_name"
        return 1
    fi
    
    # Switch to base branch and pull latest
    print_status "Switching to $BASE_BRANCH and pulling latest changes..."
    git checkout "$BASE_BRANCH"
    git pull origin "$BASE_BRANCH"
    
    # Create new branch
    print_status "Creating branch '$BRANCH_NAME' from '$BASE_BRANCH'..."
    git checkout -b "$BRANCH_NAME"
    
    # Push new branch to remote
    print_status "Pushing branch '$BRANCH_NAME' to remote..."
    git push -u origin "$BRANCH_NAME"
    
    print_success "Successfully created branch '$BRANCH_NAME' in $repo_name"
    echo ""
}

# Main function
main() {
    # Check if branch name is provided
    if [[ -z "$BRANCH_NAME" ]]; then
        echo "Usage: $0 <branch_name> [base_branch]"
        echo ""
        echo "Arguments:"
        echo "  branch_name  : Name of the new branch to create"
        echo "  base_branch  : Base branch to create from (default: main)"
        echo ""
        echo "Example: $0 feature/new-feature develop"
        exit 1
    fi
    
    print_status "Creating branch '$BRANCH_NAME' from '$BASE_BRANCH' in ${#REPOSITORIES[@]} repositories..."
    echo ""
    
    local success_count=0
    local total_count=${#REPOSITORIES[@]}
    
    # Process each repository
    for repo in "${REPOSITORIES[@]}"; do
        if create_branch_in_repo "$repo"; then
            ((success_count++))
        fi
    done
    
    # Summary
    echo "=================================="
    print_status "Summary:"
    print_success "Successfully created branches: $success_count/$total_count"
    
    if [[ $success_count -eq $total_count ]]; then
        print_success "All branches created successfully!"
    elif [[ $success_count -gt 0 ]]; then
        print_warning "Some branches were created successfully, but there were errors with others."
    else
        print_error "No branches were created successfully."
        exit 1
    fi
}

# Run main function
main "$@"
