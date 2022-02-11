load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "authentication",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:org_projectlombok_lombok",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
    ],
    intdeps = [
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/runtime/src/authentication",
        "//src/agent_sdk/sdk/src/utils",
        "//src/agent_sdk/compatibility_layers/aggregation_service/src/steppable_execution",
    ],
    plugins = [
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = ["//visibility:public"],
)
