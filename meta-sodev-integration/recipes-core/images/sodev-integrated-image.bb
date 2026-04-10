SUMMARY = "Full SoDeV disk image assembled without moulin/rouge"
DESCRIPTION = "Creates a GPT disk image aligned with moulin full.img semantics."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

inherit image

SRC_URI += "file://sodev-images.json"

IMAGE_FSTYPES ?= "wic"
WKS_FILE = "sodev-integrated-image.wks.in"
WKS_FILE_DEPENDS = "e2fsprogs-native dosfstools-native mtools-native gptfdisk-native"
WICVARS:append = " SODEV_IMAGE_DOMD_IMAGE SODEV_IMAGE_FREE_SPACE SODEV_IMAGE_PARTS"

ANDROID_INTEGRATED_IMAGE_NAME ?= "android_only.img"

SODEV_IMAGE_DOM0_FITIMAGE = "${DEPLOY_DIR_IMAGE}/fitImage"
SODEV_IMAGE_DOM0_FLASHBIN = "${DEPLOY_DIR_IMAGE}/ipl-burning/flash.bin"
SODEV_IMAGE_ANDROID ?= "${@d.expand('${DEPLOY_DIR_IMAGE}/${ANDROID_INTEGRATED_IMAGE_NAME}') if (d.getVar('SODEV_IMAGE_ANDROID_BOOT_IMG') or '') else ''}"
SODEV_IMAGE_FREE_SPACE ?= "16367K"

def sodev_images(d):
    import json
    import os

    config_path = bb.utils.which(d.getVar("FILESPATH"), "sodev-images.json")
    if not config_path:
        bb.fatal("Could not find sodev-images.json")

    with open(config_path, "r", encoding="utf-8") as f:
        specs = json.load(f)

    parts = []

    for spec in specs:
        image_path = d.getVar(spec["source_var"]) or ""
        if not image_path:
            continue

        parts.append(
            'part %s --source rawcopy --sourceparams="file=%s" '
            '--ondisk mmcblk0 --align 1024 --part-name %s '
            '--part-type %s' % ('/' + spec["name"], image_path, spec["name"], spec["gpt_type"])
        )

    return "\n".join(parts)

SODEV_IMAGE_PARTS = "${@sodev_images(d)}"

IMAGE_BOOT_FILES = " \
    ${SODEV_IMAGE_DOM0_FITIMAGE};fitImage \
    ${SODEV_IMAGE_DOM0_FLASHBIN};flash.bin \
    ${SODEV_IMAGE_DOM0_FITIMAGE};boot/fitImage \
"

do_image_wic[depends] += " \
    core-image-thin-initramfs:do_image_complete \
    linux-fitimage:do_compile \
    ipl-burning:do_deploy \
    ${@'android-integrated-image:do_deploy' if (d.getVar('SODEV_IMAGE_ANDROID_BOOT_IMG') or '') else ''} \
"

do_populate_lic_deploy[noexec] = "1"
