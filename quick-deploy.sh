#!/bin/bash

# Quick Deploy Script - Deploy Fat JAR Only
# Usage: ./quick-deploy.sh <version>
# Example: ./quick-deploy.sh 1.0.0

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 1.0.0"
    exit 1
fi

echo "🚀 Quick deploy version: $VERSION"

# Make mvnw executable and deploy
chmod +x ./mvnw
./mvnw versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false
./mvnw versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false -pl modules/avro
./mvnw clean install -DskipTests
./mvnw deploy -DskipTests -Davro.publish.enabled=false -pl modules/application

echo "✅ Fat JAR deployed: application-${VERSION}.jar" 



# ~/.bashrc - Colorful terminal configuration for RHEL 8

# Source global definitions if they exist
if [ -f /etc/bashrc ]; then
    . /etc/bashrc
fi

# Enable color support for various commands
if [ -x /usr/bin/dircolors ]; then
    test -r ~/.dircolors && eval "$(dircolors -b ~/.dircolors)" || eval "$(dircolors -b)"
fi

# Colorful aliases for common commands
alias ls='ls --color=auto'
alias ll='ls -alF --color=auto'
alias la='ls -A --color=auto'
alias l='ls -CF --color=auto'
alias dir='dir --color=auto'
alias vdir='vdir --color=auto'
alias grep='grep --color=auto'
alias fgrep='fgrep --color=auto'
alias egrep='egrep --color=auto'
alias diff='diff --color=auto'
alias tree='tree -C'

# Enable colored GCC warnings and errors
export GCC_COLORS='error=01;31:warning=01;35:note=01;36:caret=01;32:locus=01:quote=01'

# Color definitions for PS1 prompt
# Regular colors
RED='\[\033[0;31m\]'
GREEN='\[\033[0;32m\]'
YELLOW='\[\033[0;33m\]'
BLUE='\[\033[0;34m\]'
PURPLE='\[\033[0;35m\]'
CYAN='\[\033[0;36m\]'
WHITE='\[\033[0;37m\]'

# Bold colors
BRED='\[\033[1;31m\]'
BGREEN='\[\033[1;32m\]'
BYELLOW='\[\033[1;33m\]'
BBLUE='\[\033[1;34m\]'
BPURPLE='\[\033[1;35m\]'
BCYAN='\[\033[1;36m\]'
BWHITE='\[\033[1;37m\]'

# Reset color
NC='\[\033[0m\]' # No Color / Reset

# Function to get git branch for prompt (if git is available)
parse_git_branch() {
    git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/(\1)/'
}

# Check if we're root or regular user and set colors accordingly
if [ "$EUID" -eq 0 ]; then
    # Root user - Red prompt for safety
    PS1="${BRED}┌─[${BYELLOW}\u${BRED}@${BYELLOW}\h${BRED}]─[${BWHITE}\w${BRED}]${YELLOW}\$(parse_git_branch)${NC}\n${BRED}└─${BRED}\$${NC} "
else
    # Regular user - Green/Blue theme
    PS1="${BGREEN}┌─[${BCYAN}\u${BGREEN}@${BCYAN}\h${BGREEN}]─[${BWHITE}\w${BGREEN}]${YELLOW}\$(parse_git_branch)${NC}\n${BGREEN}└─${BGREEN}\$${NC} "
fi

# Alternative simpler colorful prompts (uncomment one to use instead):

# Simple colorful prompt:
# PS1="${GREEN}\u@\h${NC}:${BLUE}\w${NC}\$ "

# Prompt with time:
# PS1="${PURPLE}[\t] ${GREEN}\u@\h${NC}:${BLUE}\w${NC}\$ "

# Prompt with full path highlighting:
# PS1="${BGREEN}\u${NC}@${BCYAN}\h${NC}:${BYELLOW}\w${NC}${BRED}\$${NC} "

