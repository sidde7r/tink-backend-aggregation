load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "standalone-agent-ob-sibs",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob([
        "src/main/resources/**/*",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/sa_agent/sa_agent_framework:sa-agent-framework",
        "//src/integration/sa_agent/sa_grpc_api:sa-grpc-api",
        "//third_party:standalone_agent_libs",
        "//third_party/sa_agent:grpc_client_libs",
    ],
)

java_binary(
    name = "standalone-agent-ob-sibs-bin",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "se.tink.sa.agent.pt.ob.sibs.SibsStandaloneAgent",
    resources = glob([
        "src/main/resources/**/*",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/sa_agent/sa_agent_framework:sa-agent-framework",
        "//src/integration/sa_agent/sa_grpc_api:sa-grpc-api",
        "//third_party:standalone_agent_libs",
        "//third_party/sa_agent:grpc_client_libs",
    ],
)

junit_test(
    name = "standalone-agent-ob-sibs-test",
    srcs = glob(["src/test/java/**/*.java"]),
    data = [
        "//data:aggregation_test",
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
        ":standalone-agent-ob-sibs",
        "//src/integration/sa_agent/sa_agent_framework:sa-agent-framework",
        "//src/integration/sa_agent/sa_grpc_api:sa-grpc-api",
        "//third_party:standalone_agent_test_libs",
        "//third_party/sa_agent:grpc_client_libs",
        "@junit_junit//jar",
        "@org_slf4j_slf4j_api//jar",
    ],
)
