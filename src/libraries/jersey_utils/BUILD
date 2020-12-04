load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "jersey_utils",
    srcs = glob(["src/main/**/*.java"]),
    data = ["//data:tink_ca"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/net/src/main/java/se/tink/libraries/net/client",
        "//src/libraries/net/src/main/java/se/tink/libraries/net/client/handler",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:commons_codec_commons_codec",
        "//third_party:eu_geekplace_javapinning_java_pinning_jar",
        "//third_party:lombok_libs",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/tracing",
    ],
)

junit_test(
    name = "jersey_utils_test",
    srcs = glob(["src/test/**/*.java"]),
    runtime_deps = [
        "//src/libraries/console_json_logger",
    ],
    deps = [
        "//src/libraries/jersey_utils",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@aggregation//:com_google_guava_guava",
    ],
)
