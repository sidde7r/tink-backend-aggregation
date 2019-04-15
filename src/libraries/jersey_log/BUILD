load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "jersey_log",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.jersey.logging",
    visibility = ["//visibility:public"],
    deps = [
        # Log
        "//third_party:org_slf4j_slf4j_api",

        # Auth
        "//src/libraries/auth",

        # Message builder
        "//src/libraries/access_logging",

        # Metrics (Prometheus)
        "//src/libraries/metrics",

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
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
