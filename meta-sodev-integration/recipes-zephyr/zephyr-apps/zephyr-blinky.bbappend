FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
ZEPHYR_TOOLCHAIN_VARIANT = "zephyr"
require recipes-kernel/zephyr-kernel/zephyr-toolchain-zephyr.inc
