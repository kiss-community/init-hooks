SYSCONFDIR = /etc

install:
	install -Dm644 encryption.boot 			${DESTDIR}${SYSCONFDIR}/rc.d/encryption.boot
	install -Dm644 encryption.post.shutdown ${DESTDIR}${SYSCONFDIR}/rc.d/encryption.post.shutdown

uninstall:
	rm -f ${DESTDIR}${SYSCONFDIR}/rc.d/encryption.boot
	rm -f ${DESTDIR}${SYSCONFDIR}/rc.d/encryption.post.shutdown
