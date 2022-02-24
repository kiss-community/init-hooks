PREFIX = /usr
LIBDIR = ${PREFIX}/lib

install:
	mkdir -p        ${DESTDIR}${LIBDIR}/init/rc.d
	cp */*.boot     ${DESTDIR}${LIBDIR}/init/rc.d/
	cp */*.shutdown ${DESTDIR}${LIBDIR}/init/rc.d/
