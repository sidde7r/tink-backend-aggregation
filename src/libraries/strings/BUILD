load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "strings",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.strings",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/uuid",
        "//third_party:com_google_guava_guava",
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "strings_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/strings",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
