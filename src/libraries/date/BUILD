load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "date",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.date",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:de_jollyday_jollyday",
        "@maven//:io_protostuff_protostuff_api",
        "@maven//:io_swagger_swagger_annotations",
        "@maven//:joda_time_joda_time",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_pojava_pojava",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "date_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":date",
        "@maven//:com_google_guava_guava",
        "@maven//:joda_time_joda_time",
        "@maven//:junit_junit",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_pojava_pojava",
        "@maven//:pl_pragmatists_JUnitParams",
    ],
)
