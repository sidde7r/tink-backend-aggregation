load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "amount",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.amount",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
    ],
)

junit_test(
    name = "amount-test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":amount",
        "//third_party:org_assertj_assertj_core",
    ],
)
