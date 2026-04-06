SUMMARY = "Flatcar Container Linux guest domain"
DESCRIPTION = "Config files and kernel for Flatcar guest domain"

PV = "0.1"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit externalsrc systemd

EXTERNALSRC_SYMLINKS = ""

SRC_URI = "\
    file://dom-flatcar-virtio.cfg \
    file://dom-flatcar.service \
    file://dom-flatcar-set-root \
    file://virtio-env.conf \
"

FILES:${PN} = " \
    ${sysconfdir}/xen/dom-flatcar.cfg \
    ${libdir}/xen/boot/linux-flatcar \
    ${systemd_unitdir}/system/dom-flatcar.service \
    ${sysconfdir}/systemd/system/dom-flatcar.service.d/virtio-env.conf \
    ${libdir}/xen/bin/dom-flatcar-set-root \
"

SYSTEMD_SERVICE:${PN} = "dom-flatcar.service"
SYSTEMD_AUTO_ENABLE:${PN} = "${XT_AUTOSTART_DOM_FLATCAR}"

do_install() {
    # config for xen flatcar domu
    install -d ${D}${sysconfdir}/xen
    install -m 0644 ${WORKDIR}/dom-flatcar-virtio.cfg ${D}${sysconfdir}/xen/dom-flatcar.cfg

    # Flatcar kernel
    # install vmlinuz-a in the directory specified by EXTERNALSRC as linux-flatcar
    install -d ${D}${libdir}/xen/boot
    install -m 0644 ${S}/vmlinuz-a ${D}${libdir}/xen/boot/linux-flatcar

    # systemd services
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/dom-flatcar.service ${D}${systemd_unitdir}/system/

    # launch-domain drop-in
    install -d ${D}${sysconfdir}/systemd/system/dom-flatcar.service.d
    install -m 0644 ${WORKDIR}/virtio-env.conf ${D}${sysconfdir}/systemd/system/dom-flatcar.service.d

    # set-root script
    install -d ${D}${libdir}/xen/bin
    install -m 0744 ${WORKDIR}/dom-flatcar-set-root ${D}${libdir}/xen/bin
}

RDEPENDS:${PN}:append = " backend-ready"
RDEPENDS:${PN} += " launch-domain"
