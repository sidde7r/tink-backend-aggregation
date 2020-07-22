load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "lansforsakringar",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
