load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "sa-agent-generic-agent",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/account:enums",
        "//src/integration/amount",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/models",
        "//src/integration/sa_agent/sa_common:sa-common",
        "//src/integration/sa_agent/sa_grpc_api:sa-grpc-api",
        "//src/integration/user:rpc",
        "//src/libraries/account_identifier",
        "//third_party/sa_agent:grpc_client_libs",
        "@com_fasterxml_jackson_core_jackson_annotations//jar",
        "@org_slf4j_slf4j_api//jar",
    ],
)

junit_test(
    name = "sa-agent-generic-agent-test",
    srcs = glob(["src/test/java/**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    resources = glob([
        "src/test/resources/**/*",
    ]),
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":sa-agent-generic-agent",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/sa_agent/sa_grpc_api:sa-grpc-api",
        "//third_party/sa_agent:grpc_client_libs",
        "@junit_junit//jar",
        "@org_slf4j_slf4j_api//jar",
    ],
)
