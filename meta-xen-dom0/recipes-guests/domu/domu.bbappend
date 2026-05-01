FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

RDEPENDS:${PN}:append = " backend-ready"
# It is used a lot in the do_install, so variable will be handy
CFG_FILE="${D}${sysconfdir}/xen/domu.cfg"

SYSTEMD_AUTO_ENABLE:${PN} = "${XT_AUTOSTART_DOMU}"
