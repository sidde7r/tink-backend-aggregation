load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "validation",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.validation",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_guava_guava",
    ],
)

junit_test(
    name = "validation_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/validation",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
