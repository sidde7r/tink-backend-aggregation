load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "generic_application",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.application",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
    ],
)

junit_test(
    name = "generic_application_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/generic_application",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
