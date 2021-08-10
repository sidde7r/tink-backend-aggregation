load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "resources",
    srcs = glob(["*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/resources",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/startupchecks",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/abort",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/operation",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/worker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/controllers",
        "//src/libraries/draining",
        "//src/libraries/queue_lib",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
