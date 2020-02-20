java_library(
    name = "sa-agent-framework",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/sa_agent/sa_common:sa-common",
        "//src/integration/sa_agent/sa_grpc_api:java_proto",
        "//src/integration/sa_agent/sa_grpc_api:sa-agent-grpc-api-proto",
        "//src/integration/sa_agent/sa_grpc_api:sa-grpc-api",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:standalone_agent_libs",
        "//third_party/sa_agent:grpc_client_libs",
    ],
)
