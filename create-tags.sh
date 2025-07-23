#!/usr/bin/env bash

# Multi-Repository Tag Creator with Auto-Clone
# Usage: ./create_tags.sh <tag_name> [source_branch]

set -e  # Exit on any error

# Configuration
TAG_NAME="$1"
SOURCE_BRANCH="${2:-release}"  # Default to 'release' if not specified
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

# Function to check if tag exists
tag_exists() {
    local repo_path="$1"
    local tag="$2"
    
    cd "$repo_path"
    git tag -l | grep -q "^${tag}$" || git ls-remote --tags origin | grep -q "refs/tags/${tag}$"
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

# Function to validate tag name format
validate_tag_name() {
    local tag="$1"
    # Check for valid tag name (no spaces, special characters that cause issues)
    if [[ ! "$tag" =~ ^[a-zA-Z0-9._/-]+$ ]]; then
        print_error "Invalid tag name format: $tag"
        print_error "Tag names should only contain letters, numbers, dots, underscores, hyphens, and forward slashes"
        return 1
    fi
    return 0
}

# Function to create tag in a single repository
create_tag_in_repo() {
    local repo_url="$1"
    local repo_path=$(get_repo_path "$repo_url")
    local repo_name=$(get_repo_name "$repo_url")
    
    print_status "Processing repository: $repo_name"
    echo "Repository URL: $repo_url"
    echo "Local path: $repo_path"
    echo "Tag: $TAG_NAME" 
    echo "Source branch: $SOURCE_BRANCH"
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
    
    # Check if tag already exists
    if tag_exists "$repo_path" "$TAG_NAME"; then
        print_warning "Tag '$TAG_NAME' already exists in $repo_name"
        return 0
    fi
    
    # Fetch latest changes
    print_status "Fetching latest changes in $repo_name..."
    git fetch origin --tags
    
    # Check if source branch exists
    if ! branch_exists "$repo_path" "$SOURCE_BRANCH"; then
        print_error "Source branch '$SOURCE_BRANCH' does not exist in $repo_name"
        return 1
    fi
    
    # Switch to source branch and pull latest
    print_status "Switching to $SOURCE_BRANCH and pulling latest changes..."
    git checkout "$SOURCE_BRANCH"
    git pull origin "$SOURCE_BRANCH"
    
    # Get current commit hash for verification
    local commit_hash=$(git rev-parse HEAD)
    print_status "Creating tag at commit: ${commit_hash:0:8}"
    
    # Create lightweight tag
    print_status "Creating tag '$TAG_NAME'..."
    git tag "$TAG_NAME"
    
    # Push tag to remote
    print_status "Pushing tag '$TAG_NAME' to remote..."
    git push origin "$TAG_NAME"
    
    print_success "Successfully created tag '$TAG_NAME' in $repo_name"
    print_success "Tag points to commit: ${commit_hash:0:8}"
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

# Function to show help
show_help() {
    echo "Multi-Repository Tag Creator"
    echo ""
    echo "Usage: $0 <tag_name> [source_branch]"
    echo ""
    echo "Arguments:"
    echo "  tag_name      : Name of the tag to create (required)"
    echo "  source_branch : Branch to create tag from (default: release)"
    echo ""
    echo "Environment Variables:"
    echo "  WORKSPACE_DIR : Directory to clone repositories into (default: ./workspace)"
    echo ""
    echo "Examples:"
    echo "  $0 v1.0.0"
    echo "  $0 v1.0.0 main"
    echo "  $0 v1.0.1 hotfix/urgent-fix"
    echo "  WORKSPACE_DIR=/tmp/repos $0 v2.0.0-beta"
    echo ""
    echo "Tag Naming Conventions:"
    echo "  - Semantic versioning: v1.0.0, v1.2.3"
    echo "  - Pre-release: v1.0.0-alpha, v1.0.0-beta.1"
    echo "  - Build metadata: v1.0.0+20230615"
    echo "  - Date-based: 2023.06.15, 20230615-1"
}

# Main function
main() {
    # Check if tag name is provided
    if [[ -z "$TAG_NAME" ]]; then
        show_help
        exit 1
    fi
    
    # Show help if requested
    if [[ "$TAG_NAME" == "-h" || "$TAG_NAME" == "--help" ]]; then
        show_help
        exit 0
    fi
    
    # Validate tag name
    if ! validate_tag_name "$TAG_NAME"; then
        exit 1
    fi
    
    # Validate all repository URLs
    print_status "Validating repository URLs..."
    for repo_url in "${REPOSITORIES[@]}"; do
        if ! validate_ssh_url "$repo_url"; then
            exit 1
        fi
    done
    
    print_status "Creating tag '$TAG_NAME' from branch '$SOURCE_BRANCH' in ${#REPOSITORIES[@]} repositories..."
    print_status "Workspace directory: $WORKSPACE_DIR"
    echo ""
    
    # Create workspace directory if it doesn't exist
    mkdir -p "$WORKSPACE_DIR"
    
    local success_count=0
    local total_count=${#REPOSITORIES[@]}
    local failed_repos=()
    local successful_repos=()
    
    # Process each repository
    for repo_url in "${REPOSITORIES[@]}"; do
        if create_tag_in_repo "$repo_url"; then
            ((success_count++))
            successful_repos+=("$(get_repo_name "$repo_url")")
        else
            failed_repos+=("$(get_repo_name "$repo_url")")
        fi
    done
    
    # Summary
    echo "=================================="
    print_status "Summary:"
    print_success "Successfully created tags: $success_count/$total_count"
    
    if [[ ${#successful_repos[@]} -gt 0 ]]; then
        print_success "Successful repositories: ${successful_repos[*]}"
    fi
    
    if [[ ${#failed_repos[@]} -gt 0 ]]; then
        print_error "Failed repositories: ${failed_repos[*]}"
    fi
    
    if [[ $success_count -eq $total_count ]]; then
        print_success "All tags created successfully!"
        print_status "Tag: $TAG_NAME"
        print_status "Repositories are available in: $WORKSPACE_DIR"
        echo ""
        print_status "To list tags in a repository:"
        print_status "  cd $WORKSPACE_DIR/<repo_name> && git tag -l"
        print_status "To show tag details:"
        print_status "  cd $WORKSPACE_DIR/<repo_name> && git show $TAG_NAME"
    elif [[ $success_count -gt 0 ]]; then
        print_warning "Some tags were created successfully, but there were errors with others."
    else
        print_error "No tags were created successfully."
        exit 1
    fi
}

# Run main function
main "$@"
