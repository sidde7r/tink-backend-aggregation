load("@rules_java//java:defs.bzl", "java_plugin")
load("//tools/bzl:junit.bzl", "junit_test")

java_plugin(
    name = "linter",
    srcs = glob(["src/**/*.java"]),
    plugins = [
        ":auto_service_plugin",
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:com_google_auto_service_auto_service_annotations",
        "@aggregation//:com_google_errorprone_error_prone_annotation",
        "@aggregation//:com_google_errorprone_error_prone_annotations",
        "@aggregation//:com_google_errorprone_error_prone_check_api",
        "@aggregation//:com_google_errorprone_error_prone_core",
        "@aggregation//:com_google_errorprone_javac",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_projectlombok_lombok",
    ],
)

java_plugin(
    name = "auto_service_plugin",
    processor_class = "com.google.auto.service.processor.AutoServiceProcessor",
    deps = [
        "@aggregation//:com_google_auto_service_auto_service",
    ],
)

junit_test(
    name = "test",
    srcs = ["test/AgentLinterTest.java"],
    classpath_resources = glob(["test/*Cases.java"]),
    deps = [
        ":linter",
        "@aggregation//:com_google_errorprone_error_prone_test_helpers",
    ],
)
