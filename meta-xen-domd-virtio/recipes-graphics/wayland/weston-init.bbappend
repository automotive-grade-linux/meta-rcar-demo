FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

do_install:prepend() {
    sed -i ${WORKDIR}/weston.ini \
        -e '$a shell=kiosk-shell.so' \
        -e '$a [output]' \
        -e '$a name=DP-1' \
        -e '$a app-ids=qemu-system-aarch64-domagl-ic,qemu-system-aarch64-domu' \
        -e '$a [output]' \
        -e '$a name=DSI-1' \
        -e '$a app-ids=qemu-system-aarch64-domagl-ivi,qemu-system-aarch64-doma' \
        -e '$a [output]' \
        -e '$a name=HDMI-A-1' \
        -e '$a app-ids=qemu-system-aarch64-domagl-ivi,qemu-system-aarch64-doma' \

    sed -i ${WORKDIR}/weston.service \
        -e 's|/usr/bin/weston|/usr/bin/weston --debug --log=/tmp/weston|'
}

