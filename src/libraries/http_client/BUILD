load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "http_client",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.http.client",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/request_tracing",
        "@maven//:com_google_guava_guava",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:com_sun_jersey_jersey_server",
        "@maven//:eu_geekplace_javapinning_java_pinning_jar",
    ],
)

junit_test(
    name = "http_client_test",
    srcs = glob(["src/test/**/*.java"]),
    data = ["//data:tink_ca"],
    deps = [
        ":http_client",
        "//src/libraries/request_tracing",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:com_github_tomakehurst_wiremock_standalone",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:com_sun_jersey_jersey_server",
        "@maven//:org_mockito_mockito_core",
    ],
)
