load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "serialization",
    extdeps = [
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_core",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_databind",
    ],
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
    exports = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_core",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
