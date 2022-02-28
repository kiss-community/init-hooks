#!/bin/sh

command -v lux > /dev/null && {
    log "Restoring brightness level..."
    [ -f /var/lib/lux/lux.level ] && read -r level < /var/lib/lux/lux.level
    [ -n "$level" ] && lux -S "$level"
}
