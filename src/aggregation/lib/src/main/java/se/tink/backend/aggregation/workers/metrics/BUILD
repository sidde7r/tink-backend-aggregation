load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "metrics",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/commands/metrics",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/operation/type",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//third_party:com_google_guava_guava",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_recipes",
        "//third_party:org_slf4j_slf4j_api",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/types/counters",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/types/timers",
    ],
)
