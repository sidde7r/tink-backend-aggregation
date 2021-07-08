load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "abort",
    srcs = glob(["**/*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/abort",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/operation",
        "//third_party:org_mockito_mockito_core",
    ],
)
