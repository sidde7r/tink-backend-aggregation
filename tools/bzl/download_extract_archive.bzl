def _impl(repository_ctx):
    if repository_ctx.os.name.lower().startswith("mac os"):
        url = repository_ctx.attr.macos_url
        sha256 = repository_ctx.attr.macos_sha256
        path_to_binary = repository_ctx.attr.macos_path_to_binary
    else:
        url = repository_ctx.attr.linux_url
        sha256 = repository_ctx.attr.linux_sha256
        path_to_binary = repository_ctx.attr.linux_path_to_binary
    basename = url[url.rindex("/")+1:]

    # sanitize the basename (for filenames with %20 in them)
    basename = basename.replace("%20", "-")
    repository_ctx.download(url, basename, sha256)

    # if archive is a dmg then convert it to a zip
    if basename.endswith(".dmg"):
        zipfile = basename.replace(".dmg", ".zip")
        repository_ctx.execute([repository_ctx.path(Label("//tools:convert_dmg.sh")), basename, zipfile])
        basename = zipfile

#    extract archive
    extracted_dir = "-".join([basename,"extracted"])
    repository_ctx.extract(basename,extracted_dir)
    name = repository_ctx.attr.name
    repository_ctx.symlink(extracted_dir + path_to_binary, "file/" + name)
    repository_ctx.file(
        "file/BUILD",
        "\n".join([
            ("# Automatically generated BUILD file for " + name),
            "filegroup(",
            "    name = 'file',",
            "    srcs = ['%s']," % (name),
            "    visibility = ['//visibility:public'],",
            ")",
        ]),
    )

download_extract_archive = repository_rule(
    attrs = {
        "linux_url": attr.string(),
        "linux_sha256": attr.string(),
        "linux_path_to_binary": attr.string(),
        "macos_url": attr.string(),
        "macos_sha256": attr.string(),
        "macos_path_to_binary": attr.string(),
    },
    implementation = _impl,
)