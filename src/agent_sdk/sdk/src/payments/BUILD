load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "payments",
    # TODO: use individual BUILD files for each package once we're happy with the layout.
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/models/payments",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/steppable_execution",
        "@tink_backend//src/libraries/account_identifier",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
