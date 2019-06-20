load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "jersey_log",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.jersey.logging",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/access_logging",
        "//src/libraries/api_annotations",
        "//src/libraries/api_headers",
        "//src/libraries/auth",
        "//src/libraries/metrics",
        "//third_party:io_dropwizard_dropwizard_core",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:com_sun_jersey_jersey_server",
        "@maven//:javax_servlet_javax_servlet_api",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_codehaus_plexus_plexus_utils",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "jersey_log_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/jersey_log",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
