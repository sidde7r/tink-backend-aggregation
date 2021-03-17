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

pkg_tar(
    name = "chromedriver_bin_tar",
    srcs = ["@chromedriver//file"],
    mode = "0555",
    package_dir = "/usr/share/tink-backend-aggregation/external/chromedriver/file",
    visibility = ["//docker:__pkg__"],
)

pkg_tar(
    name = "chrome_bin_tar",
    srcs = ["@chromium//file"],
    mode = "0555",
    package_dir = "/usr/share/tink-backend-aggregation/external/chromium/file",
    visibility = ["//docker:__pkg__"],
)

# end binary files