# Set LS_COLORS for colorful file listings
# This creates a comprehensive color scheme for different file types
export LS_COLORS='rs=0:di=01;34:ln=01;36:mh=00:pi=40;33:so=01;35:do=01;35:bd=40;33;01:cd=40;33;01:or=40;31;01:mi=00:su=37;41:sg=30;43:ca=30;41:tw=30;42:ow=34;42:st=37;44:ex=01;32:*.tar=01;31:*.tgz=01;31:*.arc=01;31:*.arj=01;31:*.taz=01;31:*.lha=01;31:*.lz4=01;31:*.lzh=01;31:*.lzma=01;31:*.tlz=01;31:*.txz=01;31:*.tzo=01;31:*.t7z=01;31:*.zip=01;31:*.z=01;31:*.dz=01;31:*.gz=01;31:*.lrz=01;31:*.lz=01;31:*.lzo=01;31:*.xz=01;31:*.zst=01;31:*.tzst=01;31:*.bz2=01;31:*.bz=01;31:*.tbz=01;31:*.tbz2=01;31:*.tz=01;31:*.deb=01;31:*.rpm=01;31:*.jar=01;31:*.war=01;31:*.ear=01;31:*.sar=01;31:*.rar=01;31:*.alz=01;31:*.ace=01;31:*.zoo=01;31:*.cpio=01;31:*.7z=01;31:*.rz=01;31:*.cab=01;31:*.wim=01;31:*.swm=01;31:*.dwm=01;31:*.esd=01;31:*.jpg=01;35:*.jpeg=01;35:*.mjpg=01;35:*.mjpeg=01;35:*.gif=01;35:*.bmp=01;35:*.pbm=01;35:*.pgm=01;35:*.ppm=01;35:*.tga=01;35:*.xbm=01;35:*.xpm=01;35:*.tif=01;35:*.tiff=01;35:*.png=01;35:*.svg=01;35:*.svgz=01;35:*.mng=01;35:*.pcx=01;35:*.mov=01;35:*.mpg=01;35:*.mpeg=01;35:*.m2v=01;35:*.mkv=01;35:*.webm=01;35:*.webp=01;35:*.ogm=01;35:*.mp4=01;35:*.m4v=01;35:*.mp4v=01;35:*.vob=01;35:*.qt=01;35:*.nuv=01;35:*.wmv=01;35:*.asf=01;35:*.rm=01;35:*.rmvb=01;35:*.flc=01;35:*.avi=01;35:*.fli=01;35:*.flv=01;35:*.gl=01;35:*.dl=01;35:*.xcf=01;35:*.xwd=01;35:*.yuv=01;35:*.cgm=01;35:*.emf=01;35:*.ogv=01;35:*.ogx=01;35:*.aac=00;36:*.au=00;36:*.flac=00;36:*.m4a=00;36:*.mid=00;36:*.midi=00;36:*.mka=00;36:*.mp3=00;36:*.mpc=00;36:*.ogg=00;36:*.ra=00;36:*.wav=00;36:*.oga=00;36:*.opus=00;36:*.spx=00;36:*.xspf=00;36:'

# Enable programmable completion features if available
if ! shopt -oq posix; then
  if [ -f /usr/share/bash-completion/bash_completion ]; then
    . /usr/share/bash-completion/bash_completion
  elif [ -f /etc/bash_completion ]; then
    . /etc/bash_completion
  fi
fi

# Colorful man pages
export LESS_TERMCAP_mb=$'\e[1;32m'     # begin blinking
export LESS_TERMCAP_md=$'\e[1;32m'     # begin bold
export LESS_TERMCAP_me=$'\e[0m'        # end mode
export LESS_TERMCAP_se=$'\e[0m'        # end standout-mode
export LESS_TERMCAP_so=$'\e[01;33m'    # begin standout-mode - info box
export LESS_TERMCAP_ue=$'\e[0m'        # end underline
export LESS_TERMCAP_us=$'\e[1;4;31m'   # begin underline

# Additional useful aliases with colors
alias ip='ip --color=auto'
alias dmesg='dmesg --color=auto'
alias mount='mount | column -t'
alias h='history | grep'
alias df='df -h'
alias du='du -h'
alias free='free -h'
alias ps='ps aux --forest'

# Function to display colors available in terminal
show_colors() {
    for i in {0..255}; do
        printf '\e[38;5;%dm%3d ' $i $i
        (( $i == 15 )) || (( $i > 15 )) && (( ($i-15) % 6 == 0 )) && printf '\e[0m\n'
    done
    printf '\e[0m\n'
}

# Set a colorful welcome message (optional)
echo -e "${BGREEN}Welcome to ${HOSTNAME}!${NC}"
echo -e "${CYAN}Current time: $(date)${NC}"
echo -e "${YELLOW}Uptime: $(uptime -p)${NC}"
