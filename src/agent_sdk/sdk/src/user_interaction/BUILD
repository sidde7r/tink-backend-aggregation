load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "user_interaction",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "//src/agent_sdk:lombok_libs",
    ],
    intdeps = [
        "//src/libraries/serialization",
        "//src/agent_sdk/sdk/src/models/field",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
    ],
    visibility = ["//visibility:public"],
)
