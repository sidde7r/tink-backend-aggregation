load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "validation",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/framework/validation",
        "//third_party:junit_junit",
    ],
)
