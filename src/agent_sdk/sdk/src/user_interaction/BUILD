load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "user_interaction",
    srcs = glob(["*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:com_google_code_findbugs_jsr305",
        "@agent_sdk_maven//:org_projectlombok_lombok",
        "//src/agent_sdk:lombok_plugin",
    ],
    intdeps = [
        "@tink_backend//src/libraries/serialization_utils",
        "//src/agent_sdk/sdk/src/models/field",
        "//src/agent_sdk/sdk/src/user_interaction/swedish_mobile_bankid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/agent_sdk/sdk/src/user_interaction/swedish_mobile_bankid",
    ],
)
