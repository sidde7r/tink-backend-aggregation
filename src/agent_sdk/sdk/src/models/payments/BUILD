load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "payments",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/storage",
        "//src/integration/amount",
        "@tink_backend//src/libraries/account_identifier",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
