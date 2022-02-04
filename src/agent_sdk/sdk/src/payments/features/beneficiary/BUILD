load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "beneficiary",
    srcs = glob(["*.java"]),
    intdeps = [
        "@tink_backend//src/libraries/account_identifier",
        "//src/agent_sdk/sdk/src/payments/beneficiary",
    ],
    visibility = [
        "//src/agent_sdk:__subpackages__",
    ],
)
