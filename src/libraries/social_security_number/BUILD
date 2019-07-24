load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "social_security_number",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.social.security",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/credentials:demo",
        "//src/libraries/date",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_codec_commons_codec",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "social_security_number_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/social_security_number",
        "@maven//:com_google_guava_guava",
        "@maven//:joda_time_joda_time",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
