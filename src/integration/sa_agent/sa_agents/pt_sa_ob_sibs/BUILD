load("//tools/bzl:junit_old.bzl", "junit_test")

java_binary(
    name = "standalone-agent-ob-sibs-bin",
    srcs = glob(["src/main/java/**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
    ],
    jvm_flags = ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=6666"],
    main_class = "se.tink.sa.agent.pt.ob.sibs.SibsStandaloneAgent",
    resources = glob([
        "src/main/resources/**/*",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/sa_agent/sa_agent_framework:sa-agent-framework",
        "//src/integration/sa_agent/sa_common:sa-common",
        "//src/integration/sa_agent/sa_grpc_api:java_proto",
        "//src/integration/sa_agent/sa_grpc_api:sa-agent-grpc-api-proto",
        "//third_party:standalone_agent_libs",
        "//third_party/sa_agent:grpc_client_libs",
    ],
)

junit_test(
    name = "standalone-agent-ob-sibs-test",
    srcs = glob(["src/test/java/**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":standalone-agent-ob-sibs-bin",
        "//src/integration/sa_agent/sa_agent_framework:sa-agent-framework",
        "//src/integration/sa_agent/sa_common:sa-common",
        "//src/integration/sa_agent/sa_grpc_api:java_proto",
        "//src/integration/sa_agent/sa_grpc_api:sa-agent-grpc-api-proto",
        "//third_party:standalone_agent_test_libs",
        "//third_party/sa_agent:grpc_client_libs",
    ],
)

junit_test(
    name = "standalone-agent-ob-sibs-integration-test",
    srcs = glob(["src/test_integration/java/**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
    ],
    resources = glob([
        "src/test_integration/resources/**/*",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        ":standalone-agent-ob-sibs-bin",
        "//src/integration/sa_agent/sa_agent_framework:sa-agent-framework",
        "//third_party:standalone_agent_test_libs",
        "//third_party/sa_agent:grpc_client_libs",
    ],
)
