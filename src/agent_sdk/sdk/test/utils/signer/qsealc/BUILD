load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "qsealc",
    srcs = glob(["*.java"]),
    deps = [
        "//src/agent_sdk/sdk/src/utils/signer/qsealc",
        "//src/agent_sdk/sdk/src/utils/signer/signature",
        "@aggregation//:org_mockito_mockito_core",
    ],
)
