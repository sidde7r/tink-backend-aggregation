load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "wrappers",
    srcs = glob(["**/*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/models",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/wrappers",
        "//third_party:org_assertj_assertj_core",
    ],
)
