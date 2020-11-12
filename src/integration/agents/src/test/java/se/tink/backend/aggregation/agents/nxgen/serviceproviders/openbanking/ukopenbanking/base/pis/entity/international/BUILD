load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "domestic",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/base",
        "//src/integration/amount",
    ],
)
