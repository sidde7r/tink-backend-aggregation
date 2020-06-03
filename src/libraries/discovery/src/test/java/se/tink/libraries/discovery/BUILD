load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "discovery",
    srcs = glob(["*.java"]),
    deps = [
        "//src/libraries/discovery/src/main/java/se/tink/libraries/discovery",
        "//third_party:junit_junit",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
    ],
)
