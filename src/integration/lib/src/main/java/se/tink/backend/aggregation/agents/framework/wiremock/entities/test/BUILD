load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "test",
    srcs = glob(
        ["*.java"],
    ),
    data = [
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/entities",
        "//third_party:org_apache_httpcomponents_httpcore",
    ],
)
