load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "phone_number_utils",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.phonenumbers",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_googlecode_libphonenumber_libphonenumber",
    ],
)

junit_test(
    name = "phone_number_utils_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/phone_number_utils",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
