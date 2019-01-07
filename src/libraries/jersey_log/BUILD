load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "jersey-log",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        # Log
        "//src/libraries/log:log",

        # Auth
        "//src/libraries/auth:auth",
        
        # Message builder
        "//src/libraries/access_logging:access_logging",

        # Metrics (Prometheus)
        "//src/libraries/metrics:metrics",

        # Guava
        "//third_party:com_google_guava_guava",

        # Guice
        "//third_party:com_google_inject_guice",

        # Jersey
        "//third_party:com_sun_jersey_jersey_client",

        # Jackson
        "//third_party:com_fasterxml_jackson_core_jackson_databind",

        # Dropwizard
        "//third_party:io_dropwizard_dropwizard_core",

        # Codehaus Stringutils
        "//third_party:org_codehaus_plexus_plexus_utils",

        # Api utils
        "//src/api-headers",
        "//src/libraries/api_annotations:api-annotations",
    ],
)

junit_test(
    name = "jersey-log-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":jersey-log",

        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:com_google_guava_guava",
    ],
)
