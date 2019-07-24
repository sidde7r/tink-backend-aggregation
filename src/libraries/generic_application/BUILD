load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "generic_application",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.application",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/serialization",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
    ],
)

junit_test(
    name = "generic_application_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/generic_application",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
