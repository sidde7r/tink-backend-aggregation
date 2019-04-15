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
        "//third_party:com_google_guava_guava",
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "social_security_number_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/social_security_number",
        "//third_party:com_google_guava_guava",
        "//third_party:joda_time_joda_time",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
