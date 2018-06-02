load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar", "pkg_deb")

# TODO: as there is no data deb this section is repetitive
# data pkgs

pkg_tar(
    name = "aggregation_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation/data",
    strip_prefix = "/data",
)

# end data pkgs

# service files

pkg_tar(
    name = "aggregation_system_tar",
    srcs = [":tink-backend-aggregation.service"],
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

# end service files

# binary files
pkg_tar(
    name = "phantomjs_bin_tar",
    srcs = ["//tools:phantomjs_linux"],
    mode = "0555",
    package_dir = "/usr/share/tink-backend-aggregation/tools",
)

pkg_tar(
    name = "libkbc_wbaes_bin_tar",
    srcs = ["//tools:libkbc_wbaes_linux"],
    mode = "0555",
    package_dir = "/usr/share/tink-backend-aggregation/tools",
)

pkg_tar(
    name = "aggregation_bin_tar",
    srcs = ["//:renamed-aggregation-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation",
)
# end binary files

# package tars

pkg_tar(
    name = "aggregation_tar",
    deps = [
        ":aggregation_bin_tar",
        ":aggregation_data_tar",
        ":aggregation_system_tar",
        ":phantomjs_bin_tar",
        ":libkbc_wbaes_bin_tar",
    ],
    visibility = ["//visibility:public"],
)

# end package tars

# package debs

pkg_deb(
    name = "aggregation",
    architecture = "amd64",
    data = ":aggregation_tar",
    description = "Tink Backend Aggregation",
    homepage = "https://tink.se",
    maintainer = "Tink AB <support@tink.se>",
    package = "tink-backend-aggregation",
    depends = [
        "imagemagick",
        "tesseract-ocr",
        "tesseract-ocr-swe",
        "xvfb",
        "libm4ri-0.0.20140914",
    ],
    postinst = ":control/postinst",
    preinst = ":control/preinst",
    prerm = ":control/prerm",
    version_file = ".version",
)
# end package debs
