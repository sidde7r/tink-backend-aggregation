load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "jersey_log",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        # Log
        "//third_party:org_slf4j_slf4j_api",

        # Auth
        "//src/libraries/auth",

        # Message builder
        "//src/libraries/access_logging",

        # Metrics (Prometheus)
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:collection",
        "//src/libraries/metrics:configuration",
        "//src/libraries/metrics:logbackappender",
        "//src/libraries/metrics:monitor",
        "//src/libraries/metrics:prometheus",
        "//src/libraries/metrics:registry",
        "//src/libraries/metrics:counters",
        "//src/libraries/metrics:gauges",
        "//src/libraries/metrics:histograms",
        "//src/libraries/metrics:other",
        "//src/libraries/metrics:timers",

        # Guava
        "@aggregation//:com_google_guava_guava",

        # Guice
        "//third_party:com_google_inject_guice",

        # Jersey
        "//third_party:com_sun_jersey_jersey_client",

        # Jackson
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",

        # Dropwizard
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_jersey",

        # Codehaus Stringutils
        "//third_party:org_codehaus_plexus_plexus_utils",

        # Api utils
        "//src/libraries/api_headers",
        "//src/libraries/api_annotations",
        "//third_party:org_apache_commons_commons_lang3",
    ],
)

junit_test(
    name = "jersey_log_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/jersey_log",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
