load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "jersey-utils",
    srcs = glob(["src/main/**/*.java"]),
    data = ["//data:tink-ca"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/net",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:eu_geekplace_javapinning_java_pinning_jar",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "@commons_codec_commons_codec//jar",
    ],
)

junit_test(
    name = "jersey-utils-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":jersey-utils",
        "//src/libraries/net",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
