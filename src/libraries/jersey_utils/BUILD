load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "jersey_utils",
    srcs = glob(["src/main/**/*.java"]),
    data = ["//data:tink_ca"],
    groupId = "se.tink.libraries.jersey.utils",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/net",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_contribs_jersey_apache_client4",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:commons_codec_commons_codec",
        "//third_party:eu_geekplace_javapinning_java_pinning_jar",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)

junit_test(
    name = "jersey_utils_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/jersey_utils",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
