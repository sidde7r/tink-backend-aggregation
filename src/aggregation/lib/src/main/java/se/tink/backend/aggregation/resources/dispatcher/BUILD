load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "dispatcher",
    srcs = glob(
        include = ["*.java"],
        exclude = ["*Test.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api/src/main/java/se/tink/backend/aggregation/cluster/identification",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/queue",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/worker",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/libraries/queue_lib",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
    ],
)

junit_test(
    name = "dispatcher-tests",
    srcs = glob(["*Test.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":dispatcher",
        "//src/aggregation/api/src/main/java/se/tink/backend/aggregation/cluster/identification",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/worker",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/libraries/queue_lib",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/types/counters",
    ],
)
