load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "connectivityexceptiontest",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
    ],
)
