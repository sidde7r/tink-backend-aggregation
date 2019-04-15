load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "discovery",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.discovery",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_x_discovery",
    ],
)

junit_test(
    name = "discovery_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/discovery",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
