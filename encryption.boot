# vim: set ft=sh:
#
# Originally derived from https://github.com/kisslinux/init
# Modified and extended by illiliti
#

mkdir -p /run/lvm /run/cryptsetup

log "Activating LVM devices (if any exist)..."; {
    command -v lvm > /dev/null && lvm vgchange --sysinit -aay
}

log "Activating dm-crypt devices (if any exist)..."; {
    command -v cryptsetup > /dev/null && [ -e /etc/crypttab ] &&
        exec 3<&0

        # shellcheck disable=2086
        while read -r name dev pass opts err; do

            # Break on invalid crypttab (> 5 columns).
            [ "$err" ] && {
                log "Warning: A valid crypttab has only 4 columns."
                break
            }

            # Skip comments.
            [ "${name##\#*}" ] || continue

            # Turn 'UUID=*', 'LABEL=*', 'PARTUUID=*' lines into device names.
            case ${dev%%=*} in UUID|LABEL|PARTUUID)
                set -f; for line in $(blkid); do
                    case ${line%%=*} in
                        /dev/*)
                            _dev=${line%:}
                        ;;

                        UUID|LABEL|PARTUUID)
                            _line=${line##*=}
                            _line=${_line%\"}
                            _line=${_line#\"}

                            [ "$_line" = "${dev##*=}" ] && {
                                dev=${_dev}
                                break
                            }
                        ;;
                    esac
                done; set +f
            esac

            # Sanity check.
            [ "${dev##*/dev/*}" ] && {
                log "Warning: Failed to resolve device name for dm-crypt."
                continue
            }

            # Parse options by turning the list into a pseudo array.
            { old_ifs=$IFS; IFS=,; set -f; set +f -- $opts; IFS=$old_ifs; }

            # Create an argument list (no other way to do this in sh).
            for opt; do case $opt in
                discard)            copts="$copts --allow-discards" ;;
                header=*)           copts="$copts --${opt}" ;;
                readonly|read-only) copts="$copts -r" ;;
                tries=*)            copts="$copts -T ${opt##*=}" ;;
                noauto)             copts=; continue 2 ;;
            esac; done

            # If password is 'none', '-' or empty ask for it.
            case $pass in
                none|-|"") cryptsetup open $copts "$dev" "$name" <&3 ;;
                *)         cryptsetup open $copts -d "$pass" "$dev" "$name" ;;
            esac

            copts=
        done < /etc/crypttab

        exec 3>&-

        log "Activating LVM devices for dm-crypt (if any exist)..."; {
            command -v lvm > /dev/null && lvm vgchange --sysinit -aay
        }
}
