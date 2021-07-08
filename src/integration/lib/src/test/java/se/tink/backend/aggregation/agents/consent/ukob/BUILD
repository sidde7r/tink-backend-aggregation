load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "ukob",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/consent/ukob",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
