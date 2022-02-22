#!/bin/sh
#
# Derived from https://github.com/kisslinux/init

command -v lvm > /dev/null && {
    log "Activating LVM devices (if any exist)..."
    mkdir -p /run/lvm 2> /dev/null
    lvm vgchange --sysinit -aay
}
