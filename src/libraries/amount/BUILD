load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "amount",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.amount",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
    ],
)

junit_test(
    name = "amount-test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":amount",
        "@maven//:org_assertj_assertj_core",
    ],
)
