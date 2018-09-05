load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "jersey-utils",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    data = ["//data:tink-ca"],
    deps = [
        "//src/libraries/net",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",

        "//third_party:tink_org_apache_httpcomponents_httpclient",
        "//third_party:tink_org_apache_httpcomponents_httpcore",

        "//third_party:com_google_guava_guava",

        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_server",
        "@commons_codec_commons_codec//jar",

        "//third_party:eu_geekplace_javapinning_java_pinning_jar",
    ],
)

junit_test(
    name = "jersey-utils-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":jersey-utils",

        "//src/libraries/net",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:com_google_guava_guava",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_jersey_client",
    ],
)
