java_library(
    name = "console_json_logger",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    resources = glob(["src/main/resources/META-INF/services/io.dropwizard.logging.AppenderFactory"]),
    deps = [
        "//third_party:ch_qos_logback_contrib_logback_json_classic",
        "//third_party:ch_qos_logback_contrib_logback_jackson",
        "//third_party:io_dropwizard_dropwizard_logging",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
    ],
    runtime_deps = ["//third_party:ch_qos_logback_logback_classic"],
)
