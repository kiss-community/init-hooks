#!/bin/sh

command -v alsactl > /dev/null && {
    log "Restoring sound level..."
    alsactl restore
}
