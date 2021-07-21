load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "console_json_logger",
    srcs = glob(["src/main/**/*.java"]),
    resources = glob(["src/main/resources/META-INF/services/io.dropwizard.logging.AppenderFactory"]),
    visibility = ["//visibility:public"],
    runtime_deps = ["//third_party:ch_qos_logback_logback_classic"],
    deps = [
        "//third_party:ch_qos_logback_contrib_logback_jackson",
        "//third_party:ch_qos_logback_contrib_logback_json_classic",
        "//third_party:ch_qos_logback_contrib_logback_json_core",
        "//third_party:io_dropwizard_dropwizard_logging",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
    ],
)
