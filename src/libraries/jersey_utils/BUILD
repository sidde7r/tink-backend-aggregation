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
        "@maven//:com_google_guava_guava",
        "@maven//:com_sun_jersey_contribs_jersey_apache_client4",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:com_sun_jersey_jersey_server",
        "@maven//:commons_codec_commons_codec",
        "@maven//:eu_geekplace_javapinning_java_pinning_jar",
        "@maven//:org_apache_httpcomponents_httpclient",
        "@maven//:org_apache_httpcomponents_httpcore",
        "@maven//:org_bouncycastle_bcpkix_jdk15on",
        "@maven//:org_bouncycastle_bcprov_jdk15on",
    ],
)

junit_test(
    name = "jersey_utils_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/jersey_utils",
        "@maven//:com_github_tomakehurst_wiremock_standalone",
        "@maven//:com_google_guava_guava",
        "@maven//:com_sun_jersey_jersey_client",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_bouncycastle_bcpkix_jdk15on",
        "@maven//:org_bouncycastle_bcprov_jdk15on",
    ],
)
