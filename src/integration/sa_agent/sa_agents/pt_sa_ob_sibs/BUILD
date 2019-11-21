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
