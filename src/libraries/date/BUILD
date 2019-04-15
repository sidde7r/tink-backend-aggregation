load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "date",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.date",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_slf4j_slf4j_api",

        # Date libraries
        "//third_party:de_jollyday_jollyday",
        "//third_party:org_pojava_pojava",
        "//third_party:org_apache_commons_commons_lang3",

        # Annotations
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_swagger_swagger_annotations",

        # Guava
        "//third_party:com_google_guava_guava",
    ],
)

junit_test(
    name = "date_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/date",
        "//third_party:com_google_guava_guava",
        "//third_party:joda_time_joda_time",
        "//third_party:junit_junit",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_pojava_pojava",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
