load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "authentication",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/fakelogmasker",
        "//third_party:org_assertj_assertj_core",
    ],
)
