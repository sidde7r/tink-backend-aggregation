load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "discovery",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "//third_party:org_apache_curator_curator_framework",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_codehaus_jackson_jackson_core_asl",
    ],
)

junit_test(
    name = "discovery-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":discovery",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
