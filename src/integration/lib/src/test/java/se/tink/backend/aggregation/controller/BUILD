load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "controller_test",
    srcs = glob(["**/*.java"]),
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/controllers",
        "//src/libraries/cache",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
