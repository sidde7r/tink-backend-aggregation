load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "concurrency",
    srcs = glob(["*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/concurrency",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/ratelimit",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/libraries/concurrency/src/test/java/se/tink/libraries/concurrency/ticker",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
    ],
)
