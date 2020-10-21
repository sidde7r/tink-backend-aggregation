load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "aktia",
    srcs = glob(["**/*.java"]),
    data = [
        "//:agent_test_data",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:commons_io_commons_io",
        "//third_party:org_mockito_mockito_core",
    ],
)
