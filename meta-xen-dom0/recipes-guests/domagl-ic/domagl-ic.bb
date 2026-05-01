SUMMARY = "Set of files to run a generic guest domain"
DESCRIPTION = "A config file, kernel, dtb and scripts for a guest domain"

PV = "0.1"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit externalsrc systemd

EXTERNALSRC_SYMLINKS = ""

SRC_URI = "\
    file://domagl-ic-virtio.cfg \
    file://domagl-ic.service \
"

FILES:${PN} = " \
    ${sysconfdir}/xen/domagl-ic.cfg \
    ${libdir}/xen/boot/domu.dtb \
    ${libdir}/xen/boot/linux-domu \
    ${systemd_unitdir}/system/domagl-ic.service \
"

SYSTEMD_SERVICE:${PN} = "domagl-ic.service"
SYSTEMD_AUTO_ENABLE:${PN} = "${XT_AUTOSTART_DOMAGL_IC}"

# Set path to dtb file to empty value in case it was not defined before.
# In this case dtb will not be installed in 'do_install'.
# This is usefull in case if precompiled dtb is not needed and only one
# provided by xen will be used.
XT_DOMU_DTB_NAME ??= ""

do_install() {
    install -d ${D}${sysconfdir}/xen
    install -d ${D}${libdir}/xen/boot
    install -m 0644 ${WORKDIR}/domagl-ic-virtio.cfg ${D}${sysconfdir}/xen/domagl-ic.cfg
    if [ -n "${XT_DOMU_DTB_NAME}" ]; then
        install -m 0644 ${S}/${XT_DOMU_DTB_NAME} ${D}${libdir}/xen/boot/domu.dtb
    fi
    install -m 0644 ${S}/Image ${D}${libdir}/xen/boot/linux-domu

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/domagl-ic.service ${D}${systemd_unitdir}/system/
}

RDEPENDS:${PN}:append = " backend-ready"


SRC_URI += "\
    file://virtio-env.conf \
    file://virtio-env-weston.conf \
"

RDEPENDS:${PN} += " \
    launch-domain \
"

FILES:${PN} += " \
    ${sysconfdir}/systemd/system/domagl-ic.service.d/virtio-env.conf \
    ${sysconfdir}/systemd/system/domagl-ic.service.d/virtio-env-weston.conf \
"

do_install:append() {
    install -d ${D}${sysconfdir}/systemd/system/domagl-ic.service.d
    install -m 0644 ${WORKDIR}/virtio-env.conf ${D}${sysconfdir}/systemd/system/domagl-ic.service.d
    install -m 0644 ${WORKDIR}/virtio-env-weston.conf ${D}${sysconfdir}/systemd/system/domagl-ic.service.d
}
