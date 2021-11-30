load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "storage",
    srcs = glob(["*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_core",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_databind",
        "@agent_sdk_maven//:org_projectlombok_lombok",
        "//src/agent_sdk:lombok_plugin",
    ],
    intdeps = [
        "@tink_backend//src/libraries/serialization_utils",
        "//src/agent_sdk/sdk/src/models/authentication",
    ],
    visibility = ["//visibility:public"],
)
