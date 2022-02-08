load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "connectivity_errors",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentplatform",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/nemid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/exceptions/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
    ],
)

junit_test(
    name = "connectivity_errors_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentplatform",
        "//src/libraries/connectivity_errors",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
    ],
)
