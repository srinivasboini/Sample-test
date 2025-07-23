#!/usr/bin/env bash

# Multi-Repository Branch Creator with Auto-Clone
# Usage: ./create_branches.sh <branch_name> [base_branch]

set -e  # Exit on any error

# Configuration
BRANCH_NAME="$1"
BASE_BRANCH="${2:-main}"  # Default to 'main' if not specified
WORKSPACE_DIR="${WORKSPACE_DIR:-./workspace}"  # Directory to clone repos into

# List of repository SSH URLs (modify these URLs to match your repositories)
REPOSITORIES=(
    "git@github.com:username/repo1.git"
    "git@github.com:username/repo2.git"
    "git@github.com:username/repo3.git"
    # Add more repository SSH URLs here
)

# Alternative: Read repositories from a file
# You can create a file called 'repos.txt' with one SSH URL per line
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

# Function to extract repository name from SSH URL
get_repo_name() {
    local url="$1"
    # Extract repo name from SSH URL (e.g., git@github.com:user/repo.git -> repo)
    basename "$url" .git
}

# Function to get local repository path
get_repo_path() {
    local url="$1"
    local repo_name=$(get_repo_name "$url")
    echo "${WORKSPACE_DIR}/${repo_name}"
}

# Function to check if branch exists
branch_exists() {
    local repo_path="$1"
    local branch="$2"
    
    cd "$repo_path"
    git branch -a | grep -q "^[[:space:]]*${branch}$\|^[[:space:]]*remotes/origin/${branch}$"
}

# Function to clone repository if it doesn't exist
clone_repo_if_needed() {
    local repo_url="$1"
    local repo_path="$2"
    local repo_name=$(get_repo_name "$repo_url")
    
    if [[ ! -d "$repo_path" ]]; then
        print_status "Repository not found locally. Cloning $repo_name..."
        mkdir -p "$WORKSPACE_DIR"
        git clone "$repo_url" "$repo_path"
        print_success "Successfully cloned $repo_name"
    else
        print_status "Repository $repo_name already exists locally"
    fi
}

# Function to create branch in a single repository
create_branch_in_repo() {
    local repo_url="$1"
    local repo_path=$(get_repo_path "$repo_url")
    local repo_name=$(get_repo_name "$repo_url")
    
    print_status "Processing repository: $repo_name"
    echo "Repository URL: $repo_url"
    echo "Local path: $repo_path"
    echo ""
    
    # Clone repository if it doesn't exist
    if ! clone_repo_if_needed "$repo_url" "$repo_path"; then
        print_error "Failed to clone repository: $repo_name"
        return 1
    fi
    
    cd "$repo_path"
    
    # Verify it's a git repository
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

# Function to validate SSH URL format
validate_ssh_url() {
    local url="$1"
    if [[ ! "$url" =~ ^git@.*:.*\.git$ ]]; then
        print_error "Invalid SSH URL format: $url"
        print_error "Expected format: git@hostname:username/repository.git"
        return 1
    fi
    return 0
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
        echo "Environment Variables:"
        echo "  WORKSPACE_DIR : Directory to clone repositories into (default: ./workspace)"
        echo ""
        echo "Examples:"
        echo "  $0 feature/new-feature"
        echo "  $0 feature/new-feature develop"
        echo "  WORKSPACE_DIR=/tmp/repos $0 hotfix/urgent-fix"
        exit 1
    fi
    
    # Validate all repository URLs
    print_status "Validating repository URLs..."
    for repo_url in "${REPOSITORIES[@]}"; do
        if ! validate_ssh_url "$repo_url"; then
            exit 1
        fi
    done
    
    print_status "Creating branch '$BRANCH_NAME' from '$BASE_BRANCH' in ${#REPOSITORIES[@]} repositories..."
    print_status "Workspace directory: $WORKSPACE_DIR"
    echo ""
    
    # Create workspace directory if it doesn't exist
    mkdir -p "$WORKSPACE_DIR"
    
    local success_count=0
    local total_count=${#REPOSITORIES[@]}
    local failed_repos=()
    
    # Process each repository
    for repo_url in "${REPOSITORIES[@]}"; do
        if create_branch_in_repo "$repo_url"; then
            ((success_count++))
        else
            failed_repos+=("$(get_repo_name "$repo_url")")
        fi
    done
    
    # Summary
    echo "=================================="
    print_status "Summary:"
    print_success "Successfully created branches: $success_count/$total_count"
    
    if [[ ${#failed_repos[@]} -gt 0 ]]; then
        print_error "Failed repositories: ${failed_repos[*]}"
    fi
    
    if [[ $success_count -eq $total_count ]]; then
        print_success "All branches created successfully!"
        print_status "Repositories are available in: $WORKSPACE_DIR"
    elif [[ $success_count -gt 0 ]]; then
        print_warning "Some branches were created successfully, but there were errors with others."
    else
        print_error "No branches were created successfully."
        exit 1
    fi
}

# Run main function
main "$@"
