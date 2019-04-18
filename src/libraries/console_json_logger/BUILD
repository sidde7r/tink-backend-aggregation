load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "console_json_logger",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.logger.json",
    resources = glob(["src/main/resources/META-INF/services/io.dropwizard.logging.AppenderFactory"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:io_dropwizard_dropwizard_logging",
        "@maven//:ch_qos_logback_contrib_logback_jackson",
        "@maven//:ch_qos_logback_contrib_logback_json_classic",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
    ],
)
