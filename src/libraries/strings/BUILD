load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "strings",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.strings",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/uuid",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_codec_commons_codec",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "strings_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/strings",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
