load("@bazel_tools//tools/build_defs/pkg:pkg.bzl", "pkg_tar", "pkg_deb")

# TODO: as there is no data deb this section is repetitive
# data pkgs
pkg_tar(
    name = "main_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-main/data",
    strip_prefix = "/data",
)

pkg_tar(
    name = "main_css_tar",
    srcs = ["//:main-bundled-css"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-main",
)

pkg_tar(
    name = "system_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-system/data",
    strip_prefix = "/data",
)

pkg_tar(
    name = "aggregation_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation/data",
    strip_prefix = "/data",
)

pkg_tar(
    name = "connector_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-connector/data",
    strip_prefix = "/data",
)

pkg_tar(
    name = "webhook_data_tar",
    srcs = ["//data:version", "//data:security"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-webhook/data",
    strip_prefix = "/data",
)

pkg_tar(
    name = "insights_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-insights/data",
    strip_prefix = "/data",
)

pkg_tar(
    name = "aggregation_controller_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation-controller/data",
    strip_prefix = "/data",
)

pkg_tar(
    name = "data_export_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-data-export/data",
    strip_prefix = "/data",
)

pkg_tar(
    name = "product_executor_data_tar",
    srcs = ["//data"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-product-executor/data",
    strip_prefix = "/data",
)

# end data pkgs

# service files
pkg_tar(
    name = "main_system_tar",
    srcs = [":tink-backend-main.service"],
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

pkg_tar(
    name = "system_system_tar",
    srcs = [":tink-backend-system.service"],
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

pkg_tar(
    name = "connector_system_tar",
    srcs = [":tink-backend-connector.service"],
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

pkg_tar(
    name = "aggregation_system_tar",
    srcs = [":tink-backend-aggregation.service"],
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

pkg_tar(
    name = "webhook_system_tar",
    srcs = [":tink-backend-webhook.service"],
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

pkg_tar(
    name = "insights_system_tar",
    srcs = [":tink-backend-insights.service"],
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

pkg_tar(
    name = "aggregation_controller_system_tar",
    srcs = [":tink-backend-aggregation-controller.service"],
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

pkg_tar(
    name = "data_export_system_tar",
    mode = "0444",
    package_dir = "/lib/systemd/system",
)

pkg_tar(
    name = "product_executor_system_tar",
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
    name = "main_bin_tar",
    srcs = ["//:renamed-main-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-main",
)

pkg_tar(
    name = "system_bin_tar",
    srcs = ["//:renamed-system-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-system",
)

pkg_tar(
    name = "aggregation_bin_tar",
    srcs = ["//:renamed-aggregation-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation",
)

pkg_tar(
    name = "connector_bin_tar",
    srcs = ["//:renamed-connector-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-connector",
)

pkg_tar(
    name = "webhook_bin_tar",
    srcs = ["//:renamed-webhook-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-webhook",
)

pkg_tar(
    name = "insights_bin_tar",
    srcs = ["//:renamed-insights-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-insights",
)

pkg_tar(
    name = "aggregation_controller_bin_tar",
    srcs = ["//:renamed-aggregation-controller-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-aggregation-controller",
)

pkg_tar(
    name = "data_export_bin_tar",
    srcs = ["//:renamed-data-export-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-data-export",
)

pkg_tar(
    name = "product_executor_bin_tar",
    srcs = ["//:renamed-product-executor-deploy-jar"],
    mode = "0444",
    package_dir = "/usr/share/tink-backend-product-executor",
)
# end binary files

# package tars
pkg_tar(
    name = "main_tar",
    deps = [
        ":main_bin_tar",
        ":main_data_tar",
        ":main_css_tar",
        ":main_system_tar",
    ],
    visibility = ["//visibility:public"],
)

pkg_tar(
    name = "system_tar",
    deps = [
        ":system_bin_tar",
        ":system_data_tar",
        ":system_system_tar",
    ],
    visibility = ["//visibility:public"],
)

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

pkg_tar(
    name = "connector_tar",
    deps = [
        ":connector_bin_tar",
        ":connector_data_tar",
        ":connector_system_tar",
    ],
    visibility = ["//visibility:public"],
)

pkg_tar(
    name = "webhook_tar",
    deps = [
        ":webhook_bin_tar",
        ":webhook_data_tar",
        ":webhook_system_tar",
    ],
    visibility = ["//visibility:public"],
)

pkg_tar(
    name = "insights_tar",
    visibility = ["//visibility:public"],
    deps = [
        ":insights_bin_tar",
        ":insights_data_tar",
        ":insights_system_tar",
    ],
)

pkg_tar(
    name = "aggregation_controller_tar",
    visibility = ["//visibility:public"],
    deps = [
        ":aggregation_controller_bin_tar",
        ":aggregation_controller_data_tar",
        ":aggregation_controller_system_tar",
    ],
)

pkg_tar(
    name = "data_export_tar",
    visibility = ["//visibility:public"],
    deps = [
        ":data_export_bin_tar",
        ":data_export_data_tar",
        ":data_export_system_tar",
    ],
)

pkg_tar(
    name = "product_executor_tar",
    visibility = ["//visibility:public"],
    deps = [
        ":product_executor_bin_tar",
        ":product_executor_data_tar",
        ":product_executor_system_tar",
    ],
)
# end package tars

# package debs
pkg_deb(
    name = "main",
    architecture = "amd64",
    data = ":main_tar",
    description = "Tink Backend Main",
    homepage = "https://tink.se",
    maintainer = "Tink AB <support@tink.se>",
    package = "tink-backend-main",
    postinst = ":control/postinst",
    preinst = ":control/preinst",
    prerm = ":control/prerm",
    version_file = ".version",
)

pkg_deb(
    name = "system",
    architecture = "amd64",
    data = ":system_tar",
    description = "Tink Backend System",
    homepage = "https://tink.se",
    maintainer = "Tink AB <support@tink.se>",
    package = "tink-backend-system",
    postinst = ":control/postinst",
    preinst = ":control/preinst",
    prerm = ":control/prerm",
    version_file = ".version",
)

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

pkg_deb(
    name = "connector",
    architecture = "amd64",
    data = ":connector_tar",
    description = "Tink Backend Connector",
    homepage = "https://tink.se",
    maintainer = "Tink AB <support@tink.se>",
    package = "tink-backend-connector",
    postinst = ":control/postinst",
    preinst = ":control/preinst",
    prerm = ":control/prerm",
    version_file = ".version",
)

pkg_deb(
    name = "webhook",
    architecture = "amd64",
    data = ":webhook_tar",
    description = "Tink Backend Webhook",
    homepage = "https://tink.se",
    maintainer = "Tink AB <support@tink.se>",
    package = "tink-backend-webhook",
    postinst = ":control/postinst",
    preinst = ":control/preinst",
    prerm = ":control/prerm",
    version_file = ".version",
)

pkg_deb(
    name = "insights",
    architecture = "amd64",
    data = ":insights_tar",
    description = "Tink Backend Insights",
    homepage = "https://tink.se",
    maintainer = "Tink AB <support@tink.se>",
    package = "tink-backend-insights",
    postinst = ":control/postinst",
    preinst = ":control/preinst",
    prerm = ":control/prerm",
    version_file = ".version",
)

pkg_deb(
    name = "aggregation_controller",
    architecture = "amd64",
    data = ":aggregation_controller_tar",
    description = "Tink Backend Aggregation Controller",
    homepage = "https://tink.se",
    maintainer = "Tink AB <support@tink.se>",
    package = "tink-backend-aggregation-controller",
    postinst = ":control/postinst",
    preinst = ":control/preinst",
    prerm = ":control/prerm",
    version_file = ".version",
)
# end package debs
