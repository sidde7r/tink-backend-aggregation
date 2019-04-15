load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "jersey_log",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/access_logging",
        "//src/libraries/api_annotations",
        "//src/libraries/api_headers",
        "//src/libraries/auth",
        "//src/libraries/metrics",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_codehaus_plexus_plexus_utils",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "jersey_log_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/jersey_log",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
