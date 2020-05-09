PREFIX = /usr
LIBDIR = ${PREFIX}/lib

install:
	install -Dm644 encryption.boot          ${DESTDIR}${LIBDIR}/init/rc.d/encryption.boot
	install -Dm644 encryption.post.shutdown ${DESTDIR}${LIBDIR}/init/rc.d/encryption.post.shutdown

uninstall:
	rm -f ${DESTDIR}${LIBDIR}/init/rc.d/encryption.boot
	rm -f ${DESTDIR}${LIBDIR}/init/rc.d/encryption.post.shutdown
