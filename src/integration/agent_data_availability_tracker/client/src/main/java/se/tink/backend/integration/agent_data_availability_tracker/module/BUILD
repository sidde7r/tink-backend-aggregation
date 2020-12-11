java_library(
    name = "agent-data-availability-tracker-module",
    srcs = glob(
        ["**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agent_data_availability_tracker/client",
        "//third_party:com_google_inject_guice",
        "//third_party:io_grpc_grpc_core",
        "//third_party:io_grpc_grpc_netty",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_netty_netty_handler",
        "@tink_backend//src/agent-data-availability-tracker/src/main/java/se/tink/backend/agent_data_availability_tracker/client",
    ],
)
