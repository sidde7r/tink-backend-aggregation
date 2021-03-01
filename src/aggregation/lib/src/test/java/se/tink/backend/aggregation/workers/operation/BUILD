load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "operation",
    srcs = glob(["*.java"]),
    runtime_deps = [
        "//po:i18n",
    ],
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller/v1/rpc",
        "//src/aggregation/api",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/events",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/commands",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/operation",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/controllers",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_zookeeper_zookeeper",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/collection",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/types/timers",
    ],
)
