load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "identity_data",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.identitydata",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_slf4j_slf4j_api",

        # Guava
        "//third_party:com_google_guava_guava",

        # Jackson
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
)

junit_test(
    name = "identity_data_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/identity_data",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
