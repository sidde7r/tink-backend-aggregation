load("@rules_pkg//:pkg.bzl", "pkg_tar")

# TODO: as there is no data deb this section is repetitive
# data pkgs

pkg_tar(
    name = "aggregation_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation/data",
    strip_prefix = "/data",
    visibility = ["//docker:__pkg__"],
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
    visibility = ["//docker:__pkg__"],
)

pkg_tar(
    name = "libkbc_wbaes_bin_tar",
    srcs = ["//tools:libkbc_wbaes_linux"],
    mode = "0555",
    package_dir = "/usr/share/tink-backend-aggregation/tools",
    visibility = ["//docker:__pkg__"],
)

pkg_tar(
    name = "aggregation_bin_tar",
    srcs = ["//src/aggregation/service:bin_deploy"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation",
    visibility = ["//docker:__pkg__"],
)

# end binary files

# package tars

pkg_tar(
    name = "aggregation_tar",
    deps = [
        ":aggregation_bin_tar",
        ":aggregation_data_tar",
        ":aggregation_system_tar",
        ":libkbc_wbaes_bin_tar",
        ":phantomjs_bin_tar",
    ],
)

# end package tars
