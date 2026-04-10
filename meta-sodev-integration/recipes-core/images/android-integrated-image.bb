SUMMARY = "Integrate single Android image from Android output images"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit deploy

DEPENDS += "android-tools-native e2fsprogs-native gptfdisk-native"
SRC_URI += "file://android-images.json"

B = "${WORKDIR}/build"

ANDROID_INTEGRATED_IMAGE_NAME ?= "android_only.img"

python do_compile() {
    import json
    import os
    import shutil
    import subprocess

    SECTOR_SIZE = 512
    ALIGN_SECTORS = 2048
    BACKUP_GPT_SECTORS = 33
    build_dir = d.getVar("B")
    parts_dir = os.path.join(build_dir, "parts")
    image_name = d.getVar("ANDROID_INTEGRATED_IMAGE_NAME")
    image = os.path.join(build_dir, image_name)

    def run(*args, quiet=False):
        stdout = subprocess.DEVNULL if quiet else None
        stderr = subprocess.DEVNULL if quiet else None
        subprocess.run(args, check=True, stdout=stdout, stderr=stderr)

    def add_part(spec, next_start, partitions):
        src = ""
        size_bytes = 0
        name = spec["name"]
        typecode = spec["gpt_type"]

        if spec.get("source_var"):
            src = d.getVar(spec["source_var"]) or ""
        elif spec.get("sparse_source_var"):
            src_var = d.getVar(spec["sparse_source_var"]) or ""
            src = os.path.join(parts_dir, f"{spec['name']}.raw") if src_var else ""
            if src and os.path.isfile(src_var):
                run("simg2img", src_var, src)
        elif spec.get("empty_raw_mib") is not None:
            src = os.path.join(parts_dir, f"{spec['name']}.img")
            size_bytes = int(spec["empty_raw_mib"]) * 1024 * 1024
            run("truncate", "-s", str(size_bytes), src)
        elif spec.get("empty_ext4_mib") is not None:
            src = os.path.join(parts_dir, f"{spec['name']}.img")
            size_bytes = int(spec["empty_ext4_mib"]) * 1024 * 1024
            run("truncate", "-s", str(size_bytes), src)
            run("mkfs.ext4", "-F", "-L", spec["name"], src, quiet=True)

        if not src:
            return next_start
        elif not os.path.isfile(src):
            bb.fatal(f"Missing Android {spec['name']}: {src}")

        num = len(partitions) + 1
        actual_size = os.path.getsize(src) if src else size_bytes
        size_sectors = (actual_size + SECTOR_SIZE - 1) // SECTOR_SIZE
        start = next_start
        end = start + size_sectors - 1
        next_start = ((end + 1 + ALIGN_SECTORS - 1) // ALIGN_SECTORS) * ALIGN_SECTORS
        partitions.append((num, name, typecode, start, end, src))

        return next_start

    config_path = os.path.join(d.getVar("WORKDIR"), "android-images.json")
    if not os.path.isfile(config_path):
        bb.fatal(f"Could not find Android image parts config: {config_path}")

    shutil.rmtree(build_dir, ignore_errors=True)
    os.makedirs(parts_dir, exist_ok=True)

    with open(config_path, "r", encoding="utf-8") as f:
        part_specs = json.load(f)

    next_start = ALIGN_SECTORS
    partitions = []
    for spec in part_specs:
        next_start = add_part(spec, next_start, partitions)

    run("truncate", "-s", str((next_start + BACKUP_GPT_SECTORS) * SECTOR_SIZE), image)
    run("sgdisk", "--clear", image)

    for num, name, typecode, start, end, src in partitions:
        run("sgdisk", f"--new={num}:{start}:{end}", f"--typecode={num}:{typecode}",
            f"--change-name={num}:{name}", image)
        if src:
            run("dd", f"if={src}", f"of={image}", f"bs=1M", f"seek={start // 2048}",
                f"conv=notrunc", f"status=none")
}

do_install[noexec] = "1"

do_deploy() {
    install -Dm 0644 "${B}/${ANDROID_INTEGRATED_IMAGE_NAME}" "${DEPLOYDIR}/${ANDROID_INTEGRATED_IMAGE_NAME}"
}

addtask deploy after do_compile
