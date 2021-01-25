load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "metrics",
    srcs = glob(["*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/commands/metrics",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/metrics",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/operation/type",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/types/counters",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/types/timers",
        "@tink_backend//src/libraries/provider",
    ],
)
