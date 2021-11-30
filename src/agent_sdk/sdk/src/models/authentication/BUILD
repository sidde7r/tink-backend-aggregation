load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "authentication",
    srcs = glob(["*.java"]),
    extdeps = [
        "//src/agent_sdk:lombok_plugin",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:org_projectlombok_lombok",
    ],
    visibility = ["//visibility:public"],
)
