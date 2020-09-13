# vim: set ft=sh:
#
# Originally derived from https://github.com/kisslinux/init
# Modified and extended by illiliti
#

# disable globbing because we don't need it
set -f

# supress error output due to sbase mkdir
# complain about exist directories
mkdir -p /run/lvm /run/cryptsetup 2> /dev/null

command -v lvm > /dev/null && {
    log "Activating LVM devices (if any exist)..."
    lvm vgchange --sysinit -aay
}

command -v cryptsetup > /dev/null && test -f /etc/crypttab && {
    log "Activating LUKS devices (if any exist)..."

    exec 3<&0; while read -r name dev pass opts err; do

        # Skip comments.
        [ "${name##\#*}" ] || continue

        # Break on invalid crypttab.
        [ "$err" ] && {
            log "A valid /etc/crypttab has only 4 columns. Aborting..."
            break
        }

        # Turn 'UUID=*', 'LABEL=*', 'PARTUUID=*' into device name.
        case "${dev%%=*}" in UUID|LABEL|PARTUUID)
            for line in $(blkid); do case "${line%%=*}" in
                /dev/*)
                    _dev="${line%:}"
                ;;
                UUID|LABEL|PARTUUID)
                    _line="${line##*=}"
                    _line="${_line%\"}"
                    _line="${_line#\"}"

                    [ "$_line" = "${dev##*=}" ] && {
                        dev="$_dev"
                        break
                    }
                ;;
            esac; done
        esac

        # Sanity check.
        [ "${dev#/dev/*}" ] && {
            log "Failed to resolve $dev. Aborting..."
            continue
        }

        # Parse options by turning the list into a pseudo array.
        # shellcheck disable=2086
        { IFS=,; set -- $opts; unset IFS; }

        # Create an argument list (no other way to do this in sh).
        for opt; do case "$opt" in
            readonly|read-only) copts="$copts -r"     ;;
            header=*) copts="$copts --${opt}"         ;;
            tries=*)  copts="$copts -T ${opt##*=}"    ;;
            discard)  copts="$copts --allow-discards" ;;
            noauto)   copts=; continue 2              ;;
        esac; done

        # If password is 'none', '-' or empty ask for it.
        # shellcheck disable=2086
        case "$pass" in
            none|-|"") cryptsetup open $copts "$dev" "$name" <&3 ;;
            *) cryptsetup open $copts -d "$pass" "$dev" "$name"  ;;
        esac
    copts=; done < /etc/crypttab; exec 3>&-

    command -v lvm > /dev/null && {
        log "Activating LVM devices for LUKS (if any exist)..."
        lvm vgchange --sysinit -aay
        lvmvgscan --mknodes
    }
}

# restore globbing
set +f
