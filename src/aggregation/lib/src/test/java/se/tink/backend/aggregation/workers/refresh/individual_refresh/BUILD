load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "individual_refresh",
    srcs = glob(["*.java"]),
    deps = [
        #        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/ratelimit",
        #        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        #        "//src/libraries/concurrency",
        #        "//third_party:com_google_guava_guava",
        #        "//third_party:com_google_inject_guice",
        #        "//third_party:org_apache_curator_curator_framework",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/refresh/individual_refresh",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        #        "//third_party:org_slf4j_slf4j_api",
        #        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
        #        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
        #        "@tink_backend//src/libraries/provider",
    ],
)
