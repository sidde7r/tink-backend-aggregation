load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "sa-agent-generic-agent",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/sa_agent/sa_grpc_api:sa-grpc-api",
        "//third_party/sa_agent:grpc_client_libs",
        "@org_slf4j_slf4j_api//jar",
    ],
)

junit_test(
    name = "sa-agent-generic-agent-test",
    srcs = glob(["src/test/java/**/*.java"]),
    resources = glob([
        "src/test/resources/**/*",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        ":sa-agent-generic-agent",
        "//src/integration/lib",
        "//src/integration/sa_agent/sa_grpc_api:sa-grpc-api",
        "//third_party/sa_agent:grpc_client_libs",
        "@junit_junit//jar",
        "@org_slf4j_slf4j_api//jar",
    ],
)
