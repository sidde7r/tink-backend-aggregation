load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "http_client",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/http_client/src/main/java/se/tink/libraries/http/client/masker",
        "//src/libraries/request_tracing",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:eu_geekplace_javapinning_java_pinning_jar",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "http_client_test",
    srcs = glob(["src/test/**/*.java"]),
    data = ["//data:tink_ca"],
    deps = [
        "//src/libraries/http_client",
        "//src/libraries/request_tracing",
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:org_mockito_mockito_core",
    ],
)
