load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "demoagent",
    srcs = glob(["**/*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/demo/banks/psd2/redirect",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
    ],
)
